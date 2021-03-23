package mirogaudi.demo.productcatalog.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import mirogaudi.demo.productcatalog.domain.Category;
import mirogaudi.demo.productcatalog.service.CategoryService;
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
@Api(value = "categories")
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<Category> findAllCategories() {
        return categoryService.findAll();
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Category> getCategory(
            @ApiParam(value = "Category id", required = true) @PathVariable Long id) {
        Category category = categoryService.find(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(category);
    }

    /**
     * Multiple params instead of a POJO are used only for demo purposes.
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @ApiParam(value = "Category name", required = true) @Size(min = 3, max = 64) @RequestParam String name,
            @ApiParam(value = "Parent category id") @RequestParam(required = false) Long parentId) {
        Category createdCategory = categoryService.create(name, parentId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCategory.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdCategory);
    }

    /**
     * Multiple params instead of a POJO are used only for demo purposes.
     */
    @PutMapping(value = "/{id}")
    public ResponseEntity<Category> updateCategory(
            @ApiParam(value = "Category id", required = true) @PathVariable Long id,
            @ApiParam(value = "Category name", required = true) @Size(min = 3, max = 64) @RequestParam String name,
            @ApiParam(value = "Parent category id") @RequestParam(required = false) Long parentId) {
        return ResponseEntity.ok(categoryService.update(id, name, parentId));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteCategory(
            @ApiParam(value = "Category id", required = true) @PathVariable Long id) {
        categoryService.delete(id);

        return ResponseEntity.ok().build();
    }

}
