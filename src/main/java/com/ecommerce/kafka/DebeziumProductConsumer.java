package com.ecommerce.kafka;

import com.ecommerce.document.ProductDocument;
import com.ecommerce.model.Category;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductSearchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Kafka listener that consumes Debezium CDC events from the products topic
 * and keeps the Elasticsearch index in sync with MySQL.
 *
 * ── How Debezium CDC events look ─────────────────────────────────────────────
 * With schemas.enable=false, each Kafka message value is:
 *
 * {
 *   "before": { ...old row... } or null,
 *   "after":  { ...new row... } or null,
 *   "op":     "c" | "u" | "d" | "r",
 *   "source": { "db": "ecommerce", "table": "products", ... }
 * }
 *
 * Operation codes:
 *   "c" = INSERT (create)
 *   "u" = UPDATE
 *   "d" = DELETE
 *   "r" = READ (snapshot — initial dump of existing rows on connector first start)
 *
 *
 *  - Manual ACK (enable-auto-commit=false): offsets are committed only after
 *    a successful ES write. If the ES call fails, the message is not committed
 *    and will be reprocessed — guarantees at-least-once delivery.
 */
@Component
public class DebeziumProductConsumer {

    private static final Logger log = LoggerFactory.getLogger(DebeziumProductConsumer.class);

    private final ProductSearchRepository searchRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.debezium.products-topic}")
    private String productsTopic;

    public DebeziumProductConsumer(ProductSearchRepository searchRepository,
                                   CategoryRepository categoryRepository,
                                   ObjectMapper objectMapper) {
        this.searchRepository   = searchRepository;
        this.categoryRepository = categoryRepository;
        this.objectMapper       = objectMapper;
    }

    @KafkaListener(topics = "${app.debezium.products-topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message, Acknowledgment acknowledgment) {
        try {
            JsonNode root    = objectMapper.readTree(message);
            String   op      = root.path("op").asText();
            JsonNode payload = root.path("after");   // null for deletes
            JsonNode before  = root.path("before");  // null for inserts

            log.debug("[CDC] op={} table=products", op);

            switch (op) {
                case "c", "r", "u" -> {
                    // c = insert, r = snapshot read, u = update — all upsert to ES
                    if (!payload.isMissingNode() && !payload.isNull()) {
                        ProductDocument doc = toDocument(payload);
                        searchRepository.save(doc);
                        log.info("[CDC] Upserted product id={} to ES (op={})", doc.getId(), op);
                    }
                }
                case "d" -> {
                    // d = delete — remove from ES using the 'before' state id
                    if (!before.isMissingNode() && !before.isNull()) {
                        Integer id = before.path("id").asInt();
                        searchRepository.deleteById(id);
                        log.info("[CDC] Deleted product id={} from ES", id);
                    }
                }
                default -> log.warn("[CDC] Unknown op='{}' — skipping", op);
            }

            // Commit offset only after successful ES operation (at-least-once guarantee)
            acknowledgment.acknowledge();

        } catch (Exception e) {
            // Log the error but do NOT acknowledge — Kafka will redeliver the message.
            // In production you would route to a dead-letter topic after N retries.
            log.error("[CDC] Failed to process message: {}", e.getMessage(), e);
        }
    }

    private ProductDocument toDocument(JsonNode row) {
        ProductDocument doc = new ProductDocument();
        doc.setId(row.path("id").asInt());
        doc.setTitle(row.path("title").asText(null));
        doc.setDescription(row.path("description").asText(null));
        doc.setPrice(nullableDouble(row, "price"));
        doc.setDiscountPercentage(nullableDouble(row, "discount_percentage"));
        doc.setRating(nullableDouble(row, "rating"));
        doc.setStock(nullableInt(row, "stock"));
        doc.setBrand(row.path("brand").asText(null));
        doc.setThumbnail(row.path("thumbnail").asText(null));
        doc.setCreatedAt(LocalDateTime.now());

        int categoryId = row.path("category_id").asInt(0);
        if (categoryId > 0) {
            categoryRepository.findById((long) categoryId)
                    .map(Category::getName)
                    .ifPresent(doc::setCategory);
        }

        doc.setImages(java.util.Collections.emptyList());
        doc.setTags(java.util.Collections.emptyList());

        return doc;
    }

    private Double nullableDouble(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return (n.isMissingNode() || n.isNull()) ? null : n.asDouble();
    }

    private Integer nullableInt(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return (n.isMissingNode() || n.isNull()) ? null : n.asInt();
    }
}