package com.ecommerce.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch document for a product.
 *
 * Kept separate from the JPA entity on purpose:
 *   - The ES document is denormalised (category as a flat string, images/tags as arrays)
 *     because Elasticsearch is optimised for that shape.
 *   - The JPA entity uses a normalised relational schema (FK to categories, child tables).
 *
 * createIndex = false → we create the index manually in DataSeeder with explicit settings.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "products", createIndex = false)
public class ProductDocument {

    @Id
    private Integer id;

    /**
     * Mapped as `text` for full-text search.
     * The nested `keyword` sub-field allows exact-match / aggregation on title.
     */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "standard"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
        }
    )
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Float)
    private Double price;

    @Field(type = FieldType.Float, name = "discount_percentage")
    private Double discountPercentage;

    @Field(type = FieldType.Float)
    private Double rating;

    @Field(type = FieldType.Integer)
    private Integer stock;

    /**
     * `keyword` — not tokenised, so category filters are exact-match terms.
     * This is intentional: "smartphones" should not partially match "smart".
     */
    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Keyword)
    private String category;

    /** Not indexed — only stored for retrieval, never searched. */
    @Field(type = FieldType.Keyword, index = false)
    private String thumbnail;

    @Field(type = FieldType.Keyword, index = false)
    private List<String> images;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Date, name = "created_at",
           format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;
}
