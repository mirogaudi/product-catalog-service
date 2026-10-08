package mirogaudi.productcatalog.controller;

import mirogaudi.productcatalog.connector.ConnectorRuntimeException;
import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.domain.Product;
import mirogaudi.productcatalog.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static java.math.BigDecimal.TEN;
import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private static final String API_PRODUCTS = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @AfterEach
    void tearDown() {
        reset(productService);
    }

    @Test
    void getProducts() throws Exception {
        Category category = category(2L);
        Product product = product(1L, "product", category);

        given(productService.findAll()).willReturn(List.of(product));

        mockMvc.perform(get(API_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(product.getId().intValue())))
            .andExpect(jsonPath("$[0].name", is(product.getName())))
            .andExpect(jsonPath("$[0].categoryIds", hasSize(1)))
            .andExpect(jsonPath("$[0].categoryIds", hasItems(category.getId().intValue())));

        verify(productService).findAll();
    }

    @Test
    void getProduct_ok() throws Exception {
        Category category = category(2L);
        Product product = product(1L, "product", category);

        given(productService.find(product.getId())).willReturn(product);

        mockMvc.perform(get(API_PRODUCTS + "/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(product.getId().intValue())))
            .andExpect(jsonPath("$.name", is(product.getName())))
            .andExpect(jsonPath("$.categoryIds", hasSize(1)))
            .andExpect(jsonPath("$.categoryIds", hasItems(category.getId().intValue())));

        verify(productService).find(product.getId());
    }

    @Test
    void getProduct_notFound() throws Exception {
        var nonExistingProductId = -123L;

        given(productService.find(nonExistingProductId)).willReturn(null);

        mockMvc.perform(get(API_PRODUCTS + "/" + nonExistingProductId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(productService).find(nonExistingProductId);
    }

    @Test
    void createProduct() throws Exception {
        Category category1 = category(2L);
        Category category2 = category(3L);
        Set<Long> categoryIds = Set.of(category1.getId(), category2.getId());
        Product product = product(1L, "product", category1, category2);

        given(productService.create(
            product.getName(),
            product.getOriginalPrice(),
            Currency.getInstance(product.getOriginalCurrency()),
            categoryIds
        )).willReturn(product);

        mockMvc.perform(post(API_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", product.getName())
                .param("originalPrice", product.getOriginalPrice().toString())
                .param("originalCurrency", product.getOriginalCurrency())
                .param("categoryId", category1.getId().toString(), category2.getId().toString()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(product.getId().intValue())))
            .andExpect(jsonPath("$.name", is(product.getName())))
            .andExpect(jsonPath("$.categoryIds", hasSize(2)))
            .andExpect(jsonPath("$.categoryIds", hasItems(category1.getId().intValue(), category2.getId().intValue())));

        verify(productService).create(
            product.getName(),
            product.getOriginalPrice(),
            Currency.getInstance(product.getOriginalCurrency()),
            categoryIds
        );
    }

    @Test
    void createProduct_nameTooShort() throws Exception {
        String tooShortName = "ab";

        mockMvc.perform(post(API_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", tooShortName)
                .param("originalPrice", TEN.toString())
                .param("originalCurrency", EUR.getCurrencyCode())
                .param("categoryId", "1"))
            .andExpect(status().isBadRequest());

        verify(productService, never())
            .create(anyString(), any(BigDecimal.class), any(Currency.class), anySet());
    }

    @Test
    void createProduct_noOriginalPrice() throws Exception {
        mockMvc.perform(post(API_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "product")
                .param("originalCurrency", EUR.getCurrencyCode())
                .param("categoryId", "1"))
            .andExpect(status().isBadRequest());

        verify(productService, never())
            .create(anyString(), any(BigDecimal.class), any(Currency.class), anySet());
    }

    @Test
    void createProduct_noOriginalCurrency() throws Exception {
        mockMvc.perform(post(API_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "product")
                .param("originalPrice", TEN.toString())
                .param("categoryId", "1"))
            .andExpect(status().isBadRequest());

        verify(productService, never())
            .create(anyString(), any(BigDecimal.class), any(Currency.class), anySet());
    }

    @Test
    void createProduct_noCategoryId() throws Exception {
        mockMvc.perform(post(API_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "product")
                .param("originalPrice", TEN.toString())
                .param("originalCurrency", EUR.getCurrencyCode()))
            .andExpect(status().isBadRequest());

        verify(productService, never())
            .create(anyString(), any(BigDecimal.class), any(Currency.class), anySet());
    }

    @Test
    void createProduct_badGateway() throws Exception {
        Category category = category(2L);
        Set<Long> categoryIds = Set.of(category.getId());
        Product product = product(1L, "product", category);

        given(productService.create(
            product.getName(),
            product.getOriginalPrice(),
            Currency.getInstance(product.getOriginalCurrency()),
            categoryIds
        )).willThrow(ConnectorRuntimeException.class);

        mockMvc.perform(post(API_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", product.getName())
                .param("originalPrice", product.getOriginalPrice().toString())
                .param("originalCurrency", product.getOriginalCurrency())
                .param("categoryId", category.getId().toString()))
            .andExpect(status().isBadGateway());

        verify(productService).create(
            product.getName(),
            product.getOriginalPrice(),
            Currency.getInstance(product.getOriginalCurrency()),
            categoryIds
        );
    }

    @Test
    void createProduct_internalServerError() throws Exception {
        Category category = category(2L);
        Set<Long> categoryIds = Set.of(category.getId());
        Product product = product(1L, "product", category);

        given(productService.create(
            product.getName(),
            product.getOriginalPrice(),
            Currency.getInstance(product.getOriginalCurrency()),
            categoryIds
        )).willThrow(RuntimeException.class);

        mockMvc.perform(post(API_PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", product.getName())
                .param("originalPrice", product.getOriginalPrice().toString())
                .param("originalCurrency", product.getOriginalCurrency())
                .param("categoryId", category.getId().toString()))
            .andExpect(status().isInternalServerError());

        verify(productService).create(
            product.getName(),
            product.getOriginalPrice(),
            Currency.getInstance(product.getOriginalCurrency()),
            categoryIds
        );
    }

    @Test
    void updateProduct() throws Exception {
        Category category1 = category(2L);
        Category category2 = category(3L);
        Set<Long> categoryIds = Set.of(category1.getId(), category2.getId());
        Product product = product(2L, "updated product", category1, category2);

        given(productService.update(
            product.getId(),
            product.getName(),
            product.getOriginalPrice(),
            Currency.getInstance(product.getOriginalCurrency()),
            categoryIds
        )).willReturn(product);

        mockMvc.perform(put(API_PRODUCTS + "/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", product.getName())
                .param("originalPrice", product.getOriginalPrice().toString())
                .param("originalCurrency", product.getOriginalCurrency())
                .param("categoryId", category1.getId().toString(), category2.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(product.getId().intValue())))
            .andExpect(jsonPath("$.name", is(product.getName())))
            .andExpect(jsonPath("$.categoryIds", hasSize(2)))
            .andExpect(jsonPath("$.categoryIds", hasItems(category1.getId().intValue(), category2.getId().intValue())));

        verify(productService).update(
            product.getId(),
            product.getName(),
            product.getOriginalPrice(),
            Currency.getInstance(product.getOriginalCurrency()),
            categoryIds
        );
    }

    @Test
    void updateProduct_nameTooShort() throws Exception {
        String tooShortName = "ab";

        mockMvc.perform(put(API_PRODUCTS + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", tooShortName)
                .param("originalPrice", TEN.toString())
                .param("originalCurrency", EUR.getCurrencyCode())
                .param("categoryId", "1"))
            .andExpect(status().isBadRequest());

        verify(productService, never())
            .create(anyString(), any(BigDecimal.class), any(Currency.class), anySet());
    }

    @Test
    void updateProduct_noOriginalPrice() throws Exception {
        mockMvc.perform(put(API_PRODUCTS + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "product")
                .param("originalCurrency", EUR.getCurrencyCode())
                .param("categoryId", "1"))
            .andExpect(status().isBadRequest());

        verify(productService, never())
            .create(anyString(), any(BigDecimal.class), any(Currency.class), anySet());
    }

    @Test
    void updateProduct_noOriginalCurrency() throws Exception {
        mockMvc.perform(put(API_PRODUCTS + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "product")
                .param("originalPrice", TEN.toString())
                .param("categoryId", "1"))
            .andExpect(status().isBadRequest());

        verify(productService, never())
            .create(anyString(), any(BigDecimal.class), any(Currency.class), anySet());
    }

    @Test
    void updateProduct_noCategoryId() throws Exception {
        mockMvc.perform(put(API_PRODUCTS + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "product")
                .param("originalPrice", TEN.toString())
                .param("originalCurrency", EUR.getCurrencyCode()))
            .andExpect(status().isBadRequest());

        verify(productService, never())
            .create(anyString(), any(BigDecimal.class), any(Currency.class), anySet());
    }

    @Test
    void deleteProduct() throws Exception {
        var productId = 1L;

        mockMvc.perform(delete(API_PRODUCTS + "/" + productId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(productService).delete(productId);
    }

    private static Category category(Long id) {
        return Category.builder()
            .id(id)
            .build();
    }

    private static Product product(Long id, String name, Category... categories) {
        return Product.builder()
            .id(id)
            .name(name)
            .originalPrice(TEN)
            .originalCurrency(EUR.getCurrencyCode())
            .categories(List.of(categories))
            .build();
    }

}
