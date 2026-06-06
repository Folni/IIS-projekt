package hr.algebra.iis.graphql;

import hr.algebra.iis.dto.ProductDto;
import hr.algebra.iis.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * KORAK 5: GraphQL resolver za proizvode.
 * Koristi iste servise kao i REST API.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ProductGraphQlResolver {

    private final ProductService productService;

    // =================== Query ===================

    @QueryMapping
    @PreAuthorize("hasAnyRole('READ_ONLY', 'FULL_ACCESS')")
    public List<ProductDto> products(@Argument Integer page,
                                     @Argument Integer pageSize) {
        log.info("GraphQL query: products(page={}, pageSize={})", page, pageSize);
        return productService.getAllProducts();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('READ_ONLY', 'FULL_ACCESS')")
    public ProductDto product(@Argument String id) {
        log.info("GraphQL query: product(id={})", id);
        return productService.getProductById(Long.parseLong(id));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('READ_ONLY', 'FULL_ACCESS')")
    public List<ProductDto> searchProducts(@Argument String name) {
        log.info("GraphQL query: searchProducts(name={})", name);
        return productService.getAllProducts().stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    // =================== Mutation ===================

    @MutationMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public ProductDto createProduct(@Argument ProductDto input) {
        log.info("GraphQL mutation: createProduct({})", input.getName());
        return productService.saveProduct(input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public ProductDto updateProduct(@Argument String id,
                                    @Argument ProductDto input) {
        log.info("GraphQL mutation: updateProduct(id={})", id);
        return productService.updateProduct(Long.parseLong(id), input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public Boolean deleteProduct(@Argument String id) {
        log.info("GraphQL mutation: deleteProduct(id={})", id);
        productService.deleteProduct(Long.parseLong(id));
        return true;
    }
}
