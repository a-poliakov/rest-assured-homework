package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class Vet {
    private String firstName;
    private String lastName;
    private List<Speciality> specialties;
}
