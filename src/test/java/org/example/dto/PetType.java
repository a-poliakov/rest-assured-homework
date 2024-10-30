package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PetType {
    private String name;
    private Integer id;
}
