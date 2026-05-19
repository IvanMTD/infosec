package net.security.infosec.component;

import net.security.infosec.models.entity.DepartmentRole;
import net.security.infosec.models.entity.Implementer;
import net.security.infosec.models.entity.Role;
import org.springframework.stereotype.Component;

@Component("securityCheck")
public class SecurityCheck {

    public boolean canManageSystems(Implementer user) {
        if (user == null) return false;
        return user.getRole() == Role.ADMIN || user.getDepartmentRole() == DepartmentRole.IB;
    }
}
