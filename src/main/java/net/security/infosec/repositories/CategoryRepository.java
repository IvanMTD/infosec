package net.security.infosec.repositories;

import net.security.infosec.models.Category;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveCrudRepository<Category,Integer> {
    @Query("select * from category where :troubleId = any(trouble_ids)")
    Mono<Category> findCategoryWhereTroubleIdIn(@Param("troubleId") Integer id);
}
