package org.example.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Customer {
    private String firstName;
    private String lastName;
}
