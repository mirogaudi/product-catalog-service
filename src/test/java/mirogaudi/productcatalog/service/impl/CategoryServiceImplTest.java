package mirogaudi.productcatalog.service.impl;

import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl sut;

    @Test
    void findAll() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        var categories = sut.findAll();
        assertTrue(categories.isEmpty());

        verify(categoryRepository).findAll();
    }

    @Test
    void find() {
        Long id = 1L;
        Category expectedCategory = category();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(expectedCategory));

        Category category = sut.find(id);
        assertEquals(expectedCategory, category);

        verify(categoryRepository).findById(id);
    }

    @ParameterizedTest
    @NullSource
    void find_null_id(Long id) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.find(id));
    }

    @Test
    void create() {
        Long parentId = 1L;
        Category parentCategory = category();
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));

        Category expectedCategory = category();
        when(categoryRepository.save(any())).thenReturn(expectedCategory);

        Category createdCategory = sut.create("name", parentId);
        assertEquals(expectedCategory, createdCategory);

        verify(categoryRepository).save(any());
    }

    @Test
    void create_null_parentId() {
        Category expectedCategory = category();
        when(categoryRepository.save(any())).thenReturn(expectedCategory);

        Category createdCategory = sut.create("name", null);
        assertEquals(expectedCategory, createdCategory);

        verify(categoryRepository).save(any());
    }

    @ParameterizedTest
    @NullSource
    void create_null_name(String name) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.create(name, 1L));
    }

    @Test
    void create_invalid_parentId() {
        Long parentId = 1L;
        when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
            () -> sut.create("name", parentId));
    }

    @Test
    void update() {
        Long id = 2L;
        Category category = category();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        Long parentId = 1L;
        Category parentCategory = category();
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));

        Category expectedCategory = category();
        when(categoryRepository.save(any())).thenReturn(expectedCategory);

        Category updatedCategory = sut.update(id, "name", parentId);
        assertEquals(expectedCategory, updatedCategory);

        verify(categoryRepository).save(any());
    }

    @ParameterizedTest
    @NullSource
    void update_null_id(Long id) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.update(id, "name", 1L));
    }

    @ParameterizedTest
    @NullSource
    void update_null_name(String name) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.update(1L, name, 1L));
    }

    @Test
    void update_invalid_id() {
        Long id = 1L;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
            () -> sut.update(id, "name", 1L));
    }

    @Test
    void update_invalid_parentId() {
        Long id = 2L;
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category()));

        Long parentId = 1L;
        when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
            () -> sut.update(id, "name", parentId));
    }

    @Test
    void update_invalid_parentId_self_reference() {
        Long id = 1L;
        Category category = mock(Category.class);
        when(category.getId()).thenReturn(id);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        assertThrows(IllegalArgumentException.class,
            () -> sut.update(id, "name", id));
    }

    @Test
    void delete() {
        Long id = 1L;
        when(categoryRepository.existsById(id)).thenReturn(true);

        sut.delete(id);

        verify(categoryRepository).deleteById(id);
    }

    @ParameterizedTest
    @NullSource
    void delete_null_id(Long id) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.delete(id));
    }

    @Test
    void delete_invalid_id() {
        Long id = 1L;
        when(categoryRepository.existsById(id)).thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> sut.delete(id));
    }

    private Category category() {
        return new Category();
    }

}
