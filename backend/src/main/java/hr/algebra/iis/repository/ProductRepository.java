package hr.algebra.iis.repository;

import hr.algebra.iis.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByStatus(Product.ProductStatus status);

    List<Product> findByFromExternalApi(Boolean fromExternalApi);
}