package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.TicketDataTransferObject;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor
public class Trouble {
    @Id
    private int id;
    /**
     * Описание модели
     */
    private String name;
    private String description;
    /**
     * Связные модели
     */
    private int categoryId;

    public Trouble(TicketDataTransferObject dto) {
        setName(dto.getName());
        setDescription(dto.getDescription());
        setCategoryId(dto.getCategoryId());
    }

    public Trouble update(TicketDataTransferObject ticketDTO) {
        setName(ticketDTO.getName());
        setDescription(ticketDTO.getDescription());
        setCategoryId(ticketDTO.getCategoryId());
        return this;
    }
}
