package org.example;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.example.dto.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.example.utils.Constants.*;
import static org.hamcrest.Matchers.*;

public class MainTest {
    @BeforeAll
    static void setUp() {
        // Создаём через Builder, как в лекции учили
        Role role = Role.builder()
                .name("admin")
                .build();
        User user = User.builder()
                .username("testadmin")
                .password("123456")
                .enabled(true)
                .roles(new ArrayList<>(Arrays.asList(role)))
                .build();
        // Далее создаём через конструкторы, потому как короче и нагляднее чем через Builder
        List<OwnerToAdd> ownersToAdd = new ArrayList<>(
                Arrays.asList(new OwnerToAdd("Name-one", "GigSF-one", "110", "Moscow", "6085551023"),
                        new OwnerToAdd("Name-one", "GigSF-two", "110", "Moscow", "6085551023")));
        List<SpecialtyToAdd> specialtiesToAdd = new ArrayList<>(
                Arrays.asList(new SpecialtyToAdd("orthopedics"),
                        new SpecialtyToAdd("cardiology")));
        List<PettypesToAdd> pettypesToAdd = new ArrayList<>(
                Arrays.asList(new PettypesToAdd("rabbit"),
                        new PettypesToAdd("meerkat"),
                        new PettypesToAdd("turtle")));
        List<VetToAdd> vetsToAdd = new ArrayList<>(
                Arrays.asList(new VetToAdd("NameVet-one", "Vet-one", new ArrayList<>(Arrays.asList(specialtiesToAdd.get(0)))),
                        new VetToAdd("NameVet-two", "Vet-two", new ArrayList<>(Arrays.asList(specialtiesToAdd.get(1)))),
                        new VetToAdd("NameVet-three", "Vet-three", new ArrayList<>(Arrays.asList(specialtiesToAdd.get(0), specialtiesToAdd.get(1))))
                ));

        // 1. Добавить одного пользователя с ролью admin
        Response responseUserAdd = requestSpecification()
                .given()
                .with().body(user)
                .when()
                .post("/petclinic/api/users");
        // Проверка кода возврата "вынесена" из RequestSpecification для формирования анализируемого сообщения
        Assertions.assertTrue((CREATE_CODE == responseUserAdd.statusCode() || NOTFOUND_CODE == responseUserAdd.statusCode()), "Не смог создать|найти пользователя " + user.getUsername());

        // 2. Добавить 2 владельцев питомцев
        Map<String, Integer> mapOwners = new HashMap<>();
        for (OwnerToAdd owner : ownersToAdd) {
            ExtractableResponse<Response> postResponse = requestSpecification()
                    .given()
                    .with().body(owner)
                    .when()
                    .post("petclinic/api/owners")
                    .then()
                    .extract();
            // Проверка кода возврата "вынесена" из RequestSpecification для формирования анализируемого сообщения
            Assertions.assertEquals(CREATE_CODE, postResponse.statusCode(), "Не смог создать владельца " + owner.getLastName());
            JsonPath jsonPath = postResponse.jsonPath();
            mapOwners.put(owner.getLastName(), jsonPath.getInt("id"));
        }

        // 3. Добавить 3 вида питомцев
        Map<String, Integer> mapPettypes = new HashMap<>();
        for (PettypesToAdd pettype : pettypesToAdd) {
            ExtractableResponse<Response> postResponse = requestSpecification()
                    .given()
                    .with().body(pettype)
                    .when()
                    .post("petclinic/api/pettypes")
                    .then()
                    .extract();
            Assertions.assertEquals(CREATE_CODE, postResponse.statusCode(), "Не смог создать вид питомцев " + pettype.getName());
            JsonPath jsonPath = postResponse.jsonPath();
            mapPettypes.put(pettype.getName(), jsonPath.getInt("id"));
        }

        // 4. Добавить 5 питомцев
        List<PetToAdd> petsToAdd = new ArrayList<>(
                Arrays.asList(new PetToAdd("Meerkat-one", "2024-01-01", new PettypesFull("meerkat", (int) mapPettypes.get("meerkat"))),
                        new PetToAdd("Meerkat-two", "2024-02-01", new PettypesFull("meerkat", (int) mapPettypes.get("meerkat"))),
                        new PetToAdd("Turtle-one", "2024-03-01", new PettypesFull("turtle", (int) mapPettypes.get("turtle"))),
                        new PetToAdd("Turtle-two", "2024-04-01", new PettypesFull("turtle", (int) mapPettypes.get("turtle"))),
                        new PetToAdd("Rabbit-one", "2024-05-01", new PettypesFull("rabbit", (int) mapPettypes.get("rabbit")))
                ));
        int ownerId = mapOwners.get("GigSF-one"); // Первых три животных - для GigSF-one
        for (int i = 0; i < 5; i++) {
            if (i == 3)
                ownerId = mapOwners.get("GigSF-two");// Остальные два животных - для GigSF-two
            Response responsePetsAdd = requestSpecification()
                    .given()
                    .with().body(petsToAdd.get(i))
                    .pathParam("ownerId", ownerId)
                    .when()
                    .post("/petclinic/api/owners/{ownerId}/pets");
            Assertions.assertEquals(CREATE_CODE, responsePetsAdd.statusCode(), "Не смог добавить питомца " + petsToAdd.get(i).getName());
        }

        // 5. Создать 2 специализации ветеринаров
        for (SpecialtyToAdd specialty : specialtiesToAdd) {
            Response responseSpecialtyAdd = requestSpecification()
                    .given()
                    .with().body(specialty)
                    .when()
                    .post("/petclinic/api/specialties");
            Assertions.assertEquals(CREATE_CODE, responseSpecialtyAdd.statusCode(), "Не смог создать специализацию ветеринаров " + specialty.getName());
        }

        // 6. Создать 3 ветеринара
        for (VetToAdd vet : vetsToAdd) {
            Response responseSpecialtyAdd = requestSpecification()
                    .given()
                    .with().body(vet)
                    .when()
                    .post("/petclinic/api/vets");
            Assertions.assertEquals(CREATE_CODE, responseSpecialtyAdd.statusCode(), "Не смог создать ветеринара " + vet.getLastName());
        }
    }

    public static RequestSpecification requestSpecification() { // Сменил со статического объекта на пересоздаваемый по рекомендациям из интернет (невозможно убрать pathParam после установки в статическом объекте)
        RequestSpecification requestSpecification;
        requestSpecification = RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .accept("application/json");
        return requestSpecification;
    }

    ///// Основной сценарий
    /*
    Владелец GigSF-one хочет записать своих питомцев Meerkat-one и Turtle-one на приём к ветеринарам,
    причём у Meerkat-one проблемы с orthopedics и владелец собирается свозить его в клинику 1-го сентября, а у Turtle-one - с cardiology, и владелец
    собирается съездить с ним в клинику 3-го сентября.
    При этом владельцу рекомендовали Vet-two и Vet-one как хороших ветеринаров, но при рекомендации не указали их специализации.
     */
    @Test
    @Tag("basic")
    @DisplayName("Проверка основного сценария создания визитов")
    void testCreateVisits() {
        // Определяем начальные переменные для тестов
        String lactNameOwner = "GigSF-one"; // фамилия владельца
        String namePet01 = "Meerkat-one"; // кличка питомца 1
        String namePet02 = "Turtle-one"; // кличка питомца 2
        String specialty01 = "orthopedics"; // проблема питомца 1
        String specialty02 = "cardiology"; // проблема питомца 2
        List<String> lastNameFavoriteVets = new ArrayList<>(Arrays.asList("Vet-two", "Vet-one")); // Фамилии рекомендованных ветеринаров
        VisitToAdd visit01 = new VisitToAdd("2025-09-01", "specialty01 + idVet01");
        VisitToAdd visit02 = new VisitToAdd("2025-09-03", "specialty02 + idVet02");
        // 01 выбираем владельца питомца по фамилии из общего списка (берём с наибольшим ID)
        ExtractableResponse<Response> postResponseOwner = requestSpecification()
                .given()
                .pathParam("lastName", lactNameOwner)
                .when()
                .get("/petclinic/api/owners?lastName={lastName}")
                .then()
                .extract();
        // Проверка кода возврата "вынесена" из RequestSpecification для формирования анализируемого сообщения
        Assertions.assertEquals(SUCCESS_CODE, postResponseOwner.statusCode(), "Не смог найти владельца " + lactNameOwner);
        JsonPath jsonPathOwner = postResponseOwner.jsonPath();
        int countOwner = jsonPathOwner.getInt("size()");
        Assertions.assertTrue(countOwner >= 1, "Не нашёл владельца " + lactNameOwner);
        int ownerId = -1;
        ownerId = jsonPathOwner.getInt("id[" + (countOwner - 1) + "]");
        // 02 Выбираем двух питомцев по кличкам (у выбранного владельца)
        ExtractableResponse<Response> postResponsePet = requestSpecification()
                .given()
                .pathParam("ownerId", ownerId)
                .when()
                .get("/petclinic/api/owners/{ownerId}")
                .then()
                .statusCode(SUCCESS_CODE)
                .extract();
        JsonPath jsonPathPet = postResponsePet.jsonPath();
        int sizePet = jsonPathPet.getInt("pets.size()");
        int petId01 = -1;
        int petId02 = -1;
        for (int i = 0; i < sizePet; i++) {
            if (jsonPathPet.getString("pets.name[" + i + "]").equals(namePet01))
                petId01 = jsonPathPet.getInt("pets.id[" + i + "]");
            if (jsonPathPet.getString("pets.name[" + i + "]").equals(namePet02))
                petId02 = jsonPathPet.getInt("pets.id[" + i + "]");
        }
        // 03 Выбираем ветеринаров по специализации, из них по фамилии
        Map<String, Integer> mapVet01 = new HashMap<>(); // ветеринары для специализации 1
        Map<String, Integer> mapVet02 = new HashMap<>(); // ветеринары для специализации 2
        ExtractableResponse<Response> postResponseVet = requestSpecification()
                .given()
                .when()
                .get("/petclinic/api/vets")
                .then()
                .extract();
        Assertions.assertEquals(SUCCESS_CODE, postResponseVet.statusCode(), "Не смог получить список ветеринаров");
        JsonPath jsonPathVet = postResponseVet.jsonPath();
        int sizeVet = jsonPathVet.getInt("size()");
        for (int i = 0; i < sizeVet; i++) {
            if (jsonPathVet.getJsonObject("specialties[" + i + "]") != null) {
                Integer sizeSpeciaty = jsonPathVet.getInt("specialties[" + i + "].size()");
                for (int ii = 0; ii < sizeSpeciaty; ii++) {
                    if (jsonPathVet.getString("specialties[" + i + "].name[" + ii + "]").equals(specialty01))
                        mapVet01.put(jsonPathVet.getString("lastName[" + i + "]"), jsonPathVet.getInt("id[" + i + "]"));
                    if (jsonPathVet.getString("specialties[" + i + "].name[" + ii + "]").equals(specialty02))
                        mapVet02.put(jsonPathVet.getString("lastName[" + i + "]"), jsonPathVet.getInt("id[" + i + "]"));
                }
            }
        }
        // Выбор по фамилии из предпочитаемых ветеринаров
        int vetId01 = -1; // для id ветеринара для первого питомца (orthopedics)
        int vetId02 = -1; // для id ветеринара для второго питомца (cardiology)
        for (String lastName : lastNameFavoriteVets) {
            if (mapVet01.get(lastName) != null) {
                vetId01 = mapVet01.get(lastName);
            }
            if (mapVet02.get(lastName) != null) {
                vetId02 = mapVet02.get(lastName);
            }
        }
        // 04 Создаём визит
        int visitId01 = -1;
        int visitId02 = -1;
        for (int i = 1; i <= 2; i++) {
            VisitToAdd visit;
            int vetId = -1;
            int petId = -1;
            String specialty = "";
            if (i == 1) {
                visit = visit01;
                vetId = vetId01;
                petId = petId01;
                specialty = specialty01;
            } else {
                visit = visit02;
                vetId = vetId02;
                petId = petId02;
                specialty = specialty02;
            }
            visit.setDescription(specialty + " " + vetId);
            ExtractableResponse<Response> postResponseVisit = requestSpecification()
                    .given()
                    .with().body(visit)
                    .pathParam("ownerId", ownerId)
                    .pathParam("petId", petId)
                    .when()
                    .post("/petclinic/api/owners/{ownerId}/pets/{petId}/visits")
                    .then()
                    .statusCode(CREATE_CODE)
                    .extract();
            JsonPath jsonPathVisit = postResponseVisit.jsonPath();
            if (i == 1) {
                visitId01 = jsonPathVisit.getInt("id");
            } else {
                visitId02 = jsonPathVisit.getInt("id");
            }
        }
        // 05 Проверяем на наличие в общем списке обоих созданных визитов
        Map<String, Object> expectedVisit01 = new HashMap<String, Object>();
        Map<String, Object> expectedVisit02 = new HashMap<String, Object>();
        for (int i = 1; i <= 2; i++) {
            VisitToAdd visit;
            int vetId = -1;
            int petId = -1;
            String specialty = "";
            if (i == 1) {
                expectedVisit01.put("date", visit01.getDate());
                expectedVisit01.put("petId", petId01);
                expectedVisit01.put("description", specialty01 + " " + vetId01);
                expectedVisit01.put("id", visitId01);
            } else {
                expectedVisit02.put("date", visit02.getDate());
                expectedVisit02.put("petId", petId02);
                expectedVisit02.put("description", specialty02 + " " + vetId02);
                expectedVisit02.put("id", visitId02);
            }
        }
        ExtractableResponse<Response> postResponseAllVisit = requestSpecification()
                .given()
                .when()
                .get("/petclinic/api/visits")
                .then()
                .assertThat()
                .body("$", Matchers.hasItem(expectedVisit01))
                .body("$", Matchers.hasItem(expectedVisit02))
                .extract();
        Assertions.assertEquals(SUCCESS_CODE, postResponseAllVisit.statusCode(), "Не смог получить список визитов");
    }

    @Test
    @Tag("Negative")
    @DisplayName("Негатив: Проверка наличия неcуществующего владельца")
    void noFindOwner() {
        String notExistingOwner = "notExistingOwner";
        ExtractableResponse<Response> postResponseOwner = requestSpecification()
                .given()
                .pathParam("lastName", notExistingOwner)
                .when()
                .get("/petclinic/api/owners?lastName={lastName}")
                .then()
                .extract();
        Assertions.assertEquals(NOTFOUND_CODE, postResponseOwner.statusCode(), "Найден несуществующий владелец ");
    }

    @Test
    @Tag("Negative")
    @DisplayName("Негатив: Создание визита для неcуществующего питомца")
    void visitNotExistingOwnerId() {
        VisitToAdd visit = new VisitToAdd("2025-09-01", "DEscription");
        int petId = 2147483647; // Не должно существовать в контрольном примере
        int ownerId = 1; // Должен существовать в контрольном примере
        ExtractableResponse<Response> postResponseVisit = requestSpecification()
                .given()
                .with().body(visit)
                .pathParam("ownerId", ownerId)
                .pathParam("petId", petId)
                .when()
                .post("/petclinic/api/owners/{ownerId}/pets/{petId}/visits")
                .then()
                .extract();
        Assertions.assertEquals(NOTFOUND_CODE, postResponseVisit.statusCode(), "Создался визит для несуществующего питомца");
    }

}
