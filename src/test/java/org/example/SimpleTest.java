package org.example;

import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.dto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.module.jsv.JsonSchemaValidatorSettings.settings;
import static org.example.utils.Constants.BASE_URL;
import static org.hamcrest.Matchers.*;

public class SimpleTest {
    static RequestSpecification requestSpecification;
    static List<Integer> ownerId = new ArrayList<>();
    static List<Integer> vetsId = new ArrayList<>();

    @BeforeAll
    static void setUp() {

        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.newBuilder()
                .setValidationConfiguration(ValidationConfiguration.newBuilder()
                        .setDefaultVersion(SchemaVersion.DRAFTV4).freeze()).freeze();

        JsonSchemaValidator.settings = settings().with().jsonSchemaFactory(jsonSchemaFactory).and().with().checkedValidation(false);

        requestSpecification = RestAssured.given()
                .baseUri(BASE_URL)
                .accept(ContentType.JSON);


        User user = User.builder().username("username").password("password").enabled(true).roles(List.of(UserRole.builder().name("admin").build())).build();

        requestSpecification.given()
                .with()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/petclinic/api/users")
                .then()
                .statusCode(201)
                .body("username", equalTo(("username")));

        List<Owner> ownerList = new ArrayList<>();

        Owner ownerFirst = Owner.builder().firstName("Marija").lastName("Shvetsova").address("Millionnaja str., 12 bld., app. 15")
                .city("Saint-Petersburg").telephone("7812123411").build();

        Owner ownerSecond = Owner.builder().firstName("Denis").lastName("Korablev").address("Millionnaja str., 12 bld., app. 17")
                .city("Saint-Petersburg").telephone("7812123789").build();


        ownerList.add(ownerFirst);
        ownerList.add(ownerSecond);

        for (int i = 0; i < ownerList.size(); i++) {
            ExtractableResponse<Response> postResponse =
                    requestSpecification.given()
                            .with()
                            .contentType(ContentType.JSON)
                            .body(ownerList.get(i))
                            .when()
                            .post("/petclinic/api/owners")
                            .then()
                            .statusCode(201).extract();

            postResponse.response().body();

            JsonPath jsonPath = postResponse.jsonPath();
            ownerId.add(jsonPath.get("id"));

        }

        List<PetType> petTypeList = new ArrayList<>();
        List<Integer> petTypeId = new ArrayList<>();

        PetType firstPetType = PetType.builder().name("cat").build();
        PetType secondPetType = PetType.builder().name("dog").build();
        PetType thirdPetType = PetType.builder().name("turtle").build();

        petTypeList.add(firstPetType);
        petTypeList.add(secondPetType);
        petTypeList.add(thirdPetType);

        for (int i = 0; i < petTypeList.size(); i++) {
            ExtractableResponse<Response> postResponse =
                    requestSpecification.given()
                            .with()
                            .contentType(ContentType.JSON)
                            .body(petTypeList.get(i))
                            .when()
                            .post("/petclinic/api/pettypes")
                            .then()
                            .statusCode(201).extract();

            postResponse.response().body();

            JsonPath jsonPath = postResponse.jsonPath();
            petTypeId.add(jsonPath.get("id"));

        }

        Specialty firstSpecialty = Specialty.builder().name("cat-doctor").build();
        Specialty secondSpecialty = Specialty.builder().name("dog-doctor").build();
        Specialty thirdSpecialty = Specialty.builder().name("turtle-doctor").build();

        List<Specialty> specialtyList = new ArrayList<>();
        List<Integer> specialtyId = new ArrayList<>();

        specialtyList.add(firstSpecialty);
        specialtyList.add(secondSpecialty);
        specialtyList.add(thirdSpecialty);

        for (int i = 0; i < specialtyList.size(); i++) {
            ExtractableResponse<Response> postResponse =
                    requestSpecification.given()
                            .with()
                            .contentType(ContentType.JSON)
                            .body(specialtyList.get(i))
                            .when()
                            .post("/petclinic/api/specialties")
                            .then()
                            .statusCode(201).extract();

            postResponse.response().body();

            JsonPath jsonPath = postResponse.jsonPath();
            specialtyId.add(jsonPath.get("id"));

        }

        List<Vet> vetsList = new ArrayList<>();


        Vet firstVet = Vet.builder().firstName("Ivan").lastName("Catdoctor").specialties(List.of(Specialty.builder().name("cat-doctor").build())).build();
        Vet secondVet = Vet.builder().firstName("Valeryi").lastName("Dogdoctor").specialties(List.of(Specialty.builder().name("dog-doctor").build())).build();
        Vet thirdVet = Vet.builder().firstName("Mikhail").lastName("Turtledoctor").specialties(List.of(Specialty.builder().name("turtle-doctor").build())).build();

        vetsList.add(firstVet);
        vetsList.add(secondVet);
        vetsList.add(thirdVet);

        for (int i = 0; i < vetsList.size(); i++) {
            ExtractableResponse<Response> postResponse =
                    requestSpecification.given()
                            .with()
                            .contentType(ContentType.JSON)
                            .body(vetsList.get(i))
                            .when()
                            .post("/petclinic/api/vets")
                            .then()
                            .statusCode(201).extract();

            postResponse.response().body();

            JsonPath jsonPath = postResponse.jsonPath();
            vetsId.add(jsonPath.get("id"));
        }


        Pet firstPet = Pet.builder().name("PussyCat").birthDate("2020-09-03").type(PetType.builder().name("cat").id(petTypeId.get(0)).build()).build();
        Pet secondPet = Pet.builder().name("PussyPuppy").birthDate("2020-09-03").type(PetType.builder().name("gog").id(petTypeId.get(1)).build()).build();
        Pet thirdPet = Pet.builder().name("MaskaCat").birthDate("2020-09-03").type(PetType.builder().name("cat").id(petTypeId.get(0)).build()).build();
        Pet fourthPet = Pet.builder().name("MaxDog").birthDate("2020-09-03").type(PetType.builder().name("dog").id(petTypeId.get(1)).build()).build();
        Pet fifthPet = Pet.builder().name("OldTurtle").birthDate("2020-09-03").type(PetType.builder().name("turtle").id(petTypeId.get(2)).build()).build();

        List<Pet> petsList = new ArrayList<>();
        petsList.add(firstPet);
        petsList.add(secondPet);
        petsList.add(thirdPet);
        petsList.add(fourthPet);
        petsList.add(fifthPet);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(secondPet)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(0) + "/pets")
                .then()
                .statusCode(201);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(fourthPet)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(0) + "/pets")
                .then()
                .statusCode(201);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(firstPet)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(1) + "/pets")
                .then()
                .statusCode(201);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(thirdPet)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(1) + "/pets")
                .then()
                .statusCode(201);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(fifthPet)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(1) + "/pets")
                .then()
                .statusCode(201);
    }

    @Test
    void testStub() {
        Assertions.assertTrue(true);
    }

    @Test
    void testConnect() {
        requestSpecification.given()
                .get("/petclinic/api/owners")
                .then()
                .statusCode(200);
    }

    @Test
    void testPositiveScenario() {

        // Запрашиваем информацию по пользователю Денис Кораблёв
        ExtractableResponse<Response> postResponseOwner =
                requestSpecification.given()
                        .get("/petclinic/api/owners/" + ownerId.get(1)) // 0 - Мария Швецова, 1 - Денис Кораблёв
                        .then()
                        .statusCode(200)
                        .body("firstName", equalTo(("Denis")))
                        .body("lastName", equalTo(("Korablev"))).extract();

        postResponseOwner.response().body();
        JsonPath jsonPathOwner = postResponseOwner.jsonPath();

        // Запрашиваем информацию о ветеринарах
        ExtractableResponse<Response> postResponseVets =
                requestSpecification.given()
                        .get("/petclinic/api/vets")
                        .then()
                        .statusCode(200).extract();

        postResponseVets.response().body();
        JsonPath jsonPathVets = postResponseVets.jsonPath();

        String visitDescription = "";

        // Выбираем животное для записи
        int pet = 0; // Выбираем одно из животных Дениса Кораблёва. 0 -кошка Маска; 1 - черепаха СтараяЧерепаха; 2 - кошка Кошечка.

        // Для кошки - кошачий доктор, для собаки - собачий и т.д.
        // Формируем описание визита
        if (jsonPathOwner.getList("pets.type.name").get(pet).equals("cat")) {
            visitDescription = jsonPathVets.getList("specialties.findAll{it.name =~ /ca.*/}.name").getFirst() + " + vetId:" + jsonPathVets.getObject("findAll{it.specialties.name =~ /ca.*/}.id", List.class).getFirst();
        }
        if (jsonPathOwner.getList("pets.type.name").get(pet).equals("dog")) {
            visitDescription = jsonPathVets.getList("specialties.findAll{it.name =~ /do.*/}.name").getFirst() + " + vetId:" + jsonPathVets.getObject("findAll{it.specialties.name =~ /do.*/}.id", List.class).getFirst();
        }
        if (jsonPathOwner.getList("pets.type.name").get(pet).equals("turtle")) {
            visitDescription = jsonPathVets.getList("specialties.findAll{it.name =~ /tu.*/}.name").getFirst() + " + vetId:" + jsonPathVets.getObject("findAll{it.specialties.name =~ /tu.*/}.id", List.class).getFirst();
        }
        Visit visit = Visit.builder().date("2024-10-16").description(visitDescription).build();

        // Получаем id питомца, которого будем записывать
        Integer petId = (Integer) jsonPathOwner.getList("pets.id").get(pet);

        // Осуществляем запись питомца
        ExtractableResponse<Response> postResponseVisit =
                requestSpecification.given()
                        .contentType(ContentType.JSON)
                        .body(visit)
                        .when()
                        .post("/petclinic/api/owners/" + ownerId.get(1) + "/pets/" + petId + "/visits")
                        .then()
                        .statusCode(201).extract();

        postResponseVisit.response().body();
        JsonPath jsonPathVisit = postResponseVisit.jsonPath();

        // Получаем id записи
        Integer visitId = jsonPathVisit.getInt("id");

        // Получаем запись по id. Проверяем соответствие.
        requestSpecification.given()
                .get("/petclinic/api/visits/" + visitId)
                .then()
                .statusCode(200)
                .body("date", equalTo(jsonPathVisit.getString("date")))
                .body("description", equalTo(jsonPathVisit.getString("description")))
                .body("id", equalTo(jsonPathVisit.getInt("id")))
                .body("petId", equalTo(jsonPathVisit.getInt("petId")));
    }

    @Test
    void testNegativeScenario() {

        int petId = 1722;
        Visit visit = Visit.builder().date("2024-10-16").description("visitDescription").build();
        int visitId = 1722;

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(visit)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(1) + "/pets/" + petId + "/visits")
                .then()
                .statusCode(404);

        requestSpecification.given()
                .get("/petclinic/api/visits/" + visitId)
                .then()
                .statusCode(404);
    }
}