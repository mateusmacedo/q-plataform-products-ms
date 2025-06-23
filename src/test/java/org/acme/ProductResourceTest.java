package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import java.sql.Connection;
import java.sql.Statement;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductResourceTest {
    private final String BASE_URL = "/products";

    private final String PRODUCT_SKU_INVALID = "12345678901%";
    private final String PRODUCT_NAME_INVALID = "Product %";

    private final String PRODUCT_SKU_BLANK = "";
    private final String PRODUCT_NAME_BLANK = "";

    private final String PRODUCT_SKU_MIN = "1234";
    private final String PRODUCT_NAME_MIN = "Pr";

    private final String PRODUCT_SKU_MAX = "1234567890123";
    private final String PRODUCT_NAME_MAX = "Product 1234567890123456789012345678901234567890";

    private final String PRODUCT_SKU_VALID = "123456789012";
    private final String PRODUCT_NAME_VALID = "Product Dummy";

    private final String PRODUCT_SKU_OBLIGATORY = "SKU é obrigatório";
    private final String PRODUCT_NAME_OBLIGATORY = "Nome é obrigatório";

    private final String PRODUCT_SKU_LENGTH = "SKU deve ter entre 5 e 12 caracteres";
    private final String PRODUCT_NAME_LENGTH = "Nome deve ter entre 3 e 40 caracteres";

    private final String PRODUCT_SKU_FORMAT = "SKU deve conter apenas letras maiúsculas, números e hífen";
    private final String PRODUCT_NAME_FORMAT = "Nome deve conter apenas letras, números, espaços e hífen";

    @BeforeEach
    public void limparBanco() {
        Product.deleteAll();
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithBlankSKU() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_BLANK, PRODUCT_NAME_VALID);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(400)
                    .body(containsString(PRODUCT_SKU_OBLIGATORY))
                    .body(containsString(PRODUCT_SKU_LENGTH))
                    .body(containsString(PRODUCT_SKU_FORMAT));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithMinSKUFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_MIN, PRODUCT_NAME_VALID);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(400)
                    .body(containsString(PRODUCT_SKU_LENGTH));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithMaxSKUFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_MAX, PRODUCT_NAME_VALID);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(400)
                    .body(containsString(PRODUCT_SKU_LENGTH));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithInvalidSKUFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_INVALID, PRODUCT_NAME_VALID);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(400)
                    .body(containsString(PRODUCT_SKU_FORMAT));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithBlankName() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_VALID, PRODUCT_NAME_BLANK);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(400)
                    .body(containsString(PRODUCT_NAME_OBLIGATORY))
                    .body(containsString(PRODUCT_NAME_LENGTH))
                    .body(containsString(PRODUCT_NAME_FORMAT));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithMinNameFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_VALID, PRODUCT_NAME_MIN);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(400)
                    .body(containsString(PRODUCT_NAME_LENGTH));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithMaxNameFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_VALID, PRODUCT_NAME_MAX);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(400)
                    .body(containsString(PRODUCT_NAME_LENGTH));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShouldReturnAnErrorWhenTryCreateAProductWithInvalidNameFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_VALID, PRODUCT_NAME_INVALID);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(400)
                    .body(containsString(PRODUCT_NAME_FORMAT));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    public void testShouldReturnSuccessWhenCreateAProduct() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_VALID, PRODUCT_NAME_VALID);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(201)
                    .body(containsString("123456789012"))
                    .body(containsString("Product Dummy"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    public void testShouldReturnAnErrorWhenTryCreateAProductWithAlreadyExistsSKU() {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductInputDTO product = new ProductInputDTO(PRODUCT_SKU_VALID, PRODUCT_NAME_VALID);
        try {
            String json = objectMapper.writeValueAsString(product);
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .when().post(BASE_URL)
                    .then()
                    .statusCode(409)
                    .body(containsString("Já existe um produto com o SKU 123456789012"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
