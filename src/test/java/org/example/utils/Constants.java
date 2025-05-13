package org.example.utils;

public interface Constants {
    String BASE_URL = "http://localhost:9966/petclinic/api";
    int CLIENT_ERROR_CODE = 400;
    int NOT_FOUND_CODE = 404;
    int SUCCESS_CODE = 200;
    int CREATED_CODE = 201;
    int NO_CONTENT_CODE = 204;

    String ADMIN_USERNAME = "admin";
    String ADMIN_PASSWORD = "admin";
    String ADMIN_ROLE = "ROLE_ADMIN";

    String VISITS_ENDPOINT = "/visits";
    String OWNERS_ENDPOINT = "/owners";
    String PETS_ENDPOINT = "/pets";
    String PET_TYPES_ENDPOINT = "/pettypes";
    String VETS_ENDPOINT = "/vets";
    String SPECIALTIES_ENDPOINT = "/specialties";
    String USERS_ENDPOINT = "/users";

    String INVALID_DATE = "invalid-date";
    String EMPTY_DESCRIPTION = "";
}
