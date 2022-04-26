package mirogaudi.productcatalog.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.domain.Product;
import mirogaudi.productcatalog.repository.ProductRepository;
import mirogaudi.productcatalog.service.CategoryService;
import mirogaudi.productcatalog.service.CurrencyExchangeService;
import mirogaudi.productcatalog.service.ProductService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class ProductServiceImpl implements ProductService {

    private final Supplier<Currency> baseCurrency;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final CurrencyExchangeService currencyExchangeService;

    @Override
    public List<Product> findAll() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
            .toList();
    }

    @Override
    public Product find(@NonNull Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product create(@NonNull String name,
                          @NonNull BigDecimal originalPrice,
                          @NonNull Currency originalCurrency,
                          @NonNull Set<Long> categoryIds) {
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
        product.setCategories(findCategories(categoryIds));

        product.setOriginalPrice(originalPrice);
        product.setOriginalCurrency(originalCurrency.getCurrencyCode());

        Currency currency = baseCurrency.get();

        BigDecimal price = currencyExchangeService.convert(originalPrice, originalCurrency, currency).block();
        Assert.state(price != null, String.format(
            "No price got converting original price '%s' from '%s' to '%s'", originalPrice, originalCurrency, currency));

        product.setPrice(price);
        product.setCurrency(currency.getCurrencyCode());

        return productRepository.save(product);
    }

    private List<Category> findCategories(Set<Long> categoryIds) {
        return categoryIds.stream()
            .map(id -> {
                Category category = categoryService.find(id);
                Assert.state(category != null, String.format(
                    "Category with id '%d' not found", id));
                return category;
            })
            .toList();
    }

    @Override
    public void delete(@NonNull Long id) {
        Assert.state(productRepository.existsById(id), String.format(
            "Product with id '%d' does not exist", id));

        productRepository.deleteById(id);
    }

}
