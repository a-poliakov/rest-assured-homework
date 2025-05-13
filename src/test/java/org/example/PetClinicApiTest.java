package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.utils.Constants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class PetClinicApiTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = Constants.BASE_URL;
        RestAssured.authentication = RestAssured.basic(Constants.ADMIN_USERNAME, Constants.ADMIN_PASSWORD);
        TestDataSetup.setupTestData();
    }

    @Test
    void positiveScenario() throws Exception {
        // 1. Получаем информацию о владельце
        Response ownerResponse = RestAssured.given()
                .get(Constants.OWNERS_ENDPOINT + "/" + TestDataSetup.owner1Id);
        assertEquals(Constants.SUCCESS_CODE, ownerResponse.getStatusCode());
        ownerResponse.then()
                .body("id", equalTo(TestDataSetup.owner1Id))
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"));

        // 2. Получаем список ветеринаров
        Response vetsResponse = RestAssured.given()
                .get(Constants.VETS_ENDPOINT);
        assertEquals(Constants.SUCCESS_CODE, vetsResponse.getStatusCode());
        vetsResponse.then().body("size()", greaterThanOrEqualTo(3));

        int vetId = vetsResponse.jsonPath().getInt("[0].id");
        String vetSpecialty = vetsResponse.jsonPath().getString("[0].specialties[0].name");

        // 3. Создаем визит для питомца
        String date = TestDataSetup.getCurrentDate();
        String description = vetSpecialty + " " + vetId;

        ObjectNode visit = mapper.createObjectNode();
        visit.put("date", date);
        visit.put("description", description);
        visit.put("petId", TestDataSetup.pet1Id);

        Response visitResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(visit.toString())
                .post(Constants.VISITS_ENDPOINT);
        assertEquals(Constants.CREATED_CODE, visitResponse.getStatusCode());
        int visitId = visitResponse.jsonPath().getInt("id");

        // 4. Проверяем созданный визит
        Response getVisitResponse = RestAssured.given()
                .get(Constants.VISITS_ENDPOINT + "/" + visitId);
        assertEquals(Constants.SUCCESS_CODE, getVisitResponse.getStatusCode());
        getVisitResponse.then()
                .body("date", equalTo(date))
                .body("description", equalTo(description))
                .body("petId", equalTo(TestDataSetup.pet1Id));

        // 5. Проверяем, что визит появился в списке визитов питомца
        Response petVisitsResponse = RestAssured.given()
                .get(Constants.PETS_ENDPOINT + "/" + TestDataSetup.pet1Id + "/visits");
        assertEquals(Constants.SUCCESS_CODE, petVisitsResponse.getStatusCode());
        petVisitsResponse.then()
                .body("find { it.id == " + visitId + " }.description", equalTo(description));
    }

    @Test
    void negativeScenario_nonExistingPet() {
        // 1. Пытаемся получить визиты для несуществующего питомца
        int nonExistingPetId = 9999;
        Response response = RestAssured.given()
                .get(Constants.PETS_ENDPOINT + "/" + nonExistingPetId + "/visits");
        assertEquals(Constants.NOT_FOUND_CODE, response.getStatusCode());

        // 2. Пытаемся создать визит для несуществующего питомца
        ObjectNode visit = mapper.createObjectNode();
        visit.put("date", TestDataSetup.getCurrentDate());
        visit.put("description", "Checkup");
        visit.put("petId", nonExistingPetId);

        Response createResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(visit.toString())
                .post(Constants.VISITS_ENDPOINT);
        assertEquals(Constants.NOT_FOUND_CODE, createResponse.getStatusCode());
    }

    @Test
    void negativeScenario_invalidVisitData() {
        // 1. Неверный формат даты
        ObjectNode invalidDateVisit = mapper.createObjectNode();
        invalidDateVisit.put("date", Constants.INVALID_DATE);
        invalidDateVisit.put("description", "Checkup");
        invalidDateVisit.put("petId", TestDataSetup.pet2Id);

        Response dateResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidDateVisit.toString())
                .post(Constants.VISITS_ENDPOINT);
        assertEquals(Constants.CLIENT_ERROR_CODE, dateResponse.getStatusCode());

        // 2. Пустое описание
        ObjectNode emptyDescVisit = mapper.createObjectNode();
        emptyDescVisit.put("date", TestDataSetup.getCurrentDate());
        emptyDescVisit.put("description", Constants.EMPTY_DESCRIPTION);
        emptyDescVisit.put("petId", TestDataSetup.pet2Id);

        Response descResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(emptyDescVisit.toString())
                .post(Constants.VISITS_ENDPOINT);
        assertEquals(Constants.CLIENT_ERROR_CODE, descResponse.getStatusCode());

        // 3. Отсутствует обязательное поле
        ObjectNode missingFieldVisit = mapper.createObjectNode();
        missingFieldVisit.put("date", TestDataSetup.getCurrentDate());
        missingFieldVisit.put("description", "Checkup");
        // Нет petId

        Response fieldResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(missingFieldVisit.toString())
                .post(Constants.VISITS_ENDPOINT);
        assertEquals(Constants.CLIENT_ERROR_CODE, fieldResponse.getStatusCode());
    }

    @Test
    void testGetAllOwners() {
        RestAssured.given()
                .get(Constants.OWNERS_ENDPOINT)
                .then()
                .statusCode(Constants.SUCCESS_CODE)
                .body("size()", greaterThanOrEqualTo(2))
                .body("find { it.id == " + TestDataSetup.owner1Id + " }.firstName", equalTo("John"))
                .body("find { it.id == " + TestDataSetup.owner2Id + " }.firstName", equalTo("Jane"));
    }

    @Test
    void testGetAllVets() {
        RestAssured.given()
                .get(Constants.VETS_ENDPOINT)
                .then()
                .statusCode(Constants.SUCCESS_CODE)
                .body("size()", greaterThanOrEqualTo(3))
                .body("find { it.id == " + TestDataSetup.vet1Id + " }.firstName", equalTo("James"))
                .body("find { it.id == " + TestDataSetup.vet2Id + " }.firstName", equalTo("Helen"));
    }
}