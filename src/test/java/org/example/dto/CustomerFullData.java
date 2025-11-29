package org.example.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CustomerFullData {
    private String firstName;
    private String lastName;
    private long customerId;

}
