package hr.algebra.iis.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entitet koji predstavlja WooCommerce proizvod.
 * Pohranjen u PostgreSQL bazi podataka.
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Naziv proizvoda je obavezan")
    @Column(nullable = false, length = 500)
    private String name;

    @Column(unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductType type = ProductType.simple;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductStatus status = ProductStatus.publish;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(unique = true)
    private String sku;

    @NotNull(message = "Cijena je obavezna")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "regular_price", precision = 10, scale = 2)
    private BigDecimal regularPrice;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status")
    @Builder.Default
    private StockStatus stockStatus = StockStatus.instock;

    private String weight;

    // Kategorije kao JSON string (jednostavnost za projekt)
    @Column(name = "categories_json", columnDefinition = "TEXT")
    private String categoriesJson;

    @Column(name = "date_created")
    @Builder.Default
    private LocalDateTime dateCreated = LocalDateTime.now();

    @Column(name = "date_modified")
    @Builder.Default
    private LocalDateTime dateModified = LocalDateTime.now();

    // Oznaka: je li proizvod dohvaćen s javnog WooCommerce API-ja
    @Column(name = "from_external_api")
    @Builder.Default
    private Boolean fromExternalApi = false;

    @PreUpdate
    public void preUpdate() {
        this.dateModified = LocalDateTime.now();
    }

    // Enumeracije - moraju odgovarati XSD i JSON shemi
    public enum ProductType {
        simple, variable, grouped, external
    }

    public enum ProductStatus {
        publish, draft, pending, private_status;

        @Override
        public String toString() {
            return this.name().replace("_status", "");
        }
    }

    public enum StockStatus {
        instock, outofstock, onbackorder
    }
}
