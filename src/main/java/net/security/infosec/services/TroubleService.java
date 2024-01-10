package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.TroubleDataTransferObject;
import net.security.infosec.models.Trouble;
import net.security.infosec.repositories.TroubleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TroubleService {
    private final TroubleRepository troubleRepository;

    public Mono<Trouble> saveTrouble(TroubleDataTransferObject dto){
        Trouble trouble = new Trouble(dto);
        return troubleRepository.save(trouble);
    }
}
