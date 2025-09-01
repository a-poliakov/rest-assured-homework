package org.example.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PetToAdd {
     private String name;
     private String birthDate;
     private PettypesFull type;

     public PetToAdd(String name, String birthDate, PettypesFull type) {
          this.name = name;
          this.birthDate = birthDate;
          this.type = type;
     }
}
