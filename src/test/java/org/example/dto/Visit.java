package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Visit {
    private String date;
    private String description;

    @Override
    public String toString() {
        return "Visit{" +
                "date='" + date + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
