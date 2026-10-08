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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
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
        List<Product> expectedProducts = List.of(product(1L), product(2L));
        when(productRepository.findAllWithCategories()).thenReturn(expectedProducts);

        List<Product> products = sut.findAll();
        assertEquals(expectedProducts, products);

        verify(productRepository).findAllWithCategories();
    }

    @Test
    void find() {
        Long id = 1L;
        Product expectedProduct = product(id);
        when(productRepository.findByIdWithCategories(id)).thenReturn(Optional.of(expectedProduct));

        Product product = sut.find(id);
        assertEquals(expectedProduct, product);

        verify(productRepository).findByIdWithCategories(id);
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
        Set<Long> categoryIds = Set.of(categoryId);
        when(categoryService.findAllById(categoryIds)).thenReturn(List.of(category(categoryId)));

        BigDecimal originalPrice = TEN;
        when(currencyExchangeService.convert(originalPrice, USD, EUR)).thenReturn(ONE);

        Product expectedProduct = product(1L);
        when(productRepository.save(any())).thenReturn(expectedProduct);

        Product createdProduct = sut.create("name", originalPrice, USD, categoryIds);
        assertEquals(expectedProduct, createdProduct);

        verify(currencyExchangeService).convert(originalPrice, USD, EUR);
        verify(productRepository).save(any());
    }

    @Test
    void create_null_converted_price() {
        when(baseCurrency.get()).thenReturn(EUR);

        BigDecimal originalPrice = TEN;
        when(currencyExchangeService.convert(originalPrice, USD, EUR)).thenReturn(null);

        Set<Long> categories = Set.of();
        when(categoryService.findAllById(categories)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class,
            () -> sut.create("name", originalPrice, USD, categories));

        verify(currencyExchangeService).convert(originalPrice, USD, EUR);
        verify(productRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    void create_null_name(String name) {
        Set<Long> categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.create(name, TEN, USD, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void create_null_originalPrice(BigDecimal originalPrice) {
        Set<Long> categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.create("name", originalPrice, USD, categoryIds));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void create_negative_or_zero_originalPrice(long value) {
        BigDecimal originalPrice = BigDecimal.valueOf(value);

        Set<Long> categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.create("name", originalPrice, USD, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void create_null_originalCurrency(Currency originalCurrency) {
        Set<Long> categoryIds = Set.of(1L);

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
    void create_invalid_categoryIds() {
        Set<Long> categoryIds = Set.of(1L, 2L, 3L, 4L);
        when(categoryService.findAllById(categoryIds)).thenReturn(List.of(category(2L), category(4L)));

        IllegalStateException e = assertThrows(IllegalStateException.class,
            () -> sut.create("name", ONE, EUR, categoryIds));
        assertEquals("Not all categories were found. Expected: [1, 2, 3, 4], found: [2, 4]", e.getMessage());
    }

    @Test
    void update() {
        when(baseCurrency.get()).thenReturn(EUR);

        Long id = 1L;
        Product product = product(id);
        when(productRepository.findByIdWithCategories(id)).thenReturn(Optional.of(product));

        Long categoryId = 2L;
        Set<Long> categoryIds = Set.of(categoryId);
        when(categoryService.findAllById(categoryIds)).thenReturn(List.of(category(categoryId)));

        BigDecimal originalPrice = TEN;
        when(currencyExchangeService.convert(originalPrice, EUR, EUR)).thenReturn(TEN);

        Product expectedProduct = product(id);
        when(productRepository.save(any())).thenReturn(expectedProduct);

        Product updatedProduct = sut.update(id, "name", originalPrice, EUR, categoryIds);
        assertEquals(expectedProduct, updatedProduct);

        verify(currencyExchangeService).convert(originalPrice, EUR, EUR);
        verify(productRepository).save(any());
    }

    @Test
    void update_null_converted_price() {
        when(baseCurrency.get()).thenReturn(EUR);

        Long id = 1L;
        Product product = product(id);
        when(productRepository.findByIdWithCategories(id)).thenReturn(Optional.of(product));

        BigDecimal originalPrice = TEN;
        when(currencyExchangeService.convert(originalPrice, USD, EUR)).thenReturn(null);

        Set<Long> categories = Set.of();
        when(categoryService.findAllById(categories)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class,
            () -> sut.update(id, "name", originalPrice, USD, categories));

        verify(currencyExchangeService).convert(originalPrice, USD, EUR);
        verify(productRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    void update_null_id(Long id) {
        Set<Long> categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.update(id, "name", TEN, USD, categoryIds));
    }

    @Test
    void update_invalid_id() {
        Long id = 1L;
        when(productRepository.findByIdWithCategories(id)).thenReturn(Optional.empty());

        Set<Long> categoryIds = Set.of(1L);

        assertThrows(IllegalStateException.class,
            () -> sut.update(id, "name", ONE, EUR, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void update_null_name(String name) {
        Set<Long> categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.update(1L, name, TEN, USD, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void update_null_originalPrice(BigDecimal originalPrice) {
        Set<Long> categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.update(1L, "name", originalPrice, USD, categoryIds));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void update_negative_or_zero_originalPrice(long value) {
        BigDecimal originalPrice = BigDecimal.valueOf(value);

        Set<Long> categoryIds = Set.of(1L);

        assertThrows(IllegalArgumentException.class,
            () -> sut.update(1L, "name", originalPrice, USD, categoryIds));
    }

    @ParameterizedTest
    @NullSource
    void update_null_originalCurrency(Currency originalCurrency) {
        Set<Long> categoryIds = Set.of(1L);

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
    void update_invalid_categoryIds() {
        Long id = 1L;
        Product product = product(id);
        when(productRepository.findByIdWithCategories(id)).thenReturn(Optional.of(product));

        Set<Long> categoryIds = Set.of(1L, 2L, 3L, 4L);
        when(categoryService.findAllById(categoryIds)).thenReturn(List.of(category(2L), category(4L)));

        IllegalStateException e = assertThrows(IllegalStateException.class,
            () -> sut.update(id, "name", ONE, EUR, categoryIds));

        assertEquals("Not all categories were found. Expected: [1, 2, 3, 4], found: [2, 4]", e.getMessage());
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

    private static Category category(Long id) {
        return Category.builder()
            .id(id)
            .build();
    }

    private static Product product(Long id) {
        return Product.builder()
            .id(id)
            .build();
    }

}
