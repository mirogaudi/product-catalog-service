package mirogaudi.productcatalog.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Simplified multilevel category entity.
 */
@Entity
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotNull
    @Size(min = 3, max = 64)
    private String name;

    // top category parent supposed to be null
    @ManyToOne
    @JsonIgnore
    private Category parent;

    @JsonGetter
    public Long getParentId() {
        return (parent != null) ? parent.getId() : null;
    }

}
