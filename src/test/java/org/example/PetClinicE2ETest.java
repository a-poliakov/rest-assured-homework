package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.example.utils.Constants.BASE_URL;
import static org.example.utils.Constants.HTTP_CREATED;
import static org.example.utils.Constants.HTTP_NOT_FOUND;
import static org.example.utils.Constants.HTTP_OK;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

class PetClinicE2ETest {

    private static RequestSpecification spec;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static Integer userAdminId;
    private static String adminUsername;

    private static Integer owner1Id;
    private static Integer owner2Id;

    private static final List<Integer> petTypeIds = new ArrayList<>();
    private static final Map<Integer, String> petTypeIdToName = new HashMap<>();
    private static final Map<Integer, Integer> petIdToOwnerId = new LinkedHashMap<>();

    private static Integer specialty1Id;
    private static Integer specialty2Id;
    private static final Map<Integer, String> specialtyIdToName = new HashMap<>();

    private static Integer vet1Id;
    private static Integer vet2Id;
    private static Integer vet3Id;

    @BeforeAll
    static void beforeAll() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        spec = RestAssured.given()
                .baseUri(BASE_URL)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON);

        seedData();
    }

    private static void seedData() {
        adminUsername = "admin_" + System.currentTimeMillis();
        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", adminUsername);
        userPayload.put("password", "password");
        userPayload.put("enabled", true);
        userPayload.put("roles", List.of(Map.of("name", "admin")));

        given(spec)
                .body(userPayload)
                .when()
                .post("/users")
                .then()
                .statusCode(anyOf(is(HTTP_OK), is(HTTP_CREATED)));

        owner1Id = createOwner("John", "Doe", "101 Main St", "Metropolis", "1234567890");
        owner2Id = createOwner("Jane", "Smith", "202 Oak St", "Gotham", "0987654321");

        petTypeIds.add(createPetType("cat"));
        petTypeIds.add(createPetType("dog"));
        petTypeIds.add(createPetType("lizard"));

        LocalDate dob = LocalDate.now().minusYears(2);
        Integer pet1 = createPet(owner1Id, "Tom", dob, petTypeIds.get(0));
        Integer pet2 = createPet(owner1Id, "Rex", dob.minusMonths(2), petTypeIds.get(1));
        Integer pet3 = createPet(owner1Id, "Iggy", dob.minusMonths(6), petTypeIds.get(2));
        Integer pet4 = createPet(owner2Id, "Bella", dob.minusYears(1), petTypeIds.get(1));
        Integer pet5 = createPet(owner2Id, "Luna", dob.minusDays(30), petTypeIds.get(0));

        petIdToOwnerId.put(pet1, owner1Id);
        petIdToOwnerId.put(pet2, owner1Id);
        petIdToOwnerId.put(pet3, owner1Id);
        petIdToOwnerId.put(pet4, owner2Id);
        petIdToOwnerId.put(pet5, owner2Id);

        specialty1Id = createSpecialty("radiology");
        specialty2Id = createSpecialty("surgery");

        vet1Id = createVet("James", "Carter", List.of(specialty1Id));
        vet2Id = createVet("Helen", "Leary", List.of(specialty2Id));
        vet3Id = createVet("Sam", "Fisher", List.of(specialty1Id, specialty2Id));
    }

    private static Integer createOwner(String firstName, String lastName, String address, String city, String telephone) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("address", address);
        body.put("city", city);
        body.put("telephone", telephone);
        JsonPath jp = given(spec)
                .body(body)
                .when()
                .post("/owners")
                .then()
                .statusCode(201)
                .extract().jsonPath();
        return jp.getInt("id");
    }

    private static Integer createPetType(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        JsonPath jp = given(spec)
                .body(body)
                .when()
                .post("/pettypes")
                .then()
                .statusCode(anyOf(is(HTTP_OK), is(HTTP_CREATED)))
                .extract().jsonPath();
        Integer id = jp.getInt("id");
        petTypeIdToName.put(id, name);
        return id;
    }

    private static Integer createPet(Integer ownerId, String name, LocalDate birthDate, Integer petTypeId) {
        Map<String, Object> type = new HashMap<>();
        type.put("id", petTypeId);
        type.put("name", petTypeIdToName.get(petTypeId));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("birthDate", DATE_FMT.format(birthDate));
        body.put("type", type);
        JsonPath jp = given(spec)
                .body(body)
                .when()
                .post("/owners/{ownerId}/pets", ownerId)
                .then()
                .statusCode(201)
                .extract().jsonPath();
        return jp.getInt("id");
    }

    private static Integer createSpecialty(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        JsonPath jp = given(spec)
                .body(body)
                .when()
                .post("/specialties")
                .then()
                .statusCode(anyOf(is(HTTP_OK), is(HTTP_CREATED)))
                .extract().jsonPath();
        Integer id = jp.getInt("id");
        specialtyIdToName.put(id, name);
        return id;
    }

    private static Integer createVet(String firstName, String lastName, List<Integer> specialtyIds) {
        List<Map<String, Object>> specialties = new ArrayList<>();
        for (Integer id : specialtyIds) {
            Map<String, Object> spec = new HashMap<>();
            spec.put("id", id);
            spec.put("name", specialtyIdToName.get(id));
            specialties.add(spec);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("specialties", specialties);
        JsonPath jp = given(spec)
                .body(body)
                .when()
                .post("/vets")
                .then()
                .statusCode(anyOf(is(HTTP_OK), is(HTTP_CREATED)))
                .extract().jsonPath();
        return jp.getInt("id");
    }

    @Test
    void positive_scenario_create_visits_and_verify() {
        Integer chosenOwnerId = owner1Id;
        List<Integer> chosenPets = petIdToOwnerId.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue(), chosenOwnerId))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        var vets = given(spec)
                .when()
                .get("/vets")
                .then()
                .statusCode(HTTP_OK)
                .extract().jsonPath().getList("", Map.class);

        Map<Integer, String> vetIdToSpecName = new LinkedHashMap<>();
        for (Object o : vets) {
            @SuppressWarnings("unchecked")
            Map<String, Object> v = (Map<String, Object>) o;
            Integer id = (Integer) v.get("id");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> specs = (List<Map<String, Object>>) v.get("specialties");
            if (specs != null && !specs.isEmpty()) {
                // choose first specialty name
                String name = String.valueOf(specs.get(0).get("name"));
                vetIdToSpecName.put(id, name);
            }
        }

        Assertions.assertFalse(vetIdToSpecName.isEmpty(), "No vets with specialties available");

        String date = DATE_FMT.format(LocalDate.now());
        List<Integer> createdVisitIds = new ArrayList<>();
        int idx = 0;
        for (Integer petId : chosenPets) {
            Integer vetId = vetIdToSpecName.keySet().stream().toList().get(idx % vetIdToSpecName.size());
            String specName = vetIdToSpecName.get(vetId);
            String description = specName + " " + vetId;

            JsonPath jp = given(spec)
                    .body(Map.of("date", date, "description", description))
                    .when()
                    .post("/owners/{ownerId}/pets/{petId}/visits", chosenOwnerId, petId)
                    .then()
                    .statusCode(HTTP_CREATED)
                    .extract().jsonPath();

            Integer visitId = jp.getInt("id");
            createdVisitIds.add(visitId);

            // verify via GET /pets/{petId}
            given(spec)
                    .when()
                    .get("/pets/{petId}", petId)
                    .then()
                    .statusCode(HTTP_OK)
                    .body("visits.find { it.id == %s }.description".formatted(visitId), is(description))
                    .body("visits.find { it.id == %s }.date".formatted(visitId), is(date));

            idx++;
        }

        Assertions.assertEquals(chosenPets.size(), createdVisitIds.size());
    }

    @Test
    void negative_scenario_get_visits_for_nonexistent_pet() {
        int nonExistentPetId = 99999999;
        given(spec)
                .when()
                .get("/pets/{petId}", nonExistentPetId)
                .then()
                .statusCode(HTTP_NOT_FOUND);
    }
}


