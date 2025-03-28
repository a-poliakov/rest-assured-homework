package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Owner {
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    private Integer id;
}
