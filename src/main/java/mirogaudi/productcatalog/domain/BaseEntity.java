package mirogaudi.productcatalog.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Base entity, defines id and implements base equals and hashCode methods.
 */
@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
