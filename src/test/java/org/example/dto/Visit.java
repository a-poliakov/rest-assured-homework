package org.example.dto;

import lombok.*;

@Builder
@Getter
@Setter
public class Visit {
    private String date;
    private String description;
}
