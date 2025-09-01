package org.example.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VisitFull {
    private String date;
    private String description;
    private int id;
    private int petId;
    public VisitFull(String date, String description, int id, int petId) {
        this.date = date;
        this.description = description;
        this.id = id;
        this.petId = petId;
    }
}
