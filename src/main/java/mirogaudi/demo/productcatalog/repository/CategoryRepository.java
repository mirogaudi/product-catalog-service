package mirogaudi.demo.productcatalog.repository;

import io.swagger.annotations.Api;
import mirogaudi.demo.productcatalog.domain.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Api
public interface CategoryRepository extends CrudRepository<Category, Long> {
}