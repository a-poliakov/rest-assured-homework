package org.example.dto;

import lombok.Builder;
import lombok.Data;


import java.util.List;

@Builder
@Data
public class User {
  private String username;
  private String password;
  private Boolean enabled;
  private List<Role> roles;
}
