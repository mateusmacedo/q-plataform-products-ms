package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ProductResourceTest {

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithBlankSKU() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO("", "Product 1");
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post("/products")
                    .then()
                    .statusCode(400)
                    .body(containsString("SKU é obrigatório"))
                    .body(containsString("SKU deve ter entre 5 e 12 caracteres"))
                    .body(containsString("SKU deve conter apenas letras maiúsculas, números e hífen"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithMinSKUFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO("1234", "Product 1");
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post("/products")
                    .then()
                    .statusCode(400)
                    .body(containsString("SKU deve ter entre 5 e 12 caracteres"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithMaxSKUFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO("1234567890123", "Product 1");
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post("/products")
                    .then()
                    .statusCode(400)
                    .body(containsString("SKU deve ter entre 5 e 12 caracteres"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithInvalidSKUFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO("12345678901%", "Product 1");
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post("/products")
                    .then()
                    .statusCode(400)
                    .body(containsString("SKU deve conter apenas letras maiúsculas, números e hífen"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithBlankName() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO("123456789012", "");
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post("/products")
                    .then()
                    .statusCode(400)
                    .body(containsString("Nome é obrigatório"))
                    .body(containsString("Nome deve ter entre 3 e 40 caracteres"))
                    .body(containsString("Nome deve conter apenas letras, números, espaços e hífen"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithMinNameFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO("123456789012", "Pr");
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post("/products")
                    .then()
                    .statusCode(400)
                    .body(containsString("Nome deve ter entre 3 e 40 caracteres"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithMaxNameFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO("123456789012", "Product 1234567890123456789012345678901234567890");
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post("/products")
                    .then()
                    .statusCode(400)
                    .body(containsString("Nome deve ter entre 3 e 40 caracteres"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithInvalidNameFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO("123456789012", "Product %");
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post("/products")
                    .then()
                    .statusCode(400)
                    .body(containsString("Nome deve conter apenas letras, números, espaços e hífen"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
