package org.example.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PettypesToAdd {
    private String name;

    public PettypesToAdd(String name) {
        this.name = name;
    }
}
