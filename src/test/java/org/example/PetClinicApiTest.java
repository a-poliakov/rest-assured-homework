package org.example;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

class PetClinicApiTest {

    static RequestSpecification requestSpecification;

    static int owner1Id;
    static int owner2Id;
    static int petTypeDogId;
    static int petTypeCatId;
    static int petTypeBirdId;
    static int pet1Id;
    static int pet2Id;
    static int pet3Id;
    static int pet4Id;
    static int pet5Id;
    static int specSurgeryId;
    static int specDentistryId;
    static int vet1Id;
    static int vet2Id;
    static int vet3Id;

    @BeforeAll
    static void initData() {
        requestSpecification = given()
                .baseUri("http://localhost:9966/petclinic")
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON);

        // 1. owners
        owner1Id = requestSpecification
                .body(Map.of(
                        "firstName", "Ivan",
                        "lastName", "Ivanov",
                        "address", "Lenina 1",
                        "city", "Moscow",
                        "telephone", "1111111111"
                ))
                .when()
                .post("/api/owners")
                .then()
                .statusCode(201)
                .log().all()
                .extract().path("id");

        owner2Id = requestSpecification
                .body(Map.of(
                        "firstName", "Petr",
                        "lastName", "Petrov",
                        "address", "Lenina 2",
                        "city", "Moscow",
                        "telephone", "2222222222"
                ))
                .when()
                .post("/api/owners")
                .then()
                .statusCode(201)
                .log().all()
                .extract().path("id");

        // 2. pet types
        petTypeDogId = requestSpecification.body(Map.of("name", "dog"))
                .when().post("/api/pettypes")
                .then().statusCode(201).extract().path("id");
        petTypeCatId = requestSpecification.body(Map.of("name", "cat"))
                .when().post("/api/pettypes")
                .then().statusCode(201).extract().path("id");
        petTypeBirdId = requestSpecification.body(Map.of("name", "bird"))
                .when().post("/api/pettypes")
                .then().statusCode(201).extract().path("id");

        // 3. pets
        pet1Id = createPet(owner1Id, "Sharik", petTypeDogId, "dog");
        pet2Id = createPet(owner1Id, "Barsik", petTypeCatId, "cat");
        pet3Id = createPet(owner2Id, "Kesha", petTypeBirdId, "bird");
        pet4Id = createPet(owner2Id, "Bobik", petTypeDogId, "dog");
        pet5Id = createPet(owner2Id, "Murka", petTypeCatId, "cat");

        // 4. specialties
        specSurgeryId = requestSpecification.body(Map.of("name", "surgery"))
                .when().post("/api/specialties")
                .then().statusCode(201).extract().path("id");
        specDentistryId = requestSpecification.body(Map.of("name", "dentistry"))
                .when().post("/api/specialties")
                .then().statusCode(201).extract().path("id");

        // 5. vets
        vet1Id = requestSpecification.body(Map.of(
                        "firstName", "Dr",
                        "lastName", "House",
                        "specialtiesIds", List.of(specSurgeryId)
                ))
                .when().post("/api/vets")
                .then().statusCode(201).extract().path("id");

        vet2Id = requestSpecification.body(Map.of(
                        "firstName", "Dr",
                        "lastName", "Who",
                        "specialtiesIds", List.of(specDentistryId)
                ))
                .when().post("/api/vets")
                .then().statusCode(201).extract().path("id");

        vet3Id = requestSpecification.body(Map.of(
                        "firstName", "Dr",
                        "lastName", "Strange",
                        "specialtiesIds", List.of(specSurgeryId, specDentistryId)
                ))
                .when().post("/api/vets")
                .then()
                .log().all()
                .statusCode(201).extract().path("id");
    }

    static int createPet(int ownerId, String name, int petTypeId, String petTypeName) {
        return given()
                .spec(requestSpecification)
                .pathParam("ownerId", ownerId)
                .body(Map.of(
                        "name", name,
                        "birthDate", "2025-01-01",
                        "type", Map.of("id", petTypeId, "name", petTypeName)
                ))
                .when()
                .post("/api/owners/{ownerId}/pets")
                .then()
                .statusCode(201)
                .log().all()
                .extract().path("id");
    }

    @Test
    void positiveVisitScenario() {
        String date = "2025-11-30";
        String description = "surgery vetId=" + vet1Id;

        // 1. GET owner - НОВАЯ спецификация
        given().spec(requestSpecification)
                .pathParam("ownerId", owner1Id)
                .when().get("/api/owners/{ownerId}")
                .then().statusCode(200);

        // 2. GET vets
        given().spec(requestSpecification)
                .when().get("/api/vets")
                .then().statusCode(200)
                .body("id", hasItem(vet1Id));

        // 3. POST visit
        int visitId = given().spec(requestSpecification)
                .pathParam("ownerId", owner1Id)
                .pathParam("petId", pet1Id)
                .body(Map.of("date", date, "description", description))
                .when().post("/api/owners/{ownerId}/pets/{petId}/visits")
                .then().statusCode(201)
                .extract().path("id");

        // 4. GET visits
        given().spec(requestSpecification)
                .pathParam("ownerId", owner1Id)
                .pathParam("petId", pet1Id)
                .when().get("/api/owners/{ownerId}/pets/{petId}/visits")
                .then()
                .statusCode(200)
                .body("find { it.id == %s }.description", withArgs(visitId), equalTo(description))
                .body("find { it.id == %s }.date", withArgs(visitId), equalTo(date));
    }

    @Test
    void negativeVisitForNonExistingPet() {
        int nonExistingPetId = 999999;
        given().spec(requestSpecification)
                .pathParam("ownerId", owner1Id)
                .pathParam("petId", nonExistingPetId)
                .when().get("/api/owners/{ownerId}/pets/{petId}/visits")
                .then().statusCode(500);  // Сервер возвращает 500
    }
}

