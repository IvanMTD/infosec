package net.security.infosec.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DivisionNode {
    private String title;
    private List<DivisionNode> divisionNode = new ArrayList<>();
    private List<PersonNode> persons = new ArrayList<>();
}
