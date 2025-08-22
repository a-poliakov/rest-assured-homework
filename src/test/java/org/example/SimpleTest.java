package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.example.utils.Constants.*;
import static org.hamcrest.Matchers.*;

public class SimpleTest {
    static RequestSpecification requestSpecification;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Тестовые данные
    private static Integer testOwnerId;
    private static Integer testPetId;
    private static Integer testPetTypeId;
    private static Integer testVetId;
    private static Integer testSpecialtyId;
    private static String testUsername;

    @BeforeAll
    static void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpecification = RestAssured.given()
                .baseUri(BASE_URL)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON);
        
        // Создаем тестовые данные перед всеми тестами
        createTestData();
    }

    private static void createTestData() {
        // Создаем пользователя
        testUsername = "testuser_" + System.currentTimeMillis();
        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", testUsername);
        userPayload.put("password", "testpass123");
        userPayload.put("enabled", true);
        userPayload.put("roles", List.of(Map.of("name", "owner")));

        given(requestSpecification)
                .body(userPayload)
                .when()
                .post("/users")
                .then()
                .statusCode(anyOf(is(HTTP_OK), is(HTTP_CREATED)));

        // Создаем владельца
        testOwnerId = createOwner("Иван", "Петров", "ул. Ленина 1", "Москва", "1234567890");
        
        // Создаем тип питомца
        testPetTypeId = createPetType("кот");
        
        // Создаем питомца
        LocalDate petBirthDate = LocalDate.now().minusYears(2);
        testPetId = createPet(testOwnerId, "Мурзик", petBirthDate, testPetTypeId);
        
        // Создаем специализацию ветеринара
        testSpecialtyId = createSpecialty("терапия");
        
        // Создаем ветеринара
        testVetId = createVet("Доктор", "Айболит", List.of(testSpecialtyId));
    }

    private static Integer createOwner(String firstName, String lastName, String address, String city, String telephone) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("address", address);
        body.put("city", city);
        body.put("telephone", telephone);
        
        JsonPath jp = given(requestSpecification)
                .body(body)
                .when()
                .post("/owners")
                .then()
                .statusCode(HTTP_CREATED)
                .extract().jsonPath();
        return jp.getInt("id");
    }

    private static Integer createPetType(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        
        JsonPath jp = given(requestSpecification)
                .body(body)
                .when()
                .post("/pettypes")
                .then()
                .statusCode(anyOf(is(HTTP_OK), is(HTTP_CREATED)))
                .extract().jsonPath();
        return jp.getInt("id");
    }

    private static Integer createPet(Integer ownerId, String name, LocalDate birthDate, Integer petTypeId) {
        Map<String, Object> type = new HashMap<>();
        type.put("id", petTypeId);
        type.put("name", "кот");
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("birthDate", DATE_FMT.format(birthDate));
        body.put("type", type);
        
        JsonPath jp = given(requestSpecification)
                .body(body)
                .when()
                .post("/owners/{ownerId}/pets", ownerId)
                .then()
                .statusCode(HTTP_CREATED)
                .extract().jsonPath();
        return jp.getInt("id");
    }

    private static Integer createSpecialty(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        
        JsonPath jp = given(requestSpecification)
                .body(body)
                .when()
                .post("/specialties")
                .then()
                .statusCode(anyOf(is(HTTP_OK), is(HTTP_CREATED)))
                .extract().jsonPath();
        return jp.getInt("id");
    }

    private static Integer createVet(String firstName, String lastName, List<Integer> specialtyIds) {
        List<Map<String, Object>> specialties = new ArrayList<>();
        for (Integer id : specialtyIds) {
            Map<String, Object> spec = new HashMap<>();
            spec.put("id", id);
            spec.put("name", "терапия");
            specialties.add(spec);
        }
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("specialties", specialties);
        
        JsonPath jp = given(requestSpecification)
                .body(body)
                .when()
                .post("/vets")
                .then()
                .statusCode(anyOf(is(HTTP_OK), is(HTTP_CREATED)))
                .extract().jsonPath();
        return jp.getInt("id");
    }

    @Test
    @DisplayName("Проверка подключения к API")
    void testConnect() {
        given(requestSpecification)
                .when()
                .get("/vets")
                .then()
                .statusCode(HTTP_OK)
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Полноценный позитивный сценарий: запись на прием")
    void positiveScenarioCreateVisit() {
        // Шаг 1: Проверяем, что питомец существует
        given(requestSpecification)
                .when()
                .get("/pets/{petId}", testPetId)
                .then()
                .statusCode(HTTP_OK)
                .body("id", equalTo(testPetId))
                .body("name", equalTo("Мурзик"));

        // Шаг 2: Создаем запись на прием
        String visitDate = DATE_FMT.format(LocalDate.now().plusDays(1));
        String visitDescription = "Плановый осмотр и вакцинация";
        
        Map<String, Object> visitPayload = new HashMap<>();
        visitPayload.put("date", visitDate);
        visitPayload.put("description", visitDescription);

        JsonPath visitResponse = given(requestSpecification)
                .body(visitPayload)
                .when()
                .post("/owners/{ownerId}/pets/{petId}/visits", testOwnerId, testPetId)
                .then()
                .statusCode(HTTP_CREATED)
                .body("id", notNullValue())
                .body("date", equalTo(visitDate))
                .body("description", equalTo(visitDescription))
                .extract().jsonPath();

        Integer visitId = visitResponse.getInt("id");

        // Шаг 3: Проверяем, что запись создалась через GET запрос
        given(requestSpecification)
                .when()
                .get("/pets/{petId}", testPetId)
                .then()
                .statusCode(HTTP_OK)
                .body("visits.find { it.id == " + visitId + " }.description", equalTo(visitDescription))
                .body("visits.find { it.id == " + visitId + " }.date", equalTo(visitDate));

        // Шаг 4: Проверяем запись через прямой GET запрос
        given(requestSpecification)
                .when()
                .get("/visits/{visitId}", visitId)
                .then()
                .statusCode(HTTP_OK)
                .body("id", equalTo(visitId))
                .body("date", equalTo(visitDate))
                .body("description", equalTo(visitDescription));
    }

    @Test
    @DisplayName("Негативный тест: попытка создать запись для несуществующего питомца")
    void negativeScenarioNonExistentPet() {
        int nonExistentPetId = 999999;
        String visitDate = DATE_FMT.format(LocalDate.now().plusDays(1));
        
        Map<String, Object> visitPayload = new HashMap<>();
        visitPayload.put("date", visitDate);
        visitPayload.put("description", "Тестовая запись");

        given(requestSpecification)
                .body(visitPayload)
                .when()
                .post("/owners/{ownerId}/pets/{petId}/visits", testOwnerId, nonExistentPetId)
                .then()
                .statusCode(HTTP_NOT_FOUND);
    }

    @Test
    @DisplayName("Получение списка всех ветеринаров")
    void testGetAllVets() {
        given(requestSpecification)
                .when()
                .get("/vets")
                .then()
                .statusCode(HTTP_OK)
                .body("size()", greaterThan(0))
                .body("find { it.id == " + testVetId + " }.firstName", equalTo("Доктор"))
                .body("find { it.id == " + testVetId + " }.lastName", equalTo("Айболит"));
    }

    @Test
    @DisplayName("Получение информации о владельце")
    void testGetOwner() {
        given(requestSpecification)
                .when()
                .get("/owners/{ownerId}", testOwnerId)
                .then()
                .statusCode(HTTP_OK)
                .body("id", equalTo(testOwnerId))
                .body("firstName", equalTo("Иван"))
                .body("lastName", equalTo("Петров"))
                .body("pets.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Получение информации о питомце")
    void testGetPet() {
        given(requestSpecification)
                .when()
                .get("/pets/{petId}", testPetId)
                .then()
                .statusCode(HTTP_OK)
                .body("id", equalTo(testPetId))
                .body("name", equalTo("Мурзик"))
                .body("type.name", equalTo("кот"));
    }
}
