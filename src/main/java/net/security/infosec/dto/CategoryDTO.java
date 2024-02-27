package net.security.infosec.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDTO {
    private int id;
    private String title;
    private String description;
    private boolean show = true;
    private List<TroubleDTO> troubles = new ArrayList<>();

    public void addTrouble(TroubleDTO trouble){
        troubles.add(trouble);
    }

    public void reconstruct(){
        int count = 0;
        for(TroubleDTO troubleDTO : troubles){
            if(troubleDTO.getTasks().size() == 0){
                troubleDTO.setShow(false);
            }else{
                count++;
            }
        }
        if(count == 0){
            show = false;
        }
    }
}
