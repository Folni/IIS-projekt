package hr.algebra.iis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.iis.dto.ProductDto;
import hr.algebra.iis.entity.Product;
import hr.algebra.iis.exception.ProductNotFoundException;
import hr.algebra.iis.exception.ValidationException;
import hr.algebra.iis.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servis za upravljanje proizvodima.
 * Implementira PREKIDAČ između javnog WooCommerce API-ja i vlastitog backenda.
 * Korak 5: app.api.mode=public → WooCommerce, app.api.mode=custom → naša baza
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    // PREKIDAČ - postavlja se u application.properties
    @Value("${app.api.mode:public}")
    private String apiMode;

    private final ProductRepository productRepository;
    private final WooCommerceApiClient wooCommerceApiClient;
    private final ValidationService validationService;
    private final XmlService xmlService;
    private final ObjectMapper objectMapper;

    /**
     * GET - dohvati sve proizvode
     * Prekidač odlučuje: javni API ili naša baza
     */
    public List<ProductDto> getAllProducts() {
        if ("custom".equalsIgnoreCase(apiMode)) {
            log.info("API mode: custom - dohvaćam iz baze");
            return productRepository.findAll().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else {
            log.info("API mode: public - dohvaćam s WooCommerce API-ja");
            return wooCommerceApiClient.getAllProducts();
        }
    }

    /**
     * GET - dohvati jedan proizvod
     */
    public ProductDto getProductById(Long id) {
        if ("custom".equalsIgnoreCase(apiMode)) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException("Proizvod s ID " + id + " nije pronađen"));
            return toDto(product);
        } else {
            return wooCommerceApiClient.getProductById(id);
        }
    }

    /**
     * KORAK 1: POST - kreiraj novi proizvod uz XML i JSON validaciju
     * Validira XML i JSON datoteku, sprema u bazu ako je valjano
     */
    @Transactional
    public ProductDto createProductWithValidation(String xmlContent, String jsonContent)
            throws ValidationException {

        // 1. Validiraj XML prema XSD shemi
        List<String> xmlErrors = validationService.validateXml(xmlContent);
        if (!xmlErrors.isEmpty()) {
            throw new ValidationException("XML validacija neuspješna", xmlErrors);
        }
        log.info("XML validacija uspješna");

        // 2. Validiraj JSON prema JSON shemi
        List<String> jsonErrors = validationService.validateJson(jsonContent);
        if (!jsonErrors.isEmpty()) {
            throw new ValidationException("JSON validacija neuspješna", jsonErrors);
        }
        log.info("JSON validacija uspješna");

        // 3. Parsiraj JSON i spremi u bazu
        try {
            ProductDto dto = objectMapper.readValue(jsonContent, ProductDto.class);
            return saveProduct(dto);
        } catch (Exception e) {
            throw new ValidationException("Greška pri parsiranju podataka: " + e.getMessage(), List.of());
        }
    }

    /**
     * PUT - ažuriraj proizvod
     */
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        if ("custom".equalsIgnoreCase(apiMode)) {
            Product existing = productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException("Proizvod s ID " + id + " nije pronađen"));
            updateEntityFromDto(existing, dto);
            return toDto(productRepository.save(existing));
        } else {
            return wooCommerceApiClient.updateProduct(id, dto);
        }
    }

    /**
     * DELETE - obriši proizvod
     */
    @Transactional
    public void deleteProduct(Long id) {
        if ("custom".equalsIgnoreCase(apiMode)) {
            if (!productRepository.existsById(id)) {
                throw new ProductNotFoundException("Proizvod s ID " + id + " nije pronađen");
            }
            productRepository.deleteById(id);
        } else {
            wooCommerceApiClient.deleteProduct(id);
        }
    }

    /**
     * Sprema DTO u bazu kao Product entitet
     */
    @Transactional
    public ProductDto saveProduct(ProductDto dto) {
        Product product = toEntity(dto);
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    // =================== Mapper metode ===================

    public ProductDto toDto(Product p) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return ProductDto.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .type(p.getType() != null ? p.getType().name() : null)
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .description(p.getDescription())
                .shortDescription(p.getShortDescription())
                .sku(p.getSku())
                .price(p.getPrice() != null ? p.getPrice().toPlainString() : "0")
                .regularPrice(p.getRegularPrice() != null ? p.getRegularPrice().toPlainString() : null)
                .salePrice(p.getSalePrice() != null ? p.getSalePrice().toPlainString() : null)
                .stockQuantity(p.getStockQuantity())
                .stockStatus(p.getStockStatus() != null ? p.getStockStatus().name() : null)
                .weight(p.getWeight())
                .dateCreated(p.getDateCreated() != null ? p.getDateCreated().format(fmt) : null)
                .dateModified(p.getDateModified() != null ? p.getDateModified().format(fmt) : null)
                .build();
    }

    private Product toEntity(ProductDto dto) {
        return Product.builder()
                .name(dto.getName())
                .slug(dto.getSlug())
                .type(parseEnum(Product.ProductType.class, dto.getType(), Product.ProductType.simple))
                .status(parseEnum(Product.ProductStatus.class, dto.getStatus(), Product.ProductStatus.publish))
                .description(dto.getDescription())
                .shortDescription(dto.getShortDescription())
                .sku(dto.getSku())
                .price(dto.getPrice() != null ? new BigDecimal(dto.getPrice()) : BigDecimal.ZERO)
                .regularPrice(dto.getRegularPrice() != null ? new BigDecimal(dto.getRegularPrice()) : null)
                .salePrice(dto.getSalePrice() != null && !dto.getSalePrice().isEmpty()
                        ? new BigDecimal(dto.getSalePrice()) : null)
                .stockQuantity(dto.getStockQuantity())
                .stockStatus(parseEnum(Product.StockStatus.class, dto.getStockStatus(), Product.StockStatus.instock))
                .weight(dto.getWeight())
                .build();
    }

    private void updateEntityFromDto(Product entity, ProductDto dto) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getPrice() != null) entity.setPrice(new BigDecimal(dto.getPrice()));
        if (dto.getStockQuantity() != null) entity.setStockQuantity(dto.getStockQuantity());
        if (dto.getStatus() != null)
            entity.setStatus(parseEnum(Product.ProductStatus.class, dto.getStatus(), entity.getStatus()));
    }

    private <T extends Enum<T>> T parseEnum(Class<T> cls, String value, T defaultVal) {
        if (value == null || value.isBlank()) return defaultVal;
        try {
            return Enum.valueOf(cls, value);
        } catch (IllegalArgumentException e) {
            return defaultVal;
        }
    }
}
