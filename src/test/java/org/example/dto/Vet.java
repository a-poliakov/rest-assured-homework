package org.example.dto;

import lombok.*;
import java.util.List;

@Builder
@Getter
@Setter
public class Vet {
    private String firstName;
    private String lastName;
    private List<Specialty> specialties;
}


