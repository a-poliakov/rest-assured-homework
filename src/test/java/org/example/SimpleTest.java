package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.example.dto.OwnersData;
import org.example.dto.PetType;
import org.example.dto.PetsData;
import org.example.dto.UsersData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.example.utils.Constants.BASE_URL;
import static org.example.utils.Constants.SUCCESS_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleTest {
    static RequestSpecification requestSpecification;


    @BeforeAll
    static void setUp() {

        requestSpecification = RestAssured.given()
                .baseUri(BASE_URL)
                .accept(ContentType.JSON);


        String admin = "admin";
        UsersData usersData = UsersData.builder()
                .username("Jojo")
                .password("123543")
                .enabled(true)
                .roles(admin)
                .build();
        requestSpecification
                .with().body(usersData)
                .given()
                .post("/users");

        OwnersData ownersData1 = OwnersData.builder()
                .firstname("Alan")
                .lastname("Becker")
                .adress("Nevsky Avenue, 28")
                .city("Saint-Petersburg")
                .id(0);
        requestSpecification
                .with().body(ownersData1)
                .given()
                .post("/owners");

        OwnersData ownersData2 = OwnersData.builder()
                .firstname("Sherlock")
                .lastname("Holmes")
                .adress("Becker Street, 221B")
                .city("London")
                .id(1);
        requestSpecification
                .with().body(ownersData2)
                .given()
                .post("/owners");

        PetType typeCat = PetType.builder()
                .type("cat")
                .id(1);
        requestSpecification
                .with().body(typeCat)
                .given()
                .post("/pettypes");

        PetsData catBoris = PetsData.builder()
                .petName("Boris")
                .birthdate("27-11-2024")
                .type(typeCat);
        requestSpecification
                .with().body(catBoris)
                .given()
                .post("/owners/1/pets");


    }

    @Test
    void testStub() {
        Assertions.assertTrue(true);
    }

    @Test
    void testConnect() {
        requestSpecification
                .given()
                .get("/owners")
                .then()
                .statusCode(SUCCESS_CODE);

    }
    @Test
    void testPositive(){
        requestSpecification
                .given()
                .get("/owners/0")
                .then()
                .statusCode(SUCCESS_CODE);


    }

    @Test
    void testNegative(){
        requestSpecification
                .given()
                .get("/unOwners")
                .then()
                .statusCode(400);

        requestSpecification
                .given()
                .get("/owners/0/pets/Boris/visits")
                .then()
                .statusCode(404);
    }
}
