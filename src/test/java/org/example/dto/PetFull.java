package org.example.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PetFull {
    private String name;
    private String birthDate;
    private PettypesFull types;
    private int id;
    private int ownerId;
    private List<VisitFull> visits;
    public PetFull(String name, String birthDate, PettypesFull types, int id, int ownerId, List<VisitFull> visits) {
        this.name = name;
        this.birthDate = birthDate;
        this.types = types;
        this.id = id;
        this.ownerId = ownerId;
        this.visits = visits;
    }
}
