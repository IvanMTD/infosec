package net.security.infosec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.Category;
import net.security.infosec.models.DepartmentRole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CategoryDTO {
    private int id;
    private String title;
    private String description;
    private DepartmentRole departmentRole;
    private boolean show = true;
    private List<TroubleDTO> troubles = new ArrayList<>();

    public CategoryDTO(Category category){
        setId(category.getId());
        setTitle(category.getName());
        setDepartmentRole(category.getDepartmentRole());
        setDescription(category.getDescription());
    }

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

    public void doSort(){
        for(TroubleDTO troubleDTO : troubles){
            troubleDTO.sortTasks();
        }
        troubles = troubles.stream().sorted(Comparator.comparing(TroubleDTO::getTitle)).collect(Collectors.toList());
    }
}
