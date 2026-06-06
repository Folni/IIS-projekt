package hr.algebra.iis.service;

import hr.algebra.iis.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.List;

/**
 * Klijent za komunikaciju s javnim WooCommerce REST API-jem.
 * Koristi se kad je app.api.mode=public
 */
@Slf4j
@Service
public class WooCommerceApiClient {

    @Value("${app.woocommerce.base-url}")
    private String baseUrl;

    @Value("${app.woocommerce.consumer-key}")
    private String consumerKey;

    @Value("${app.woocommerce.consumer-secret}")
    private String consumerSecret;

    /**
     * Dohvaća sve proizvode s javnog WooCommerce API-ja (GET)
     */
    public List<ProductDto> getAllProducts() {
        log.info("Dohvaćam proizvode s WooCommerce API-ja: {}", baseUrl);
        try {
            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", buildAuthHeader())
                    .build()
                    .get()
                    .uri("/products")
                    .retrieve()
                    .bodyToFlux(ProductDto.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Greška pri dohvaćanju s WooCommerce API-ja", e);
            throw new RuntimeException("Ne mogu se spojiti na WooCommerce API: " + e.getMessage());
        }
    }

    /**
     * Dohvaća jedan proizvod s javnog API-ja (GET by ID)
     */
    public ProductDto getProductById(Long id) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", buildAuthHeader())
                .build()
                .get()
                .uri("/products/" + id)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .block();
    }

    /**
     * Kreira proizvod na javnom WooCommerce API-ju (POST)
     */
    public ProductDto createProduct(ProductDto product) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", buildAuthHeader())
                .build()
                .post()
                .uri("/products")
                .bodyValue(product)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .block();
    }

    /**
     * Ažurira proizvod na javnom API-ju (PUT)
     */
    public ProductDto updateProduct(Long id, ProductDto product) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", buildAuthHeader())
                .build()
                .put()
                .uri("/products/" + id)
                .bodyValue(product)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .block();
    }

    /**
     * Briše proizvod s javnog API-ja (DELETE)
     */
    public void deleteProduct(Long id) {
        WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", buildAuthHeader())
                .build()
                .delete()
                .uri("/products/" + id)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    // WooCommerce koristi Basic Auth s consumer key i secret
    private String buildAuthHeader() {
        String credentials = consumerKey + ":" + consumerSecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
