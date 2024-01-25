package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.TicketDataTransferObject;
import net.security.infosec.dto.TroubleTicketDataTransferObject;
import net.security.infosec.models.Category;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Task;
import net.security.infosec.models.Trouble;
import net.security.infosec.repositories.CategoryRepository;
import net.security.infosec.repositories.TroubleRepository;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TroubleTicketService {
    private final CategoryRepository categoryRepository;
    private final TroubleRepository troubleRepository;

    public Mono<Category> saveCategory(TicketDataTransferObject dto) {
        Category category = new Category(dto);
        return categoryRepository.save(category);
    }

    public Flux<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Mono<Category> saveTroubleInCategory(TicketDataTransferObject dto) {
        return troubleRepository.save(new Trouble(dto)).flatMap(trouble -> {
            log.info("Trouble saved: " + trouble.toString());
            return categoryRepository.findById(trouble.getCategoryId()).flatMap(category -> {
                category.addTrouble(trouble);
                return categoryRepository.save(category);
            });
        });
    }

    public Flux<TroubleTicketDataTransferObject> getTroubleTickets() {
        return categoryRepository.findAll().flatMap(category -> {
            TroubleTicketDataTransferObject dto = new TroubleTicketDataTransferObject();
            dto.setCategoryId(category.getId());
            dto.setCategoryName(category.getName());
            dto.setCategoryDescription(category.getDescription());
            return troubleRepository.findAllByIdIn(category.getTroubleIds()).collectList().flatMap(l -> {
                dto.setTroubles(l);
                return Mono.just(dto);
            });
        });
    }

    public Mono<Trouble> getTaskTrouble(Task task) {
        return troubleRepository.findById(task.getTroubleId());
    }

    public Mono<Category> getTroubleCategory(Trouble trouble) {
        return categoryRepository.findById(trouble.getCategoryId());
    }

    public Flux<Trouble> getAllTrouble() {
        return troubleRepository.findAll();
    }
}
