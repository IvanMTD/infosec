package net.security.infosec.models.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("app_settings")
public class AppSettings {
    @Id
    private String key;
    private String value;

    public AppSettings(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
