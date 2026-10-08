package mirogaudi.productcatalog.controller;

import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.service.CategoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyLong;
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
    void getCategories() throws Exception {
        Category topCategory = category(1L, "topCategory", null);
        Category subCategory = category(2L, "subCategory", topCategory);

        given(categoryService.findAll()).willReturn(List.of(subCategory));

        mockMvc.perform(get(API_CATEGORIES)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(subCategory.getId().intValue())))
            .andExpect(jsonPath("$[0].name", is(subCategory.getName())))
            .andExpect(jsonPath("$[0].parentId", is(topCategory.getId().intValue())));

        verify(categoryService).findAll();
    }

    @Test
    void getCategory_ok() throws Exception {
        Category category = category(1L, "category", null);

        given(categoryService.find(category.getId())).willReturn(category);

        mockMvc.perform(get(API_CATEGORIES + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(category.getId().intValue())))
            .andExpect(jsonPath("$.name", is(category.getName())))
            .andExpect(jsonPath("$.parentId", nullValue()));

        verify(categoryService).find(category.getId());
    }

    @Test
    void getCategory_notFound() throws Exception {
        var nonExistingCategoryId = -123L;

        given(categoryService.find(nonExistingCategoryId)).willReturn(null);

        mockMvc.perform(get(API_CATEGORIES + "/" + nonExistingCategoryId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(categoryService).find(nonExistingCategoryId);
    }

    @Test
    void createCategory() throws Exception {
        Category category = category(1L, "category", null);

        given(categoryService.create(category.getName(), null)).willReturn(category);

        mockMvc.perform(post(API_CATEGORIES)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", category.getName()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(category.getId().intValue())))
            .andExpect(jsonPath("$.name", is(category.getName())))
            .andExpect(jsonPath("$.parentId", nullValue()));

        verify(categoryService).create(category.getName(), null);
    }

    @Test
    void createCategory_nameTooShort() throws Exception {
        String tooShortName = "ab";

        mockMvc.perform(post(API_CATEGORIES)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", tooShortName))
            .andExpect(status().isBadRequest());

        verify(categoryService, never()).create(anyString(), anyLong());
    }

    @ParameterizedTest
    @ValueSource(classes = {
        ConcurrencyFailureException.class,
        DataIntegrityViolationException.class
    })
    void createCategory_conflict(Class<? extends Throwable> clazz) throws Exception {
        Category category = category(1L, "category", null);

        given(categoryService.create(category.getName(), null)).willThrow(clazz);

        mockMvc.perform(post(API_CATEGORIES)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", category.getName()))
            .andExpect(status().isConflict());

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
            .andExpect(jsonPath("$.id", is(category.getId().intValue())))
            .andExpect(jsonPath("$.name", is(category.getName())))
            .andExpect(jsonPath("$.parentId", nullValue()));

        verify(categoryService).update(category.getId(), category.getName(), null);
    }

    @Test
    void updateCategory_nameTooShort() throws Exception {
        String tooShortName = "ab";

        mockMvc.perform(put(API_CATEGORIES + "/" + 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", tooShortName))
            .andExpect(status().isBadRequest());

        verify(categoryService, never()).update(anyLong(), anyString(), anyLong());
    }

    @Test
    void deleteCategory() throws Exception {
        var categoryId = 1L;

        mockMvc.perform(delete(API_CATEGORIES + "/" + categoryId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(categoryService).delete(categoryId);
    }

    static Category category(Long id, String name, Category parent) {
        return Category.builder()
            .id(id)
            .name(name)
            .parent(parent)
            .build();
    }

}
