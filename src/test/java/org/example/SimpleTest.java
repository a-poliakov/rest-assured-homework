package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.example.dto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.example.utils.Constants.*;
import static org.hamcrest.Matchers.equalTo;

public class SimpleTest {
    static RequestSpecification requestSpecification;

    static List<Owner> owners = new ArrayList<>();
    static List<PetType> petTypes = new ArrayList<>();
    static List<Pet> pets = new ArrayList<>();
    static List<Specialty> specialties = new ArrayList<>();
    static List<Vet> vets = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        requestSpecification = RestAssured.given()
                .baseUri(BASE_URL)
                .accept(ContentType.JSON);

        // Добавить одного пользователя с ролью admin
        User user = User.builder().username("admin").password("pass").enabled(true).roles(List.of(Role.builder().name("admin").build())).build();

        requestSpecification.given()
                .with().body(user)
                .contentType("application/json")
                .when()
                .post("/petclinic/api/users")
                .then()
                .statusCode(SUCCESS_POST_CODE)
                .body("username", equalTo("admin"))
                .body("roles[0].name", equalTo("ROLE_admin"));

        // Добавить 2 владельцев питомцев
        Owner owner1 = Owner.builder().firstName("Mike").lastName("Davis").address("115 W. Liberty St.").city("NewYork").telephone("8294615723").build();
        Owner owner2 = Owner.builder().firstName("Betty").lastName("Sewell").address("231 Oak St").city("NewArk").telephone("8373407451").build();

        owners.add(owner1);
        owners.add(owner2);

        for (Owner currentOwner : owners) {
            Integer id = requestSpecification.given()
                    .with().body(currentOwner)
                    .contentType("application/json")
                    .when()
                    .post("/petclinic/api/owners")
                    .then()
                    .statusCode(SUCCESS_POST_CODE)
                    .body("firstName", equalTo(currentOwner.getFirstName()))
                    .body("lastName", equalTo(currentOwner.getLastName()))
                    .extract().path("id");

            currentOwner.setId(id);
        }

        // Добавить 3 вида питомцев
        PetType petType1 = PetType.builder().name("cat").build();
        PetType petType2 = PetType.builder().name("dog").build();
        PetType petType3 = PetType.builder().name("hamster").build();

        petTypes.add(petType1);
        petTypes.add(petType2);
        petTypes.add(petType3);

        for (PetType currentPetType : petTypes) {
            Integer id = requestSpecification.given()
                    .with().body(currentPetType)
                    .contentType("application/json")
                    .when()
                    .post("/petclinic/api/pettypes")
                    .then()
                    .statusCode(SUCCESS_POST_CODE)
                    .body("name", equalTo(currentPetType.getName()))
                    .extract().path("id");

            currentPetType.setId(id);
        }

        // Добавить 5 питомцев
        Pet pet1 = Pet.builder().name("Molly").birthDate("2010-09-07").type(petTypes.get(0)).ownerId(owners.get(0).getId()).build();  // cat, owner1
        Pet pet2 = Pet.builder().name("Bella").birthDate("2012-08-06").type(petTypes.get(1)).ownerId(owners.get(1).getId()).build();  // dog, owner2
        Pet pet3 = Pet.builder().name("Luna").birthDate("2011-07-05").type(petTypes.get(2)).ownerId(owners.get(0).getId()).build();  // hamster, owner1
        Pet pet4 = Pet.builder().name("Toby").birthDate("2010-06-04").type(petTypes.get(0)).ownerId(owners.get(1).getId()).build();  // cat, owner2
        Pet pet5 = Pet.builder().name("Lucy").birthDate("2012-05-03").type(petTypes.get(1)).ownerId(owners.get(0).getId()).build();  // dog, owner1

        pets.add(pet1);
        pets.add(pet2);
        pets.add(pet3);
        pets.add(pet4);
        pets.add(pet5);

        for (Pet currentPet : pets) {
            Integer id = requestSpecification.given()
                    .with().body(currentPet)
                    .contentType("application/json")
                    .when()
                    .post("/petclinic/api/owners/" + currentPet.getOwnerId() + "/pets")
                    .then()
                    .statusCode(SUCCESS_POST_CODE)
                    .body("name", equalTo(currentPet.getName()))
                    .body("birthDate", equalTo(currentPet.getBirthDate()))
                    .extract().path("id");

            currentPet.setId(id);
        }

        // Создать 2 специализации ветеринаров
        Specialty specialty1 = Specialty.builder().name("cat-dog-doctor").build();
        Specialty specialty2 = Specialty.builder().name("hamster-doctor").build();

        specialties.add(specialty1);
        specialties.add(specialty2);

        for (Specialty currentSpecialty : specialties) {
            Integer id = requestSpecification.given()
                    .with().body(currentSpecialty)
                    .contentType("application/json")
                    .when()
                    .post("/petclinic/api/specialties")
                    .then()
                    .statusCode(SUCCESS_POST_CODE)
                    .body("name", equalTo(currentSpecialty.getName()))
                    .extract().path("id");

            currentSpecialty.setId(id);
        }

        // Создать 3 ветеринара
        Vet vet1 = Vet.builder().firstName("John").lastName("Neal").specialties(List.of(specialty1)).build();  // cat-dog-doctor
        Vet vet2 = Vet.builder().firstName("Jane").lastName("Moore").specialties(List.of(specialty2)).build();  // hamster-doctor
        Vet vet3 = Vet.builder().firstName("Bob").lastName("Edwards").specialties(List.of(specialty1, specialty2)).build();  // cat-dog-doctor, hamster-doctor

        vets.add(vet1);
        vets.add(vet2);
        vets.add(vet3);

        for (Vet currentVet : vets) {
            Integer id = requestSpecification.given()
                    .with().body(currentVet)
                    .contentType("application/json")
                    .when()
                    .post("/petclinic/api/vets")
                    .then()
                    .statusCode(SUCCESS_POST_CODE)
                    .body("firstName", equalTo(currentVet.getFirstName()))
                    .body("lastName", equalTo(currentVet.getLastName()))
                    .body("specialties[0].name", equalTo(currentVet.getSpecialties().get(0).getName()))
                    .extract().path("id");

            currentVet.setId(id);
        }
    }

    @Test
    void testStub() {
        Assertions.assertTrue(true);
    }

    @Test
    void testPositive() {
        Owner testOwner = owners.get(0);
        Pet testPet = pets.get(0);
        Vet testVet = vets.get(0);

        // Выбор владельца питомца
        requestSpecification.given()
                .when()
                .get("/petclinic/api/owners/" + testOwner.getId())
                .then()
                .statusCode(SUCCESS_CODE)
                .body("firstName", equalTo(testOwner.getFirstName()))
                .body("lastName", equalTo(testOwner.getLastName()))
                .body("address", equalTo(testOwner.getAddress()))
                .body("city", equalTo(testOwner.getCity()))
                .body("telephone", equalTo(testOwner.getTelephone()))
                .body("id", equalTo(testOwner.getId()));

        // Выбор питомца
        requestSpecification.given()
                .when()
                .get("/petclinic/api/owners/" + testOwner.getId() + "/pets/" + testPet.getId())
                .then()
                .statusCode(SUCCESS_CODE)
                .body("name", equalTo(testPet.getName()))
                .body("birthDate", equalTo(testPet.getBirthDate()))
                .body("type.name", equalTo(testPet.getType().getName()))
                .body("ownerId", equalTo(testOwner.getId()))
                .body("id", equalTo(testPet.getId()));

        // Выбор ветеринара
        requestSpecification.given()
                .when()
                .get("/petclinic/api/vets/" + testVet.getId())
                .then()
                .statusCode(SUCCESS_CODE)
                .body("firstName", equalTo(testVet.getFirstName()))
                .body("lastName", equalTo(testVet.getLastName()))
                .body("specialties[0].name", equalTo(testVet.getSpecialties().get(0).getName()))
                .body("id", equalTo(testVet.getId()));

        // Создание записи на приём
        Visit testVisit = Visit.builder().date("2025-03-28").description("description for " + testPet.getName()).build();

        Integer id = requestSpecification.given()
                .with().body(testVisit)
                .contentType("application/json")
                .when()
                .post("/petclinic/api/owners/" + testOwner.getId() + "/pets/" + testPet.getId() + "/visits")
                .then()
                .statusCode(SUCCESS_POST_CODE)
                .body("description", equalTo(testVisit.getDescription()))
                .body("date", equalTo(testVisit.getDate()))
                .extract().path("id");

        testVisit.setId(id);

        // Получим созданную запись
        requestSpecification.given()
                .when()
                .get("/petclinic/api/visits/" + testVisit.getId())
                .then()
                .statusCode(SUCCESS_CODE)
                .body("description", equalTo(testVisit.getDescription()))
                .body("date", equalTo(testVisit.getDate()))
                .body("id", equalTo(testVisit.getId()))
                .body("petId", equalTo(testPet.getId()));
    }

    @Test
    void testNegative() {

        int invalidId = Integer.MAX_VALUE;

        // Создание некорректной записи на приём
        Visit testVisit = Visit.builder().date("2025-03-28").description("test description").build();

        requestSpecification.given()
                .with().body(testVisit)
                .contentType("application/json")
                .when()
                .post("/petclinic/api/owners/" + invalidId + "/pets/" + invalidId + "/visits")
                .then()
                .statusCode(NOT_FOUND_CODE);

        // Получение записи для несуществующего питомца
        requestSpecification.given()
                .when()
                .get("/petclinic/api/visits/" + invalidId)
                .then()
                .statusCode(NOT_FOUND_CODE);
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
