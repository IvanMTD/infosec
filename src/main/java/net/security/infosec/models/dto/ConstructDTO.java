package net.security.infosec.models.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConstructDTO {
    private int cid;
    private String title;
    private List<ChartDTO> charts = new ArrayList<>();

    public void addChart(ChartDTO chartDTO){
        charts.add(chartDTO);
    }
}
