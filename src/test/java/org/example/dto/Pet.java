package org.example.dto;

import lombok.*;

@Builder
@Getter
@Setter
public class Pet {
    private String name;
    private String birthDate;
    private PetType type;
}
