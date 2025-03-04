package mirogaudi.productcatalog.controller;

import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.service.CategoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    private static final String API_CATEGORIES = "/api/v1/categories";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @AfterEach
    void tearDown() {
        reset(categoryService);
    }

    @Test
    void findAllCategories() throws Exception {
        Category topCategory = category(1L, "topCategory", null);
        Category subCategory = category(2L, "subCategory", topCategory);

        given(categoryService.findAll()).willReturn(List.of(subCategory));

        mockMvc.perform(get(API_CATEGORIES)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(subCategory.getId()))
            .andExpect(jsonPath("$[0].name", is(subCategory.getName())))
            .andExpect(jsonPath("$[0].parentId").value(topCategory.getId()));
    }

    @Test
    void getCategory_ok() throws Exception {
        Category category = category(1L, "category", null);

        given(categoryService.find(category.getId())).willReturn(category);

        mockMvc.perform(get(API_CATEGORIES + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(category.getId()))
            .andExpect(jsonPath("$.name", is(category.getName())))
            .andExpect(jsonPath("$.parentId").isEmpty());
    }

    @Test
    void getCategory_notFound() throws Exception {
        Category category = category(100L, "not existing category", null);

        given(categoryService.find(category.getId())).willReturn(null);

        mockMvc.perform(get(API_CATEGORIES + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void getCategory_badRequest() throws Exception {
        var id = "abc";

        mockMvc.perform(get(API_CATEGORIES + "/" + id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(categoryService, never()).find(any());
    }

    @ParameterizedTest
    @ValueSource(classes = {
        ConcurrencyFailureException.class,
        DataIntegrityViolationException.class
    })
    void getCategory_conflict(Class<? extends Throwable> clazz) throws Exception {
        var id = 1L;

        given(categoryService.find(id)).willThrow(clazz);

        mockMvc.perform(get(API_CATEGORIES + "/" + id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    void getCategory_internalServerError() throws Exception {
        var id = 1L;

        given(categoryService.find(id)).willThrow(IllegalStateException.class);

        mockMvc.perform(get(API_CATEGORIES + "/" + id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void createCategory() throws Exception {
        Category category = category(1L, "category", null);

        given(categoryService.create(category.getName(), null)).willReturn(category);

        mockMvc.perform(post(API_CATEGORIES)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", category.getName()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(category.getId()))
            .andExpect(jsonPath("$.name", is(category.getName())))
            .andExpect(jsonPath("$.parentId").isEmpty());
        verify(categoryService).create(category.getName(), null);
    }

    @Test
    void updateCategory() throws Exception {
        Category category = category(1L, "category", null);

        given(categoryService.update(category.getId(), category.getName(), null)).willReturn(category);

        mockMvc.perform(put(API_CATEGORIES + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", category.getName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(category.getId()))
            .andExpect(jsonPath("$.name", is(category.getName())))
            .andExpect(jsonPath("$.parentId").isEmpty());
        verify(categoryService).update(category.getId(), category.getName(), null);
    }

    @Test
    void updateCategory_badRequest() throws Exception {
        var id = 1L;
        var invalidName = "c";

        mockMvc.perform(put(API_CATEGORIES + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", invalidName))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory() throws Exception {
        Category category = category(1L, "category", null);

        mockMvc.perform(delete(API_CATEGORIES + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(categoryService).delete(category.getId());
    }

    static Category category(Long id, String name, Category parent) {
        return Category.builder()
            .id(id)
            .name(name)
            .parent(parent)
            .build();
    }

}
