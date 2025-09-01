package org.example.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class VetToAdd {
    private String firstName;
    private String lastName;
    private List<SpecialtyToAdd> specialties;

    public VetToAdd(String firstName, String lastName, List<SpecialtyToAdd> specialties) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialties = specialties;
    }
}
