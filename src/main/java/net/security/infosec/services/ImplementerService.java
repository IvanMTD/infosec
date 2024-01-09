package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import net.security.infosec.repositories.ImplementerRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImplementerService implements ReactiveUserDetailsService {
    private final ImplementerRepository implementerRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return implementerRepository.findByEmail(username).map(implementer -> implementer);
    }
}
