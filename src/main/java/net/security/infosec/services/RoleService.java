package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import net.security.infosec.repositories.RoleRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
}
