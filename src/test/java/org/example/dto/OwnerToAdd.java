package org.example.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OwnerToAdd {
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    public OwnerToAdd(String firstName, String lastName, String address, String city, String telephone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.telephone = telephone;
    }
}
