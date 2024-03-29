package mirogaudi.productcatalog.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Simplified multilevel category entity.
 */
@Entity
@Table(name = "category")
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    @NotNull
    @Size(min = 3, max = 128)
    @ToString.Include
    private String name;

    // top category parent supposed to be null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @JsonGetter
    @ToString.Include
    public Long getParentId() {
        return (parent != null) ? parent.getId() : null;
    }

    @SuppressWarnings("PMD.UselessOverridingMethod") // method is needed since hashCode() was overridden
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
