package ru.socks.inventory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.socks.inventory.dto.OperatorEnum;
import ru.socks.inventory.dto.SockRequest;
import ru.socks.inventory.model.Sock;
import ru.socks.inventory.repository.SockRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SockServiceTest {

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    SockRepository sockRepository;

    @LocalServerPort
    private Integer port;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        sockRepository.deleteAll();
    }

    @Test
    void incomeIncrementTest() {
        Sock sock = new Sock(null, "Red", 100, 10);
        sockRepository.save(sock);

        SockRequest sockRequest = new SockRequest()
                .setColor("Red")
                .setCottonContent(100)
                .setQuantity(10);
        given()
                .contentType(ContentType.JSON)
                .body(sockRequest)
                .when()
                .post("/api/socks/income")
                .then()
                .statusCode(200)
                .body(equalTo("Income registered successfully"));

        List<Sock> socks = sockRepository.findAll();
        assertThat(socks).hasSize(1);
        assertThat(socks.get(0).getQuantity()).isEqualTo(20);
    }

    @Test
    void incomeAddSockTest() {
        Sock sock = new Sock(null, "Gray", 100, 10);
        sockRepository.save(sock);

        SockRequest sockRequest = new SockRequest()
                .setColor("Red")
                .setCottonContent(100)
                .setQuantity(10);
        given()
                .contentType(ContentType.JSON)
                .body(sockRequest)
                .when()
                .post("/api/socks/income")
                .then()
                .statusCode(200)
                .body(equalTo("Income registered successfully"));

        List<Sock> socks = sockRepository.findAll();
        assertThat(socks).hasSize(2);
    }

    @Test
    void outcomeSockTest() {
        Sock sock = new Sock(null, "Red", 100, 10);
        sockRepository.save(sock);

        SockRequest sockRequest = new SockRequest()
                .setColor("Red")
                .setCottonContent(100)
                .setQuantity(1);
        given()
                .contentType(ContentType.JSON)
                .body(sockRequest)
                .when()
                .post("/api/socks/outcome")
                .then()
                .statusCode(200)
                .body(equalTo("Outcome registered successfully"));

        List<Sock> socks = sockRepository.findAll();
        assertThat(socks.get(0).getQuantity()).isEqualTo(9);
    }

    @Test
    void outcomeConflictTest() {
        Sock sock = new Sock(null, "Red", 100, 10);
        sockRepository.save(sock);

        SockRequest sockRequest = new SockRequest()
                .setColor("Red")
                .setCottonContent(100)
                .setQuantity(11);
        given()
                .contentType(ContentType.JSON)
                .body(sockRequest)
                .when()
                .post("/api/socks/outcome")
                .then()
                .statusCode(409)
                .body(equalTo("Conflict while searching for socks: Not enough socks in stock to perform outcome operation"));
    }

    @Test
    void getAllSocksTest() {
        List<Sock> socks = List.of(
                new Sock(null, "Red", 100, 10),
                new Sock(null, "Red", 50, 10),
                new Sock(null, "Red", 1, 10)
        );
        sockRepository.saveAll(socks);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/socks")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThan(0));

        given()
                .contentType(ContentType.JSON)
                .param("color", "Red")
                .when()
                .get("/api/socks")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThan(2));

        given()
                .contentType(ContentType.JSON)
                .param("cottonContent", 1)
                .param("maxCottonContent", 50)
                .when()
                .get("/api/socks")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThan(1));

        given()
                .contentType(ContentType.JSON)
                .param("operation", OperatorEnum.EQUAL.getOperator())
                .param("cottonContent", 1)
                .when()
                .get("/api/socks")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThan(0));

        given()
                .contentType(ContentType.JSON)
                .param("operation", OperatorEnum.LESS_THAN.getOperator())
                .param("cottonContent", 100)
                .when()
                .get("/api/socks")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThan(1));

        given()
                .contentType(ContentType.JSON)
                .param("operation", OperatorEnum.MORE_THAN.getOperator())
                .param("cottonContent", 1)
                .when()
                .get("/api/socks")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThan(1));
    }

    @Test
    void updateSocksTest() {
        Sock sock = new Sock(null, "Red", 100, 10);
        sockRepository.save(sock);
        Long id = sockRepository.findAll().get(0).getId();

        SockRequest sockRequest = new SockRequest()
                .setColor("Red")
                .setCottonContent(100)
                .setQuantity(1);
        given()
                .contentType(ContentType.JSON)
                .body(sockRequest)
                .when()
                .put("/api/socks/" + id)
                .then()
                .statusCode(200)
                .body(equalTo("Socks updated successfully"));

        Sock response = sockRepository.findAll().get(0);
        assertThat(response.getQuantity()).isEqualTo(1);
    }

    @Test
    void updateNotFoundSockTest() {
        SockRequest sockRequest = new SockRequest()
                .setColor("Red")
                .setCottonContent(100)
                .setQuantity(1);
        given()
                .contentType(ContentType.JSON)
                .body(sockRequest)
                .when()
                .put("/api/socks/123")
                .then()
                .statusCode(409)
                .body(equalTo("Conflict while searching for socks: Conflict detected or sock not found"));
    }

    @Test
    void batchInsertSockTest() throws IOException {
        File file = new File("src/test/resources/sock_batch.xlsx");
        FileInputStream fileInputStream = new FileInputStream(file);

        given()
                .multiPart("file", file.getName(), fileInputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .when()
                .post("/api/socks/batch")
                .then()
                .statusCode(200)
                .body(equalTo("Batch uploaded successfully"));
    }
}