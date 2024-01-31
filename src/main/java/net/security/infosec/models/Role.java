package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class Role {
    @Id
    private int id;

    private String name;
    private String description;

    private Set<Authority> authorities = new HashSet<>();
    private Set<Integer> implementersIds = new HashSet<>();

    public void addAuthority(Authority authority){
        authorities.add(authority);
    }

    public void addImplementer(Implementer implementer){
        implementersIds.add(implementer.getId());
    }

    public void updateAuthorities(Authority[] authorities) {
        setAuthorities(new HashSet<>(Arrays.asList(authorities)));
    }

    public enum Authority {
        CREATE("Создавать"),
        READ("Читать"),
        UPDATE("Обновлять"),
        DELETE("Удалять"),
        ALL("Все"),
        SELF("Свои"),
        SERVICE("Служебные");

        private final String name;

        Authority(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }
}
