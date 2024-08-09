package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.example.utils.Constants.BASE_URL;
import static org.example.utils.Constants.SUCCESS_CODE;

public class SimpleTest {
    static RequestSpecification requestSpecification;


    @BeforeAll
    static void setUp() {
        requestSpecification = RestAssured.given()
                .baseUri(BASE_URL)
                .accept(ContentType.JSON);
    }

    @Test
    void testStub() {
        Assertions.assertTrue(true);
    }

    @Test
    void testConnect() {
        requestSpecification
                .given()
                    .get("/petclinic")
                .then()
                    .statusCode(SUCCESS_CODE);
    }
}
