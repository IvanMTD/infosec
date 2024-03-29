package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.TicketDataTransferObject;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class Category {
    @Id
    private int id;

    private String name;
    private String description;

    private Set<Integer> troubleIds = new HashSet<>();

    public Category(TicketDataTransferObject dto) {
        setName(dto.getName());
        setDescription(dto.getDescription());
    }

    public void addTrouble(Trouble trouble){
        troubleIds.add(trouble.getId());
    }

    public Category update(TicketDataTransferObject ticketDTO) {
        setName(ticketDTO.getName());
        setDescription(ticketDTO.getDescription());
        return this;
    }
}
