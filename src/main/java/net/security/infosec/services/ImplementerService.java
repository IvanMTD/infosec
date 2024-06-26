package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.ImplementerDataTransferObject;
import net.security.infosec.dto.PasswordDTO;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Task;
import net.security.infosec.repositories.ImplementerRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImplementerService implements ReactiveUserDetailsService {
    private final ImplementerRepository implementerRepository;
    private final PasswordEncoder encoder;

    public Mono<Implementer> saveImplementer(ImplementerDataTransferObject implementerDTO){
        implementerDTO.setPassword(encoder.encode(implementerDTO.getPassword()));
        Implementer implementer = new Implementer(implementerDTO);
        return implementerRepository.save(implementer);
    }

    public Mono<Implementer> updateImplementerTask(Implementer implementer, Task task) {
        return implementerRepository.findById(implementer.getId()).flatMap(i -> {
            i.addTask(task);
            return implementerRepository.save(i);
        });
    }

    public Mono<Implementer> getUserById(int id) {
        return implementerRepository.findById(id);
    }

    public Mono<Implementer> updateImplementer(ImplementerDataTransferObject implementerDTO) {
        return implementerRepository.findById(implementerDTO.getId()).flatMap(implementer -> implementerRepository.save(implementer.update(implementerDTO)));
    }

    public Mono<ImplementerDataTransferObject> getUserDtoById(int id) {
        return implementerRepository.findById(id).flatMap(implementer -> Mono.just(new ImplementerDataTransferObject(implementer)));
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return implementerRepository.findByEmail(username).map(implementer -> implementer);
    }

    public Flux<Implementer> getAll() {
        return implementerRepository.findAll();
    }

    public Mono<Void> deleteImplementerById(int id) {
        return implementerRepository.findById(id).flatMap(implementerRepository::delete);
    }

    public Mono<Implementer> updateUserPassword(int userId, PasswordDTO passwordDTO) {
        return implementerRepository.findById(userId).flatMap(implementer -> {
            implementer.setPassword(encoder.encode(passwordDTO.getNewPassword()));
            return implementerRepository.save(implementer);
        });
    }

    public Mono<Implementer> removeTaskFromImplementer(Task originalTask) {
        return implementerRepository.findById(originalTask.getImplementerId()).flatMap(implementer -> {
            implementer.getTaskIds().remove(originalTask.getId());
            return implementerRepository.save(implementer);
        });
    }

    public Mono<Implementer> addTaskInImplementer(Task task) {
        return implementerRepository.findById(task.getImplementerId()).flatMap(implementer -> {
            implementer.addTask(task);
            return implementerRepository.save(implementer);
        });
    }
}
