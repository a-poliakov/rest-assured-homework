package org.example.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SpecialtyToAdd {
    private String name;
    public SpecialtyToAdd(String name) {
        this.name = name;
    }
}
