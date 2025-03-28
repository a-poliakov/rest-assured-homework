package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Pet {
    private String name;
    private String birthDate;
    private PetType type;
    private Integer id;
    private Integer ownerId;
    private List<Visit> visits;
}
