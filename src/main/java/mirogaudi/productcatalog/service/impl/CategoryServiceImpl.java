package mirogaudi.productcatalog.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import mirogaudi.productcatalog.domain.Category;
import mirogaudi.productcatalog.repository.CategoryRepository;
import mirogaudi.productcatalog.service.CategoryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        return StreamSupport.stream(categoryRepository.findAll().spliterator(), false)
            .toList();
    }

    @Override
    public Category find(@NonNull Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Category create(@NonNull String name,
                           Long parentId) {
        Category category = new Category();

        return save(category, name, parentId);
    }

    @Override
    public Category update(@NonNull Long id,
                           @NonNull String name,
                           Long parentId) {
        Category category = find(id);
        Assert.state(category != null, String.format(
            "Category with id '%d' not found", id));

        return save(category, name, parentId);
    }

    private Category save(Category category,
                          String name,
                          Long parentId) {
        category.setName(name);

        Category parent = null;
        if (parentId != null) {
            Assert.isTrue(!parentId.equals(category.getId()), String.format(
                "Parent category id '%d' is invalid: category can not be a parent for itself", parentId));
            parent = find(parentId);
            Assert.state(parent != null, String.format(
                "Parent category with id '%d' not found", parentId));
        }
        category.setParent(parent);

        return categoryRepository.save(category);
    }

    @Override
    public void delete(@NonNull Long id) {
        Assert.state(categoryRepository.existsById(id), String.format(
            "Category with id '%d' does not exist", id));

        categoryRepository.deleteById(id);
    }

}
