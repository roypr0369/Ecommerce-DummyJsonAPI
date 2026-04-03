package com.ecommerce.mapper;

import com.ecommerce.document.ProductDocument;
import com.ecommerce.dto.DummyProduct;
import com.ecommerce.dto.DummyReview;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.dto.ReviewResponse;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.ProductReview;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public Product toEntity(DummyProduct dp, Category category) {
        Product product = new Product();
        product.setId(dp.getId());
        product.setTitle(dp.getTitle());
        product.setDescription(dp.getDescription());
        product.setPrice(toBigDecimal(dp.getPrice()));
        product.setDiscountPercentage(toBigDecimal(dp.getDiscountPercentage()));
        product.setRating(toBigDecimal(dp.getRating()));
        product.setStock(dp.getStock() != null ? dp.getStock() : 0);
        product.setBrand(dp.getBrand());
        product.setThumbnail(dp.getThumbnail());
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setSku(dp.getSku());
        product.setWeight(dp.getWeight());

        if (dp.getDimensions() != null) {
            product.setDimWidth(dp.getDimensions().getWidth());
            product.setDimHeight(dp.getDimensions().getHeight());
            product.setDimDepth(dp.getDimensions().getDepth());
        }

        product.setWarrantyInformation(dp.getWarrantyInformation());
        product.setShippingInformation(dp.getShippingInformation());
        product.setAvailabilityStatus(dp.getAvailabilityStatus());
        product.setReturnPolicy(dp.getReturnPolicy());
        product.setMinimumOrderQuantity(dp.getMinimumOrderQuantity());

        if (dp.getMeta() != null) {
            product.setBarcode(dp.getMeta().getBarcode());
        }

        if (dp.getReviews() != null) {
            for (DummyReview r : dp.getReviews()) {
                product.getReviews().add(
                        new ProductReview(r.getRating(), r.getComment(),
                                r.getDate(), r.getReviewerName(),
                                r.getReviewerEmail(), product)
                );
            }
        }

        safeList(dp.getImages()).forEach(product::addImage);
        safeList(dp.getTags()).forEach(product::addTag);

        return product;
    }

    public ProductDocument toDocument(DummyProduct dp) {
        ProductDocument doc = new ProductDocument();
        doc.setId(dp.getId());
        doc.setTitle(dp.getTitle());
        doc.setDescription(dp.getDescription());
        doc.setPrice(dp.getPrice());
        doc.setDiscountPercentage(dp.getDiscountPercentage());
        doc.setRating(dp.getRating());
        doc.setStock(dp.getStock());
        doc.setBrand(dp.getBrand());
        doc.setCategory(dp.getCategory());
        doc.setThumbnail(dp.getThumbnail());
        doc.setImages(safeList(dp.getImages()));
        doc.setTags(safeList(dp.getTags()));
        doc.setCreatedAt(LocalDateTime.now());
        return doc;
    }

    public ProductResponse toResponse(Product p) {
        ProductResponse r = new ProductResponse();
        r.setId(p.getId());
        r.setTitle(p.getTitle());
        r.setDescription(p.getDescription());
        r.setPrice(toDouble(p.getPrice()));
        r.setDiscountPercentage(toDouble(p.getDiscountPercentage()));
        r.setRating(toDouble(p.getRating()));
        r.setStock(p.getStock());
        r.setBrand(p.getBrand());
        r.setThumbnail(p.getThumbnail());
        r.setCategory(p.getCategory() != null ? p.getCategory().getName() : null);
        r.setImages(p.getImages().stream()
                .map(img -> img.getUrl())
                .collect(Collectors.toList()));
        r.setTags(p.getTags().stream()
                .map(tag -> tag.getTag())
                .collect(Collectors.toList()));
        r.setCreatedAt(p.getCreatedAt());
        r.setSku(p.getSku());
        r.setWeight(p.getWeight());
        r.setDimWidth(p.getDimWidth());
        r.setDimHeight(p.getDimHeight());
        r.setDimDepth(p.getDimDepth());
        r.setWarrantyInformation(p.getWarrantyInformation());
        r.setShippingInformation(p.getShippingInformation());
        r.setAvailabilityStatus(p.getAvailabilityStatus());
        r.setReturnPolicy(p.getReturnPolicy());
        r.setMinimumOrderQuantity(p.getMinimumOrderQuantity());
        r.setBarcode(p.getBarcode());
        r.setReviews(p.getReviews().stream()
                .map(rv -> new ReviewResponse(rv.getRating(), rv.getComment(),
                        rv.getReviewDate(), rv.getReviewerName()))
                .collect(Collectors.toList()));
        return r;
    }

    public ProductResponse toResponse(ProductDocument doc) {
        ProductResponse r = new ProductResponse();
        r.setId(doc.getId());
        r.setTitle(doc.getTitle());
        r.setDescription(doc.getDescription());
        r.setPrice(doc.getPrice());
        r.setDiscountPercentage(doc.getDiscountPercentage());
        r.setRating(doc.getRating());
        r.setStock(doc.getStock());
        r.setBrand(doc.getBrand());
        r.setThumbnail(doc.getThumbnail());
        r.setCategory(doc.getCategory());
        r.setImages(safeList(doc.getImages()));
        r.setTags(safeList(doc.getTags()));
        r.setCreatedAt(doc.getCreatedAt());
        return r;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }

    private Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }
}
