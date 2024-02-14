package net.security.infosec.converters;

import lombok.NonNull;
import net.security.infosec.models.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter implements Converter<String, Role> {
    @Override
    public Role convert(@NonNull String source) {
        return switch (source) {
            case "ADMIN" -> Role.ADMIN;
            case "DIRECTOR" -> Role.DIRECTOR;
            case "MANAGER" -> Role.MANAGER;
            case "WORKER" -> Role.WORKER;
            default -> null;
        };
    }
}
