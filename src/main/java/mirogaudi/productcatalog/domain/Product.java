package mirogaudi.productcatalog.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified product entity, has {@link ManyToMany} relationship to a multilevel {@link Category}.
 */
@Entity
@Table(name = "product")
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    @NotNull
    @Size(min = 3, max = 256)
    @ToString.Include
    private String name;

    // price in base currency
    @Column(name = "price", nullable = false)
    @NotNull
    @ToString.Include
    private BigDecimal price;

    // base currency ISO 4217 code
    @Column(name = "currency", nullable = false)
    @NotNull
    @Size(min = 3, max = 3)
    @ToString.Include
    private String currency;

    // price in original currency
    @Column(name = "original_price", nullable = false)
    @NotNull
    @ToString.Include
    private BigDecimal originalPrice;

    // original currency ISO 4217 code
    @Column(name = "original_currency", nullable = false)
    @NotNull
    @Size(min = 3, max = 3)
    @ToString.Include
    private String originalCurrency;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "product_category",
        joinColumns = @JoinColumn(name = "product_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id"))
    @NotNull
    @NotEmpty
    @JsonIgnore
    private List<Category> categories = new ArrayList<>();

    @JsonGetter
    @ToString.Include
    public List<Long> getCategoryIds() {
        return categories.stream()
            .map(Category::getId)
            .sorted()
            .toList();
    }

    /**
     * Sets categories converting given immutable category list into mutable one to meet Hibernate expectations.
     */
    public void setCategories(List<Category> categories) {
        this.categories = new ArrayList<>(categories);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
