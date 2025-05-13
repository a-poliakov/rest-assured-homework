package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestDataSetup {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static int owner1Id;
    public static int owner2Id;
    public static int pet1Id;
    public static int pet2Id;
    public static int pet3Id;
    public static int vet1Id;
    public static int vet2Id;
    public static int vet3Id;

    public static void setupTestData() {
        // Добавление admin пользователя
        addAdminUser();

        // Добавление 2 владельцев
        owner1Id = addOwner("John", "Doe", "Main St", "New York", "123456789");
        owner2Id = addOwner("Jane", "Smith", "Second St", "Boston", "987654321");

        // Добавление 3 видов питомцев
        int petType1Id = addPetType("Dog");
        int petType2Id = addPetType("Cat");
        int petType3Id = addPetType("Bird");

        // Добавление 5 питомцев
        pet1Id = addPet("Rex", owner1Id, petType1Id, "2018-01-01");
        pet2Id = addPet("Whiskers", owner1Id, petType2Id, "2019-05-15");
        pet3Id = addPet("Tweety", owner2Id, petType3Id, "2020-03-10");
        addPet("Max", owner2Id, petType1Id, "2017-11-20");
        addPet("Mittens", owner1Id, petType2Id, "2021-02-28");

        // Создание 2 специализаций ветеринаров
        int spec1Id = addSpecialty("Surgery");
        int spec2Id = addSpecialty("Dentistry");

        // Создание 3 ветеринаров
        vet1Id = addVet("James", "Carter", new int[]{spec1Id});
        vet2Id = addVet("Helen", "Leary", new int[]{spec2Id});
        vet3Id = addVet("Linda", "Douglas", new int[]{spec1Id, spec2Id});
    }

    private static void addAdminUser() {
        ObjectNode user = mapper.createObjectNode();
        user.put("username", Constants.ADMIN_USERNAME);
        user.put("password", Constants.ADMIN_PASSWORD);
        user.put("enabled", true);

        ObjectNode role = mapper.createObjectNode();
        role.put("name", Constants.ADMIN_ROLE);
        user.set("role", role);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(user.toString())
                .post(Constants.BASE_URL + Constants.USERS_ENDPOINT);

        if (response.getStatusCode() != Constants.CREATED_CODE) {
            throw new RuntimeException("Failed to create admin user");
        }
    }

    private static int addOwner(String firstName, String lastName, String address, String city, String telephone) {
        ObjectNode owner = mapper.createObjectNode();
        owner.put("firstName", firstName);
        owner.put("lastName", lastName);
        owner.put("address", address);
        owner.put("city", city);
        owner.put("telephone", telephone);

        Response response = RestAssured.given()
                .auth().basic(Constants.ADMIN_USERNAME, Constants.ADMIN_PASSWORD)
                .contentType(ContentType.JSON)
                .body(owner.toString())
                .post(Constants.BASE_URL + Constants.OWNERS_ENDPOINT);

        assertEquals(Constants.CREATED_CODE, response.getStatusCode());
        return response.jsonPath().getInt("id");
    }

    private static int addPetType(String name) {
        ObjectNode petType = mapper.createObjectNode();
        petType.put("name", name);

        Response response = RestAssured.given()
                .auth().basic(Constants.ADMIN_USERNAME, Constants.ADMIN_PASSWORD)
                .contentType(ContentType.JSON)
                .body(petType.toString())
                .post(Constants.BASE_URL + Constants.PET_TYPES_ENDPOINT);

        assertEquals(Constants.CREATED_CODE, response.getStatusCode());
        return response.jsonPath().getInt("id");
    }

    private static int addPet(String name, int ownerId, int typeId, String birthDate) {
        ObjectNode pet = mapper.createObjectNode();
        pet.put("name", name);
        pet.put("ownerId", ownerId);
        pet.put("typeId", typeId);
        pet.put("birthDate", birthDate);

        Response response = RestAssured.given()
                .auth().basic(Constants.ADMIN_USERNAME, Constants.ADMIN_PASSWORD)
                .contentType(ContentType.JSON)
                .body(pet.toString())
                .post(Constants.BASE_URL + Constants.PETS_ENDPOINT);

        assertEquals(Constants.CREATED_CODE, response.getStatusCode());
        return response.jsonPath().getInt("id");
    }

    private static int addSpecialty(String name) {
        ObjectNode specialty = mapper.createObjectNode();
        specialty.put("name", name);

        Response response = RestAssured.given()
                .auth().basic(Constants.ADMIN_USERNAME, Constants.ADMIN_PASSWORD)
                .contentType(ContentType.JSON)
                .body(specialty.toString())
                .post(Constants.BASE_URL + Constants.SPECIALTIES_ENDPOINT);

        assertEquals(Constants.CREATED_CODE, response.getStatusCode());
        return response.jsonPath().getInt("id");
    }

    private static int addVet(String firstName, String lastName, int[] specialtyIds) {
        ObjectNode vet = mapper.createObjectNode();
        vet.put("firstName", firstName);
        vet.put("lastName", lastName);

        ObjectNode specialties = mapper.createObjectNode();
        for (int specialtyId : specialtyIds) {
            ObjectNode spec = mapper.createObjectNode();
            spec.put("id", specialtyId);
            specialties.set(String.valueOf(specialtyId), spec);
        }
        vet.set("specialties", specialties);

        Response response = RestAssured.given()
                .auth().basic(Constants.ADMIN_USERNAME, Constants.ADMIN_PASSWORD)
                .contentType(ContentType.JSON)
                .body(vet.toString())
                .post(Constants.BASE_URL + Constants.VETS_ENDPOINT);

        assertEquals(Constants.CREATED_CODE, response.getStatusCode());
        return response.jsonPath().getInt("id");
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new RuntimeException("Expected status code " + expected + " but got " + actual);
        }
    }
}
