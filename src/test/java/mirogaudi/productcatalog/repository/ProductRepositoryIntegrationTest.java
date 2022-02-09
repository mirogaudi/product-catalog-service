package mirogaudi.productcatalog.repository;

import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.domain.Product;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.math.BigDecimal.ONE;
import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void entityIsInitialized() {
        assertTrue(productRepository.findById(1L).isPresent());
    }

    @Test
    void categoryEagerLoadEnabled() {
        Optional<Product> productOptional = productRepository.findById(1L);
        assertTrue(productOptional.isPresent());

        Product product = productOptional.get();
        product.getCategory().forEach(category ->
                assertFalse(HibernateProxy.class.isAssignableFrom(category.getClass()))
        );
    }

    @Test
    void equalsOnlyById() {
        long id = 1L;

        Optional<Product> productOptional = productRepository.findById(id);
        assertTrue(productOptional.isPresent());

        Product product = productOptional.get();
        assertNotNull(product.getName());
        List<Category> categories = product.getCategory();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());

        Product otherProduct = new Product();
        otherProduct.setId(id);

        assertEquals(product, otherProduct);
    }

    @Test
    void hashCodeConsistent() {
        Optional<Product> productOptional = productRepository.findById(1L);
        assertTrue(productOptional.isPresent());

        Product product = productOptional.get();
        product.setId(100L);
        product.setName(product.getName() + "'");
        product.setOriginalPrice(ONE);
        product.setPrice(ONE);
        product.setOriginalCurrency(EUR.getCurrencyCode());
        product.setCurrency(EUR.getCurrencyCode());

        Optional<Category> categoryOptional = categoryRepository.findById(1L);
        categoryOptional.ifPresent(category -> product.setCategory(List.of(category)));

        Set<Product> set = Set.of(product);

        productRepository.save(product);

        assertTrue(set.contains(product));
    }

}