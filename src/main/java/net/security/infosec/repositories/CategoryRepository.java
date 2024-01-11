package net.security.infosec.repositories;

import net.security.infosec.models.Category;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CategoryRepository extends ReactiveCrudRepository<Category,Integer> {

}
