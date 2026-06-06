package hr.algebra.iis.controller;

import hr.algebra.iis.dto.ProductDto;
import hr.algebra.iis.exception.ValidationException;
import hr.algebra.iis.service.ProductService;
import hr.algebra.iis.service.ValidationService;
import hr.algebra.iis.service.XmlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST kontroler za WooCommerce Products API.
 *
 * KORAK 1: POST /api/products/import  → prima XML i JSON, validira, sprema
 * KORAK 5: GET/POST/PUT/DELETE /api/products → CRUD s JWT zaštitom
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ValidationService validationService;
    private final XmlService xmlService;

    /**
     * GET /api/products
     * Dohvaća sve proizvode (READ_ONLY i FULL_ACCESS)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('READ_ONLY', 'FULL_ACCESS')")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        log.info("GET /api/products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * GET /api/products/{id}
     * Dohvaća jedan proizvod po ID-u
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('READ_ONLY', 'FULL_ACCESS')")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * POST /api/products
     * Kreira novi proizvod (samo FULL_ACCESS)
     */
    @PostMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto dto) {
        log.info("POST /api/products - naziv: {}", dto.getName());
        ProductDto created = productService.saveProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * KORAK 1: POST /api/products/import
     * Prima multipart s XML i JSON datotekama, validira prema shemama i sprema.
     * Ovo je ključni endpoint koji demonstrira XSD i JSON Schema validaciju.
     */
    @PostMapping(value = "/import",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public ResponseEntity<?> importProduct(
            @RequestPart("xml") String xmlContent,
            @RequestPart("json") String jsonContent) {

        log.info("POST /api/products/import - početak validacije");

        try {
            ProductDto created = productService.createProductWithValidation(xmlContent, jsonContent);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Proizvod uspješno validiran i kreiran",
                "product", created
            ));
        } catch (ValidationException e) {
            log.warn("Validacija neuspješna: {}", e.getErrors());
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "errors", e.getErrors()
            ));
        }
    }

    /**
     * KORAK 1: POST /api/products/validate
     * Samo validira XML i JSON bez spremanja — korisno za debugging
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateOnly(
            @RequestParam(required = false) String xml,
            @RequestParam(required = false) String json) {

        var response = new java.util.LinkedHashMap<String, Object>();

        if (xml != null) {
            List<String> xmlErrors = validationService.validateXml(xml);
            response.put("xmlValid", xmlErrors.isEmpty());
            response.put("xmlErrors", xmlErrors);
        }

        if (json != null) {
            List<String> jsonErrors = validationService.validateJson(json);
            response.put("jsonValid", jsonErrors.isEmpty());
            response.put("jsonErrors", jsonErrors);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/products/{id}
     * Ažurira proizvod (samo FULL_ACCESS)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto dto) {
        log.info("PUT /api/products/{}", id);
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    /**
     * DELETE /api/products/{id}
     * Briše proizvod (samo FULL_ACCESS)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/products/generate-xml
     * KORAK 2: Generira XML datoteku iz svih proizvoda (za SOAP endpoint)
     */
    @PostMapping("/generate-xml")
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public ResponseEntity<Map<String, Object>> generateXml() {
        log.info("Generiranje XML datoteke za SOAP...");
        List<ProductDto> products = productService.getAllProducts();
        String xml = xmlService.generateProductsXml(products);
        return ResponseEntity.ok(Map.of(
            "message", "XML datoteka generirana",
            "productCount", products.size(),
            "previewLength", xml.length()
        ));
    }

    /**
     * GET /api/products/validate-xml
     * KORAK 3: Jakarta XML (JAXB) validacija generirane XML datoteke
     */
    @GetMapping("/validate-xml")
    @PreAuthorize("hasAnyRole('READ_ONLY', 'FULL_ACCESS')")
    public ResponseEntity<Map<String, Object>> validateGeneratedXml() {
        log.info("Korak 3: Jakarta XML validacija");
        try {
            String xmlContent = xmlService.readXmlFile();
            List<String> errors = validationService.validateXml(xmlContent);
            return ResponseEntity.ok(Map.of(
                "valid", errors.isEmpty(),
                "errors", errors,
                "message", errors.isEmpty()
                    ? "XML datoteka je valjana prema XSD shemi"
                    : "XML datoteka sadrži greške"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "errors", List.of("Greška: " + e.getMessage()),
                "message", "Greška pri čitanju XML datoteke"
            ));
        }
    }
}
