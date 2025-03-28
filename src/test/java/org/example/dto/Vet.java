package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Vet {
    String firstName;
    String lastName;
    List<Specialty> specialties;
    Integer id;
}
