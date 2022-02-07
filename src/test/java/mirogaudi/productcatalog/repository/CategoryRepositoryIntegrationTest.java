package mirogaudi.productcatalog.repository;

import mirogaudi.productcatalog.domain.Category;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class CategoryRepositoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void entityIsInitialized() {
        Optional<Category> topCategory = categoryRepository.findById(1L);

        assertTrue(topCategory.isPresent());
    }

    @Test
    void parentLazyLoadEnabled() {
        Optional<Category> subCategory = categoryRepository.findById(2L);

        assertTrue(subCategory.isPresent());
        assertTrue(HibernateProxy.class.isAssignableFrom(subCategory.get().getParent().getClass()));
    }

    @Test
    void equalsOnlyById() {
        long id = 2L;

        Optional<Category> categoryOptional = categoryRepository.findById(id);
        assertTrue(categoryOptional.isPresent());

        Category category = categoryOptional.get();
        assertNotNull(category.getName());
        assertNotNull(category.getParent());

        Category otherCategory = new Category();
        otherCategory.setId(id);

        assertEquals(category, otherCategory);
    }

    @Test
    void hashCodeConsistent() {
        Optional<Category> categoryOptional = categoryRepository.findById(2L);
        assertTrue(categoryOptional.isPresent());

        Category category = categoryOptional.get();
        category.setId(100L);
        category.setName(category.getName() + "'");
        category.setParent(null);

        Set<Category> set = Set.of(category);

        categoryRepository.save(category);

        assertTrue(set.contains(category));
    }

}