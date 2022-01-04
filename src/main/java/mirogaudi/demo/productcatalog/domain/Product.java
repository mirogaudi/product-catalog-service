package mirogaudi.demo.productcatalog.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Simplified product entity, has {@link ManyToMany} relationship to a multilevel {@link Category}.
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotNull
    @Size(min = 3, max = 64)
    private String name;

    // price in base currency
    @Column(nullable = false)
    @NotNull
    private BigDecimal price;

    // base currency ISO 4217 code
    @Column(nullable = false)
    @NotNull
    @Size(min = 3, max = 3)
    private String currency;

    // price in original currency
    @Column(nullable = false)
    @NotNull
    private BigDecimal originalPrice;

    // original currency ISO 4217 code
    @Column(nullable = false)
    @NotNull
    @Size(min = 3, max = 3)
    private String originalCurrency;

    @ManyToMany(fetch = FetchType.EAGER)
    @NotNull
    @NotEmpty
    @JsonIgnore
    private List<Category> category;

    @JsonGetter
    public List<Long> getCategoryIds() {
        return category.stream()
                .map(Category::getId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Product product = (Product) o;
        return id != null && Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
