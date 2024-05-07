package net.security.infosec.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Data
@NoArgsConstructor
public class Service {
    @Id
    private long id;
    @NotBlank(message = "Поле не может быть пустым")
    private String title;
}
