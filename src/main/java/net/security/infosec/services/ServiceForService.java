package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.repositories.ServiceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceForService {
    private final ServiceRepository serviceRepository;

    public Mono<net.security.infosec.models.Service> create(net.security.infosec.models.Service service) {
        return serviceRepository.save(service);
    }

    public Flux<net.security.infosec.models.Service> getAll() {
        return serviceRepository.findAll().collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(net.security.infosec.models.Service::getTitle)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }
}
