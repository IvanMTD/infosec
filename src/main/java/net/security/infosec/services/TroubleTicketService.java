package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.dto.TicketDataTransferObject;
import net.security.infosec.models.dto.TroubleTicketDataTransferObject;
import net.security.infosec.models.entity.Category;
import net.security.infosec.models.entity.DepartmentRole;
import net.security.infosec.models.entity.Task;
import net.security.infosec.models.entity.Trouble;
import net.security.infosec.repositories.CategoryRepository;
import net.security.infosec.repositories.TroubleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

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
        return categoryRepository.findAll().collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Category::getName)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Mono<Category> saveTroubleInCategory(TicketDataTransferObject dto) {
        return categoryRepository.findById(dto.getCategoryId()).flatMap(category -> {
            dto.setDepartmentRole(category.getDepartmentRole());
            return troubleRepository.save(new Trouble(dto)).flatMap(trouble -> {
                log.info("Trouble saved: " + trouble.toString());
                return Mono.just(category);
            });
        });
    }

    public Flux<TroubleTicketDataTransferObject> getTroubleTickets(DepartmentRole departmentRole) {
        return categoryRepository.findAll().flatMap(category -> {
            if(category.getDepartmentRole() == departmentRole) {
                TroubleTicketDataTransferObject dto = new TroubleTicketDataTransferObject();
                dto.setCategoryId(category.getId());
                dto.setCategoryName(category.getName());
                dto.setCategoryDescription(category.getDescription());
                return troubleRepository.findAllByCategoryId(category.getId()).collectList().flatMap(l -> {
                    l = l.stream().sorted(Comparator.comparing(Trouble::getName)).collect(Collectors.toList());
                    dto.setTroubles(l);
                    return Mono.just(dto);
                });
            }else{
                return Mono.empty();
            }
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(TroubleTicketDataTransferObject::getCategoryName)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
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

    public Mono<TicketDataTransferObject> getCategoryDTOById(int id) {
        return categoryRepository.findById(id).flatMap(category -> Mono.just(new TicketDataTransferObject(category)));
    }

    public Mono<Category> updateCategory(TicketDataTransferObject ticketDTO, int id) {
        return categoryRepository.findById(id).flatMap(category -> {
            return categoryRepository.save(category.update(ticketDTO)).flatMap(saved -> {
                return troubleRepository.findAllByCategoryId(saved.getId()).flatMap(trouble -> {
                    trouble.setDepartmentRole(saved.getDepartmentRole());
                    return troubleRepository.save(trouble);
                }).collectList().flatMap(l -> {
                    return Mono.just(saved);
                });
            });
        });
    }

    public Mono<TicketDataTransferObject> getTroubleDTOById(int id) {
        return troubleRepository.findById(id).flatMap(trouble -> Mono.just(new TicketDataTransferObject(trouble)));
    }

    public Mono<Trouble> updateTrouble(TicketDataTransferObject ticketDTO, int id) {
        return troubleRepository.findById(id).flatMap(trouble -> troubleRepository.save(trouble.update(ticketDTO)));
    }

    public Flux<Trouble> getTroubleByIds(Set<Integer> troubleIds) {
        return troubleRepository.findAllByIdIn(troubleIds);
    }

    public Flux<Trouble> getTroublesByCategoryId(Integer categoryId) {
        return troubleRepository.findAllByCategoryId(categoryId);
    }

    public Mono<Category> redirectTroubleInCategory(Trouble trouble) {
        return categoryRepository.findById(trouble.getCategoryId());
    }

    public Mono<Trouble> getTroubleById(int troubleId) {
        return troubleRepository.findById(troubleId);
    }

    public Mono<Category> getCategoryById(int categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Mono<java.util.Map<Integer, Integer>> getTroubleCategoryMap() {
        return troubleRepository.findAll().collectList().map(troubles -> {
            java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
            for(Trouble t : troubles) {
                map.put(t.getId(), t.getCategoryId());
            }
            return map;
        });
    }
}
