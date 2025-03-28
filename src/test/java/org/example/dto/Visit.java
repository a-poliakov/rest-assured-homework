package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Visit {
    private String date;
    private String description;
    private Integer id;
}
