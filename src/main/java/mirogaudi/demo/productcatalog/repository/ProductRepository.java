package mirogaudi.demo.productcatalog.repository;

import io.swagger.annotations.Api;
import mirogaudi.demo.productcatalog.domain.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Api
public interface ProductRepository extends CrudRepository<Product, Long> {
}