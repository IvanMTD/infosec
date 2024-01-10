package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.ImplementerDataTransferObject;
import net.security.infosec.models.Implementer;
import net.security.infosec.repositories.ImplementerRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return implementerRepository.findByEmail(username).map(implementer -> implementer);
    }
}
