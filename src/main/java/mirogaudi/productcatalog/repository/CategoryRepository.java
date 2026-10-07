package mirogaudi.productcatalog.repository;

import mirogaudi.productcatalog.domain.Category;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends ListCrudRepository<Category, Long> {
}
