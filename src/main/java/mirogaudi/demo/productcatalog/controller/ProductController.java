package mirogaudi.demo.productcatalog.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
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
@Api(value = "products")
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> findAllProducts() {
        return productService.findAll();
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Product> getProduct(
            @ApiParam(value = "Product id", required = true) @PathVariable Long id) {
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
            @ApiParam(value = "Product name", required = true) @Size(min = 3, max = 64) @RequestParam String name,
            @ApiParam(value = "Product original price", required = true) @RequestParam BigDecimal originalPrice,
            @ApiParam(value = "Product original currency ISO code", required = true, allowableValues = "EUR,USD,CNY,KRW,JPY") @RequestParam String originalCurrency,
            @ApiParam(value = "Product category id", required = true) @RequestParam Long... categoryId) {

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
            @ApiParam(value = "Product id", required = true) @PathVariable Long id,
            @ApiParam(value = "Product name", required = true) @RequestParam String name,
            @ApiParam(value = "Product original price", required = true) @RequestParam BigDecimal originalPrice,
            @ApiParam(value = "Product original currency ISO code", required = true, allowableValues = "EUR,USD,CNY,KRW,JPY") @RequestParam String originalCurrency,
            @ApiParam(value = "Product category id", required = true) @RequestParam Long... categoryId) {
        return ResponseEntity.ok(productService.update(
                id, name,
                originalPrice, Currency.getInstance(originalCurrency),
                Set.of(categoryId)
        ));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteProduct(
            @ApiParam(value = "Product id", required = true) @PathVariable Long id) {
        productService.delete(id);

        return ResponseEntity.ok().build();
    }

}
