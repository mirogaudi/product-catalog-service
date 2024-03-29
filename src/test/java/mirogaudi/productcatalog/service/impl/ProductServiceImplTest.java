package mirogaudi.productcatalog.service.impl;

import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.domain.Product;
import mirogaudi.productcatalog.repository.ProductRepository;
import mirogaudi.productcatalog.service.CategoryService;
import mirogaudi.productcatalog.service.CurrencyExchangeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.productcatalog.testhelper.Currencies.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private Supplier<Currency> baseCurrency;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CurrencyExchangeService currencyExchangeService;

    @InjectMocks
    private ProductServiceImpl sut;

    @Test
    void findAll() {
        when(productRepository.findAll()).thenReturn(List.of());

        var products = sut.findAll();

        verify(productRepository).findAll();
        assertTrue(products.isEmpty());
    }

    @Test
    void find() {
        Long id = 1L;
        Product expectedProduct = product();
        when(productRepository.findById(id)).thenReturn(Optional.of(expectedProduct));

        Product product = sut.find(id);

        verify(productRepository).findById(id);
        assertEquals(expectedProduct, product);
    }

    @ParameterizedTest
    @NullSource
    void find_null_id(Long id) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.find(id));
    }

    @Test
    void create() {
        when(baseCurrency.get()).thenReturn(EUR);

        Long categoryId = 2L;
        when(categoryService.find(categoryId)).thenReturn(category());

        BigDecimal originalPrice = TEN;
        when(currencyExchangeService.convert(originalPrice, USD, EUR)).thenReturn(Mono.fromCallable(() -> ONE));

        Product expectedProduct = product();
        when(productRepository.save(any())).thenReturn(expectedProduct);

        Product createdProduct = sut.create("name", originalPrice, USD, Set.of(categoryId));

        verify(currencyExchangeService).convert(originalPrice, USD, EUR);
        verify(productRepository).save(any());
        assertEquals(expectedProduct, createdProduct);
    }

    @Test
    void create_null_converted_price() {
        when(baseCurrency.get()).thenReturn(EUR);

        BigDecimal originalPrice = TEN;
        when(currencyExchangeService.convert(originalPrice, USD, EUR)).thenReturn(Mono.fromCallable(() -> null));

        Set<Long> categories = Set.of();

        assertThrows(IllegalStateException.class,
            () -> sut.create("name", originalPrice, USD, categories));

        verify(currencyExchangeService).convert(originalPrice, USD, EUR);
        verify(productRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    void create_null_name(String name) {
        var categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.create(name, TEN, USD, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void create_null_originalPrice(BigDecimal originalPrice) {
        var categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.create("name", originalPrice, USD, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void create_null_originalCurrency(Currency originalCurrency) {
        var categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.create("name", TEN, originalCurrency, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void create_null_categoryIds(Set<Long> categoryIds) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.create("name", TEN, USD, categoryIds));
    }

    @Test
    void create_invalid_categoryId() {
        Long categoryId = 1L;
        when(categoryService.find(categoryId)).thenReturn(null);

        var categoryIds = Set.of(categoryId);

        assertThrows(IllegalStateException.class,
            () -> sut.create("name", ONE, EUR, categoryIds));
    }

    @Test
    void update() {
        when(baseCurrency.get()).thenReturn(EUR);

        Long id = 1L;
        Product product = product();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Long categoryId = 2L;
        when(categoryService.find(categoryId)).thenReturn(category());

        BigDecimal originalPrice = TEN;
        when(currencyExchangeService.convert(originalPrice, EUR, EUR)).thenReturn(Mono.fromCallable(() -> TEN));

        Product expectedProduct = product();
        when(productRepository.save(any())).thenReturn(expectedProduct);

        Product updatedProduct = sut.update(id, "name", originalPrice, EUR, Set.of(categoryId));

        verify(currencyExchangeService).convert(originalPrice, EUR, EUR);
        verify(productRepository).save(any());
        assertEquals(expectedProduct, updatedProduct);
    }

    @Test
    void update_null_converted_price() {
        when(baseCurrency.get()).thenReturn(EUR);

        Long id = 1L;
        Product product = product();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        BigDecimal originalPrice = TEN;
        when(currencyExchangeService.convert(originalPrice, USD, EUR)).thenReturn(Mono.fromCallable(() -> null));

        Set<Long> categories = Set.of();

        assertThrows(IllegalStateException.class,
            () -> sut.update(id, "name", originalPrice, USD, categories));

        verify(currencyExchangeService).convert(originalPrice, USD, EUR);
        verify(productRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    void update_null_id(Long id) {
        var categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.update(id, "name", TEN, USD, categoryIds));
    }

    @Test
    void update_invalid_id() {
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        var categoryIds = Set.of(1L);

        assertThrows(IllegalStateException.class,
            () -> sut.update(id, "name", ONE, EUR, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void update_null_name(String name) {
        var categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.update(1L, name, TEN, USD, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void update_null_originalPrice(BigDecimal originalPrice) {
        var categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.update(1L, "name", originalPrice, USD, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void update_null_originalCurrency(Currency originalCurrency) {
        var categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.update(1L, "name", TEN, originalCurrency, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void update_null_categoryIds(Set<Long> categoryIds) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.update(1L, "name", TEN, USD, categoryIds));
    }

    @Test
    void update_invalid_categoryId() {
        Long id = 1L;
        Product product = product();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Long categoryId = 2L;
        when(categoryService.find(categoryId)).thenReturn(null);

        var categoryId1 = Set.of(categoryId);

        assertThrows(IllegalStateException.class,
            () -> sut.update(id, "name", ONE, EUR, categoryId1));
    }

    @Test
    void delete() {
        Long id = 1L;
        when(productRepository.existsById(id)).thenReturn(true);

        sut.delete(id);

        verify(productRepository).deleteById(id);
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
        when(productRepository.existsById(id)).thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> sut.delete(id));
    }

    private Category category() {
        return new Category();
    }

    private Product product() {
        return new Product();
    }

}
