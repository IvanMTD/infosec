package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Set;

@Data
@RequiredArgsConstructor
public class Role {
    @Id
    private int id;

    private String name;
    private String description;

    private Set<Authority> authorities;
    private Set<Integer> implementersIds;

    public enum Authority {
        CREATE("Создавать"),
        READ("Читать"),
        UPDATE("Обновлять"),
        DELETE("Удалять"),
        ALL("Все"),
        SELF("Свои");

        private String name;

        Authority(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }
}
