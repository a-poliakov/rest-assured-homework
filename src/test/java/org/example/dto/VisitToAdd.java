package org.example.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VisitToAdd {
    private String date;
    private String description;
    public VisitToAdd(String date, String description) {
        this.date = date;
        this.description = description;
    }
}
