package hr.algebra.iis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO za WooCommerce produkt - koristi se u REST i GraphQL zahtjevima.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;

    @NotBlank(message = "Naziv proizvoda je obavezan")
    private String name;

    private String slug;
    private String type;
    private String status;
    private String description;
    private String shortDescription;
    private String sku;

    @NotNull(message = "Cijena je obavezna")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]{1,2})?$",
             message = "Cijena mora biti broj u formatu '29.99'")
    private String price;

    private String regularPrice;
    private String salePrice;
    private Integer stockQuantity;
    private String stockStatus;
    private String weight;
    private List<CategoryDto> categories;
    private List<ImageDto> images;
    private String dateCreated;
    private String dateModified;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private Integer id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDto {
        private Integer id;
        private String src;
        private String alt;
    }
}
