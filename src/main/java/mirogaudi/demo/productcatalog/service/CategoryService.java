package mirogaudi.demo.productcatalog.service;

import mirogaudi.demo.productcatalog.domain.Category;

import java.util.List;

/**
 * Service for CRUD operations with a category.
 */
public interface CategoryService {

    /**
     * Finds all categories.
     *
     * @return a list of categories
     */
    List<Category> findAll();

    /**
     * Finds a category.
     *
     * @param id category id
     * @return a category
     */
    Category find(Long id);

    /**
     * Creates a category.
     *
     * @param name     category name
     * @param parentId parent category id, can be null
     * @return created category
     */
    Category create(String name,
                    Long parentId);

    /**
     * Updates a category.
     *
     * @param id       category id
     * @param name     category name
     * @param parentId parent category id, can be null
     * @return updated category
     */
    Category update(Long id, String name,
                    Long parentId);

    /**
     * Deletes a category.
     *
     * @param id category id
     */
    void delete(Long id);

}
