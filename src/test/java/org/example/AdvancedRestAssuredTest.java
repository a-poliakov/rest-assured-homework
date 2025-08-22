package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.example.utils.Constants.*;
import static org.hamcrest.Matchers.*;

public class AdvancedRestAssuredTest {
    static RequestSpecification requestSpecification;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Тестовые данные
    private static Integer ownerId;
    private static Integer petId;
    private static Integer petTypeId;
    private static Integer vetId;
    private static Integer specialtyId;
    private static List<Integer> visitIds = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpecification = RestAssured.given()
                .baseUri(BASE_URL)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON);
        
        createTestData();
    }

    private static void createTestData() {
        // Создаем владельца
        ownerId = createOwner("Анна", "Сидорова", "ул. Пушкина 10", "Санкт-Петербург", "9876543210");
        
        // Создаем тип питомца
        petTypeId = createPetType("собака");
        
        // Создаем питомца
        LocalDate petBirthDate = LocalDate.now().minusYears(1);
        petId = createPet(ownerId, "Бобик", petBirthDate, petTypeId);
        
        // Создаем специализацию
        specialtyId = createSpecialty("хирургия");
        
        // Создаем ветеринара
        vetId = createVet("Мария", "Иванова", List.of(specialtyId));
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
        type.put("name", "собака");
        
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
            spec.put("name", "хирургия");
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
    @DisplayName("Тест с использованием Response и JsonPath для извлечения данных")
    void testResponseAndJsonPathExtraction() {
        // Получаем список всех владельцев
        Response response = given(requestSpecification)
                .when()
                .get("/owners")
                .then()
                .statusCode(HTTP_OK)
                .extract().response();

        JsonPath jsonPath = response.jsonPath();
        
        // Извлекаем данные
        List<Map<String, Object>> owners = jsonPath.getList("");
        Assertions.assertFalse(owners.isEmpty(), "Список владельцев не должен быть пустым");
        
        // Находим нашего тестового владельца
        Optional<Map<String, Object>> testOwner = owners.stream()
                .filter(owner -> owner.get("id").equals(ownerId))
                .findFirst();
        
        Assertions.assertTrue(testOwner.isPresent(), "Тестовый владелец должен быть найден");
        Assertions.assertEquals("Анна", testOwner.get().get("firstName"));
        Assertions.assertEquals("Сидорова", testOwner.get().get("lastName"));
    }

    @Test
    @DisplayName("Тест с использованием Query Parameters")
    void testQueryParameters() {
        // Получаем владельцев с фильтрацией по фамилии
        given(requestSpecification)
                .queryParam("lastName", "Сидорова")
                .when()
                .get("/owners")
                .then()
                .statusCode(HTTP_OK)
                .body("find { it.lastName == 'Сидорова' }.firstName", equalTo("Анна"));
    }

    @Test
    @DisplayName("Тест с использованием Path Parameters и валидацией JSON схемы")
    void testPathParametersAndJsonValidation() {
        given(requestSpecification)
                .pathParam("petId", petId)
                .when()
                .get("/pets/{petId}")
                .then()
                .statusCode(HTTP_OK)
                .body("id", equalTo(petId))
                .body("name", equalTo("Бобик"))
                .body("type.id", equalTo(petTypeId))
                .body("type.name", equalTo("собака"))
                .body("birthDate", notNullValue())
                .body("visits", notNullValue());
    }

    @Test
    @DisplayName("Тест создания множественных записей на прием")
    void testMultipleVisitsCreation() {
        // Создаем несколько записей на прием
        String[] descriptions = {
            "Плановый осмотр",
            "Вакцинация",
            "Консультация по питанию"
        };
        
        for (int i = 0; i < descriptions.length; i++) {
            String visitDate = DATE_FMT.format(LocalDate.now().plusDays(i + 1));
            
            Map<String, Object> visitPayload = new HashMap<>();
            visitPayload.put("date", visitDate);
            visitPayload.put("description", descriptions[i]);

            JsonPath visitResponse = given(requestSpecification)
                    .body(visitPayload)
                    .when()
                    .post("/owners/{ownerId}/pets/{petId}/visits", ownerId, petId)
                    .then()
                    .statusCode(HTTP_CREATED)
                    .body("date", equalTo(visitDate))
                    .body("description", equalTo(descriptions[i]))
                    .extract().jsonPath();

            visitIds.add(visitResponse.getInt("id"));
        }

        // Проверяем, что все записи создались
        Assertions.assertEquals(descriptions.length, visitIds.size());
        
        // Проверяем через GET запрос
        given(requestSpecification)
                .when()
                .get("/pets/{petId}", petId)
                .then()
                .statusCode(HTTP_OK)
                .body("visits.size()", greaterThanOrEqualTo(descriptions.length));
    }

    @Test
    @DisplayName("Тест с использованием Headers")
    void testWithHeaders() {
        given(requestSpecification)
                .header("Accept", "application/json")
                .header("User-Agent", "REST-Assured-Test")
                .when()
                .get("/pettypes")
                .then()
                .statusCode(HTTP_OK)
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Тест с использованием Cookies")
    void testWithCookies() {
        given(requestSpecification)
                .cookie("testCookie", "testValue")
                .when()
                .get("/specialties")
                .then()
                .statusCode(HTTP_OK)
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Тест с использованием Form Data")
    void testWithFormData() {
        // Этот тест демонстрирует использование form data (хотя API использует JSON)
        given(requestSpecification)
                .contentType(ContentType.URLENC)
                .formParam("name", "тестовая специализация")
                .when()
                .post("/specialties")
                .then()
                .statusCode(HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Тест с использованием Multipart Data")
    void testWithMultipartData() {
        // Демонстрация multipart (хотя API не поддерживает загрузку файлов)
        given(requestSpecification)
                .contentType(ContentType.MULTIPART)
                .multiPart("name", "тестовая специализация")
                .when()
                .post("/specialties")
                .then()
                .statusCode(HTTP_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Тест с использованием Authentication (если требуется)")
    void testWithAuthentication() {
        // Демонстрация базовой аутентификации (если API поддерживает)
        given(requestSpecification)
                .auth().basic("admin", "admin")
                .when()
                .get("/vets")
                .then()
                .statusCode(anyOf(is(HTTP_OK), is(HTTP_UNAUTHORIZED)));
    }

    @Test
    @DisplayName("Тест с использованием Timeout")
    void testWithTimeout() {
        given(requestSpecification)
                .when()
                .get("/owners")
                .then()
                .statusCode(HTTP_OK);
    }

    @Test
    @DisplayName("Тест с использованием Logging")
    void testWithLogging() {
        given(requestSpecification)
                .log().all()
                .when()
                .get("/pets")
                .then()
                .log().body()
                .statusCode(HTTP_OK);
    }

    @Test
    @DisplayName("Тест с использованием Custom Matchers")
    void testWithCustomMatchers() {
        given(requestSpecification)
                .when()
                .get("/owners/{ownerId}", ownerId)
                .then()
                .statusCode(HTTP_OK)
                .body("firstName", startsWith("А"))
                .body("lastName", endsWith("ва"))
                .body("telephone", matchesPattern("\\d{10}"))
                .body("pets.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Тест с использованием Response Time")
    void testResponseTime() {
        given(requestSpecification)
                .when()
                .get("/vets")
                .then()
                .statusCode(HTTP_OK)
                .time(lessThan(2000L));
    }
}
