package mirogaudi.productcatalog.service;

import mirogaudi.productcatalog.domain.Product;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;

/**
 * Service for CRUD operations with a product.
 */
public interface ProductService {

    /**
     * Finds all products.
     *
     * @return a list of products
     */
    List<Product> findAll();

    /**
     * Finds a product.
     *
     * @param id product id
     * @return a product
     */
    Product find(Long id);

    /**
     * Creates a product.
     *
     * @param name             product name
     * @param originalPrice    product original price
     * @param originalCurrency product original currency
     * @param categoryIds      a set of ids of categories
     * @return created product
     */
    Product create(String name,
                   BigDecimal originalPrice,
                   Currency originalCurrency,
                   Set<Long> categoryIds);

    /**
     * Updates a product.
     *
     * @param id               product id
     * @param name             product name
     * @param originalPrice    product original price
     * @param originalCurrency product original currency
     * @param categoryIds      a set of ids of categories
     * @return updated product
     */
    Product update(Long id,
                   String name,
                   BigDecimal originalPrice,
                   Currency originalCurrency,
                   Set<Long> categoryIds);

    /**
     * Deletes a product.
     *
     * @param id product id
     */
    void delete(Long id);

}
