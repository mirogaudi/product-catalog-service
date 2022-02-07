package mirogaudi.productcatalog.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.service.CategoryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.Size;
import java.net.URI;
import java.util.List;

/**
 * Rest controller for CRUD operations with a category.
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories")
@Validated
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<Category> findAllCategories() {
        return categoryService.findAll();
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Category> getCategory(
            @Parameter(description = "Category ID")
            @PathVariable Long id
    ) {
        Category category = categoryService.find(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @Parameter(description = "Category name")
            @Size(min = 3, max = 128)
            @RequestParam String name,

            @Parameter(description = "Parent category ID")
            @RequestParam(required = false) Long parentId
    ) {
        Category createdCategory = categoryService.create(name, parentId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCategory.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdCategory);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "Category ID")
            @PathVariable Long id,

            @Parameter(description = "Category name")
            @Size(min = 3, max = 128)
            @RequestParam String name,

            @Parameter(description = "Parent category ID")
            @RequestParam(required = false) Long parentId
    ) {
        return ResponseEntity.ok(categoryService.update(id, name, parentId));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID")
            @PathVariable Long id
    ) {
        categoryService.delete(id);

        return ResponseEntity.ok().build();
    }

}
