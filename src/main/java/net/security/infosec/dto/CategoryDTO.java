package net.security.infosec.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDTO {
    private String title;
    private List<TroubleDTO> troubles = new ArrayList<>();

    public void addTrouble(TroubleDTO trouble){
        troubles.add(trouble);
    }
}