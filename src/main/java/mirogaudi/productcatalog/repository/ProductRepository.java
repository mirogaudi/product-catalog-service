package mirogaudi.productcatalog.repository;

import mirogaudi.productcatalog.domain.Product;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends ListCrudRepository<Product, Long> {
}
