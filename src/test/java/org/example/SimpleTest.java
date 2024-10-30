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
import static org.example.utils.Constants.SUCCESS_CODE;
import static org.hamcrest.Matchers.equalTo;

public class SimpleTest {
    static RequestSpecification requestSpecification;
    static List<Integer> ownerId = new ArrayList<>();
    static List<Integer> vetsId = new ArrayList<>();


    @BeforeAll
    static void setUp() {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.newBuilder()
                .setValidationConfiguration(ValidationConfiguration.newBuilder()
                        .setDefaultVersion(SchemaVersion.DRAFTV4).freeze()).freeze();
        JsonSchemaValidator.settings = settings().with().jsonSchemaFactory(jsonSchemaFactory)
                .and().with().checkedValidation(false);

        requestSpecification = RestAssured.given()
                .baseUri(BASE_URL)
                .accept(ContentType.JSON);

        User user = User.builder().username("nAdmin").password("password").enabled(true)
                .roles(List.of(UserRole.builder().name("admin").build())).build();

        requestSpecification.given()
                .with()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/petclinic/api/users")
                .then()
                .statusCode(201)
                .body("username", equalTo("nAdmin"));

        List<Owner> ownerList = new ArrayList<>();

        Owner owner1 = Owner.builder().firstName("Ivan").lastName("Ivanov").address("Nevskiy pr., 27 bld., app. 10")
                .city("Saint-Petersburg").telephone("7937123456").build();

        Owner owner2 = Owner.builder().firstName("Anna").lastName("Sverdlova").address("Shaymuratova str., 17 bld., app. 50")
                .city("Sterlitamak").telephone("7927654698").build();


        ownerList.add(owner1);
        ownerList.add(owner2);

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

        List<PetType> petTypes = new ArrayList<>();
        List<Integer> petTypesId = new ArrayList<>();

        PetType petType1 = PetType.builder().name("cat").build();
        PetType petType2 = PetType.builder().name("dog").build();
        PetType petType3 = PetType.builder().name("horse").build();

        petTypes.add(petType1);
        petTypes.add(petType2);
        petTypes.add(petType3);

        for (int i = 0; i < petTypes.size(); i++) {
            ExtractableResponse<Response> postResponse =
                    requestSpecification.given()
                            .with()
                            .contentType(ContentType.JSON)
                            .body(petTypes.get(i))
                            .when()
                            .post("/petclinic/api/pettypes")
                            .then()
                            .statusCode(201).extract();

            postResponse.response().body();

            JsonPath jsonPath = postResponse.jsonPath();
            petTypesId.add(jsonPath.get("id"));
        }

        Pet pushok = Pet.builder().name("pushok").birthDate("2021-03-05")
                .type(PetType.builder().name("cat").id(petTypesId.get(0)).build()).build();
        Pet murzik = Pet.builder().name("murzik").birthDate("2022-07-05")
                .type(PetType.builder().name("cat").id(petTypesId.get(0)).build()).build();
        Pet tuzik = Pet.builder().name("tuzik").birthDate("2019-03-03")
                .type(PetType.builder().name("cat").id(petTypesId.get(1)).build()).build();
        Pet rex = Pet.builder().name("rex").birthDate("2023-07-04")
                .type(PetType.builder().name("cat").id(petTypesId.get(1)).build()).build();
        Pet zvezdochka = Pet.builder().name("zvezdochka").birthDate("2020-10-10")
                .type(PetType.builder().name("cat").id(petTypesId.get(2)).build()).build();

        List<Pet> pets = new ArrayList<>();
        pets.add(pushok);
        pets.add(murzik);
        pets.add(tuzik);
        pets.add(rex);
        pets.add(zvezdochka);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(pushok)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(0) + "/pets")
                .then()
                .statusCode(201);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(murzik)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(0) + "/pets")
                .then()
                .statusCode(201);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(tuzik)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(1) + "/pets")
                .then()
                .statusCode(201);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(rex)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(1) + "/pets")
                .then()
                .statusCode(201);

        requestSpecification.given()
                .contentType(ContentType.JSON)
                .body(zvezdochka)
                .when()
                .post("/petclinic/api/owners/" + ownerId.get(0) + "/pets")
                .then()
                .statusCode(201);

        List<Speciality> specialityList = new ArrayList<>();
        List<Integer> specialityId = new ArrayList<>();

        Speciality catDoc = Speciality.builder().name("cat-doctor").build();
        Speciality dogDoc = Speciality.builder().name("dog-doctor").build();
        Speciality horseDoc = Speciality.builder().name("horse-doctor").build();

        specialityList.add(catDoc);
        specialityList.add(dogDoc);
        specialityList.add(horseDoc);

        for (int i = 0; i < specialityList.size(); i++) {
            ExtractableResponse<Response> postResponse =
                    requestSpecification.given()
                            .with()
                            .contentType(ContentType.JSON)
                            .body(specialityList.get(i))
                            .when()
                            .post("/petclinic/api/specialties")
                            .then()
                            .statusCode(201).extract();

            postResponse.response().body();

            JsonPath jsonPath = postResponse.jsonPath();
            specialityId.add(jsonPath.get("id"));
        }

        List<Vet> vetsList = new ArrayList<>();


        Vet catVet = Vet.builder().firstName("Ivan").lastName("Pavlov")
                .specialties(List.of(Speciality.builder().name("cat-doctor").build())).build();
        Vet dogVet = Vet.builder().firstName("Valeryi").lastName("Molchanov")
                .specialties(List.of(Speciality.builder().name("dog-doctor").build())).build();
        Vet horseVet = Vet.builder().firstName("Mikhail").lastName("Razygraev")
                .specialties(List.of(Speciality.builder().name("horse-doctor").build())).build();

        vetsList.add(catVet);
        vetsList.add(dogVet);
        vetsList.add(horseVet);

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
    }

    @Test
    void testStub() {
        Assertions.assertTrue(true);
    }

    @Test
    void testConnect() {
        requestSpecification
                .given()
                    .get("/petclinic/api/owners")
                .then()
                    .statusCode(SUCCESS_CODE);
    }

    @Test
    void positiveTest(){
        ExtractableResponse<Response> responseOwner = requestSpecification.given()
                .get("/petclinic/api/owners/" + ownerId.get(0))
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Ivan"))
                .body("lastName", equalTo("Ivanov")).extract();

        responseOwner.response().body();
        JsonPath jsonPathOwner = responseOwner.jsonPath();

        ExtractableResponse<Response> responseVet = requestSpecification.given()
                .get("/petclinic/api/vets")
                .then()
                .statusCode(200).extract();

        responseVet.response().body();
        JsonPath jsonPathVets = responseVet.jsonPath();

        String visitDescription = "";

        if (jsonPathOwner.getList("pets.type.name").get(0).equals("cat")) {
            visitDescription = jsonPathVets.getList("specialties.findAll{it.name =~ /ca.*/}.name")
                    .get(0) + " + vetId:"
                    + jsonPathVets.getObject("findAll{it.specialties.name =~ /ca.*/}.id", List.class)
                    .get(0);
        }
        if (jsonPathOwner.getList("pets.type.name").get(0).equals("dog")) {
            visitDescription = jsonPathVets.getList("specialties.findAll{it.name =~ /do.*/}.name") + " + vetId:" + jsonPathVets.getObject("findAll{it.specialties.name =~ /do.*/}.id", List.class);
        }
        if (jsonPathOwner.getList("pets.type.name").get(0).equals("horse")) {
            visitDescription = jsonPathVets.getList("specialties.findAll{it.name =~ /ho.*/}.name") + " + vetId:" + jsonPathVets.getObject("findAll{it.specialties.name =~ /ho.*/}.id", List.class);
        }


        Visit visit = Visit.builder().date("2024-10-30").description(visitDescription).build();
        System.out.println(visit);

        Integer petId = (Integer) jsonPathOwner.getList("pets.id").get(0);

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

        Integer visitId = jsonPathVisit.getInt("id");

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
    void negativeTest(){
        int petId = 9999;
        Visit visit = Visit.builder().date("1978-10-16").description("visitDescription").build();
        int visitId = 7777;

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
