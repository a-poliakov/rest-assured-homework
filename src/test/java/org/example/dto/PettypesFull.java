package org.example.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PettypesFull {
    private String name;
    private int id;

    public PettypesFull(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
