package mirogaudi.productcatalog.repository;

import mirogaudi.productcatalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categories")
    List<Product> findAllWithCategories();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categories WHERE p.id = :id")
    Optional<Product> findByIdWithCategories(Long id);

    /**
     * Check if a product linked to category with given category ID exists.
     * Property path: {@code categories.id}
     *
     * @param categoryId the category ID
     * @return true if a product linked to category exists
     */
    boolean existsByCategories_Id(Long categoryId);

}
