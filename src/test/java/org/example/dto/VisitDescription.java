package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class VisitDescription {
    private Speciality speciality;
    private Integer vetId;
}
