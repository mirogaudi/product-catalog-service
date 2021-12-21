package mirogaudi.demo.productcatalog.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mirogaudi.demo.productcatalog.domain.Product;
import mirogaudi.demo.productcatalog.service.ProductService;
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
import java.math.BigDecimal;
import java.net.URI;
import java.util.Currency;
import java.util.List;
import java.util.Set;

/**
 * Rest controller for CRUD operations with a product.
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products")
@Validated
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> findAllProducts() {
        return productService.findAll();
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Product> getProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id
    ) {
        Product product = productService.find(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    /**
     * Multiple params instead of a POJO are used only for demo purposes.
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Parameter(description = "Product name")
            @Size(min = 3, max = 64)
            @RequestParam String name,

            @Parameter(description = "Product original price")
            @RequestParam BigDecimal originalPrice,

            @Parameter(description = "Product original currency ISO code", schema = @Schema(
                    allowableValues = {"EUR", "USD", "CNY", "KRW", "JPY"}
            ))
            @RequestParam String originalCurrency,

            @Parameter(description = "Product category ID")
            @RequestParam Long... categoryId
    ) {
        Product createdProduct = productService.create(
                name,
                originalPrice, Currency.getInstance(originalCurrency),
                Set.of(categoryId)
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdProduct);
    }

    /**
     * Multiple params instead of a POJO are used only for demo purposes.
     */
    @PutMapping(value = "/{id}")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id,

            @Parameter(description = "Product name")
            @RequestParam String name,

            @Parameter(description = "Product original price")
            @RequestParam BigDecimal originalPrice,

            @Parameter(description = "Product original currency ISO code", schema = @Schema(
                    allowableValues = {"EUR", "USD", "CNY", "KRW", "JPY"}
            ))
            @RequestParam String originalCurrency,

            @Parameter(description = "Product category ID")
            @RequestParam Long... categoryId
    ) {
        return ResponseEntity.ok(productService.update(
                id, name,
                originalPrice, Currency.getInstance(originalCurrency),
                Set.of(categoryId)
        ));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id
    ) {
        productService.delete(id);

        return ResponseEntity.ok().build();
    }

}
