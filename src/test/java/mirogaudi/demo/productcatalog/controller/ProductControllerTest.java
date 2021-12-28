package mirogaudi.demo.productcatalog.controller;

import mirogaudi.demo.productcatalog.domain.Category;
import mirogaudi.demo.productcatalog.domain.Product;
import mirogaudi.demo.productcatalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Set;

import static java.math.BigDecimal.TEN;
import static mirogaudi.demo.productcatalog.controller.CategoryControllerTest.category;
import static mirogaudi.demo.productcatalog.testhelper.Currencies.EUR;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
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

    private static final Long CATEGORY_ID = 1L;
    private static final Category CATEGORY = category(CATEGORY_ID, "category", null);
    private static final String API_PRODUCTS = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void findAllProducts() throws Exception {
        Product product = product(1L, "product", CATEGORY);

        given(productService.findAll()).willReturn(Collections.singletonList(product));

        mockMvc.perform(get(API_PRODUCTS + "/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(product.getId()))
                .andExpect(jsonPath("$[0].name", is(product.getName())))
                .andExpect(jsonPath("$[0].categoryIds", hasSize(1)))
                .andExpect(jsonPath("$[0].categoryIds[0]").value(CATEGORY.getId()));
    }

    @Test
    void getProduct_ok() throws Exception {
        Product product = product(1L, "product", CATEGORY);

        given(productService.find(product.getId())).willReturn(product);

        mockMvc.perform(get(API_PRODUCTS + "/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name", is(product.getName())))
                .andExpect(jsonPath("$.categoryIds", hasSize(1)))
                .andExpect(jsonPath("$.categoryIds[0]").value(CATEGORY.getId()));
    }

    @Test
    void getProduct_notFound() throws Exception {
        Product product = product(100L, "not existing product", CATEGORY);

        given(productService.find(product.getId())).willReturn(null);

        mockMvc.perform(get(API_PRODUCTS + "/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct() throws Exception {
        Product product = product(1L, "product", CATEGORY);

        Set<Long> categoryIds = Set.of(CATEGORY.getId());
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
                        .param("categoryId", CATEGORY_ID.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name", is(product.getName())))
                .andExpect(jsonPath("$.categoryIds", hasSize(1)))
                .andExpect(jsonPath("$.categoryIds[0]").value(CATEGORY.getId()));

        verify(productService).create(
                product.getName(),
                product.getOriginalPrice(),
                Currency.getInstance(product.getOriginalCurrency()),
                categoryIds
        );

        reset(productService);
    }

    @Test
    void updateProduct() throws Exception {
        Product product = product(1L, "product", CATEGORY);

        Set<Long> categoryIds = Set.of(CATEGORY.getId());
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
                        .param("categoryId", CATEGORY_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name", is(product.getName())))
                .andExpect(jsonPath("$.categoryIds", hasSize(1)))
                .andExpect(jsonPath("$.categoryIds[0]").value(CATEGORY.getId()));

        verify(productService).update(
                product.getId(),
                product.getName(),
                product.getOriginalPrice(),
                Currency.getInstance(product.getOriginalCurrency()),
                categoryIds
        );

        reset(productService);
    }

    @Test
    void deleteProduct() throws Exception {
        Product product = product(1L, "product", CATEGORY);

        mockMvc.perform(delete(API_PRODUCTS + "/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(productService).delete(product.getId());

        reset(productService);
    }

    private static Product product(Long id, String name, Category... categories) {
        var product = new Product();

        product.setId(id);
        product.setName(name);
        product.setOriginalPrice(TEN);
        product.setOriginalCurrency(EUR.getCurrencyCode());
        product.setCategory(Arrays.asList(categories));

        return product;
    }

}