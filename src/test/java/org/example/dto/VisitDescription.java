package org.example.dto;

import lombok.*;

@Builder
@Getter
@Setter
public class VisitDescription {
    private Specialty specialty;
    private Integer vetId;
}
