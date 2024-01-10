package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.ImplementerDataTransferObject;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class Implementer implements UserDetails {
    @Id
    private int id;
    /**
     * Описание пользователя
     */
    private String email;
    private String password;
    private String firstname;
    private String middleName;
    private String lastname;
    private String officePosition;
    /**
     * Связные модели
     */
    private Set<Integer> taskIds = new HashSet<>();
    private int roleId;

    public Implementer(ImplementerDataTransferObject dto){
        setEmail(dto.getEmail());
        setPassword(dto.getPassword());
        setFirstname(dto.getFirstname());
        setMiddleName(dto.getMiddleName());
        setLastname(dto.getLastname());
        setOfficePosition(dto.getOfficePosition());
        setRoleId(dto.getRoleId());
    }

    public void addTask(Task task){
        taskIds.add(task.getId());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new HashSet<>();
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
