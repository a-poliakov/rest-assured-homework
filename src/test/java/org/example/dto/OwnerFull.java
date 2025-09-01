package org.example.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class OwnerFull {
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    private int id;
    private List<PetFull> pets;
}
