package mirogaudi.productcatalog.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.domain.Product;
import mirogaudi.productcatalog.repository.ProductRepository;
import mirogaudi.productcatalog.service.CategoryService;
import mirogaudi.productcatalog.service.CurrencyExchangeService;
import mirogaudi.productcatalog.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final Supplier<Currency> baseCurrency;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final CurrencyExchangeService currencyExchangeService;

    @Override
    public List<Product> findAll() {
        return productRepository.findAllWithCategories();
    }

    @Override
    public Product find(@NonNull Long id) {
        return productRepository.findByIdWithCategories(id).orElse(null);
    }

    @Override
    public Product create(@NonNull String name,
                          @NonNull BigDecimal originalPrice,
                          @NonNull Currency originalCurrency,
                          @NonNull Set<Long> categoryIds) {
        Assert.isTrue(originalPrice.signum() > 0, "Price must be positive");

        Product product = new Product();

        return save(
            product,
            name,
            originalPrice, originalCurrency,
            categoryIds
        );
    }

    @Override
    public Product update(@NonNull Long id,
                          @NonNull String name,
                          @NonNull BigDecimal originalPrice,
                          @NonNull Currency originalCurrency,
                          @NonNull Set<Long> categoryIds) {
        Assert.isTrue(originalPrice.signum() > 0, "Price must be positive");

        Product product = find(id);
        Assert.state(product != null, String.format(
            "Product with id '%d' not found", id));

        return save(
            product,
            name,
            originalPrice, originalCurrency,
            categoryIds
        );
    }

    private Product save(Product product,
                         String name,
                         BigDecimal originalPrice,
                         Currency originalCurrency,
                         Set<Long> categoryIds) {
        product.setName(name);

        Assert.isTrue(!categoryIds.isEmpty(), "At least one category id is required");

        product.setCategories(findCategories(categoryIds));

        product.setOriginalPrice(originalPrice);
        product.setOriginalCurrency(originalCurrency.getCurrencyCode());

        Currency currency = baseCurrency.get();

        BigDecimal price = currencyExchangeService.convert(originalPrice, originalCurrency, currency);
        Assert.state(price != null, String.format(
            "Failed to convert original price '%s' from '%s' to '%s'",
            originalPrice, originalCurrency, currency));

        product.setPrice(price);
        product.setCurrency(currency.getCurrencyCode());

        return productRepository.save(product);
    }

    private List<Category> findCategories(Set<Long> categoryIds) {
        List<Category> categories = categoryService.findAllById(categoryIds);

        Assert.state(categories.size() == categoryIds.size(), String.format(
            "Not all categories were found. Expected: %s, found: %s",
            categoryIds.stream().sorted().toList(),
            categories.stream().map(Category::getId).sorted().toList()));

        return categories;
    }

    @Override
    public void delete(@NonNull Long id) {
        // in real project consider to use getReferenceById(id)!
        Assert.state(productRepository.existsById(id), String.format(
            "Product with id '%d' does not exist", id));

        productRepository.deleteById(id);
    }

}
