package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Pet {
    private String name;
    private String birthDate;
    private PetType type;
}
