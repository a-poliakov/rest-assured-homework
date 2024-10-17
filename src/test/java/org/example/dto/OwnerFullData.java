package org.example.dto;

import lombok.*;

@Builder
@Getter
@Setter
public class OwnerFullData {
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;;
    private long ownerId;
}
