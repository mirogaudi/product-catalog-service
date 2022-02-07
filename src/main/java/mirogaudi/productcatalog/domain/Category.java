package mirogaudi.productcatalog.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.FetchType.LAZY;

/**
 * Simplified multilevel category entity.
 */
@Entity
@Table(name = "category")
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
@Getter
@Setter
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    @NotNull
    @Size(min = 3, max = 128)
    @ToString.Include
    private String name;

    // Top category parent supposed to be null
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @JsonGetter
    @ToString.Include
    public Long getParentId() {
        return (parent != null) ? parent.getId() : null;
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
