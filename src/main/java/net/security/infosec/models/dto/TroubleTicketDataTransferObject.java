package net.security.infosec.models.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.entity.Trouble;

import java.util.List;

@Data
@RequiredArgsConstructor
public class TroubleTicketDataTransferObject {
    private int categoryId;
    private String categoryName;
    private String categoryDescription;
    private List<Trouble> troubles;
}
