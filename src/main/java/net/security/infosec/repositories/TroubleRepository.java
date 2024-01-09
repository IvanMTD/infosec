package net.security.infosec.repositories;

import net.security.infosec.models.Trouble;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TroubleRepository extends ReactiveCrudRepository<Trouble,Integer> {

}
