package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.ImplementerDataTransferObject;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

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
    private Role role;
    private DepartmentRole departmentRole;
    /**
     * Связные модели
     */
    private Set<Integer> taskIds = new HashSet<>();

    public Implementer(ImplementerDataTransferObject dto){
        setEmail(dto.getEmail());
        setPassword(dto.getPassword());
        setFirstname(dto.getFirstname());
        setMiddleName(dto.getMiddleName());
        setLastname(dto.getLastname());
        setOfficePosition(dto.getOfficePosition());
        setRole(dto.getRole());
        setDepartmentRole(dto.getDepartmentRole());
    }

    public Implementer update(ImplementerDataTransferObject implementerDTO) {
        setFirstname(implementerDTO.getFirstname());
        setMiddleName(implementerDTO.getMiddleName());
        setLastname(implementerDTO.getLastname());
        setOfficePosition(implementerDTO.getOfficePosition());
        setRole(implementerDTO.getRole());
        setDepartmentRole(implementerDTO.getDepartmentRole());
        return this;
    }

    public void addTask(Task task){
        taskIds.add(task.getId());
    }

    public String getFullName(){
        return lastname + " " + firstname + " " + middleName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_" + getRole().toString()));
        return list;
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
