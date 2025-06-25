package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

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
class ProductResourceTest {
    private final String BASE_URL = "/products";
    private final String SKU_VALID = "123456789012";
    private final String NAME_VALID = "Product Dummy";
    private final String SKU_MIN = "12345";
    private final String NAME_MIN = "Nom";
    private final String SKU_MAX = "012345678901";
    private final String NAME_MAX = "0123456789012345678901234567890123456789";
    private final String SKU_INVALID = "sku12345";
    private final String NAME_INVALID = "Product %";
    private final String SKU_OBRIG = "SKU é obrigatório";
    private final String NAME_OBRIG = "Nome é obrigatório";
    private final String SKU_LENGTH = "SKU deve ter entre 5 e 12 caracteres";
    private final String NAME_LENGTH = "Nome deve ter entre 3 e 40 caracteres";
    private final String SKU_FORMAT = "SKU deve conter apenas letras maiúsculas, números e hífen";
    private final String NAME_FORMAT = "Nome deve conter apenas letras, números, espaços e hífen";

    @BeforeEach
    void limparBanco() {
        Product.deleteAll().await().indefinitely();
    }

    private String toJson(ProductInputDTO dto) {
        try { return new ObjectMapper().writeValueAsString(dto); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }

    private void postAndExpect(String json, int status, String... expectedContents) {
        var req = given().contentType(MediaType.APPLICATION_JSON).body(json).when().post(BASE_URL).then().statusCode(status);
        for (String content : expectedContents) req.body(containsString(content));
    }

    @Test
    void shouldReturnErrorWhenSkuIsBlank() {
        postAndExpect(toJson(new ProductInputDTO("", NAME_VALID)), 400, SKU_OBRIG, SKU_LENGTH, SKU_FORMAT);
    }

    @Test
    void shouldReturnErrorWhenNameIsBlank() {
        postAndExpect(toJson(new ProductInputDTO(SKU_VALID, "")), 400, NAME_OBRIG, NAME_LENGTH, NAME_FORMAT);
    }

    @Test
    void shouldReturnErrorWhenSkuIsNull() {
        postAndExpect("{\"sku\":null,\"name\":\"Produto Teste\"}", 400, SKU_OBRIG);
    }

    @Test
    void shouldReturnErrorWhenNameIsNull() {
        postAndExpect("{\"sku\":\"SKU12345\",\"name\":null}", 400, NAME_OBRIG);
    }

    @Test
    void shouldReturnErrorWhenSkuIsMissing() {
        postAndExpect("{\"name\":\"Produto Teste\"}", 400, SKU_OBRIG);
    }

    @Test
    void shouldReturnErrorWhenNameIsMissing() {
        postAndExpect("{\"sku\":\"SKU12345\"}", 400, NAME_OBRIG);
    }

    @Test
    void shouldReturnErrorWhenSkuIsTooShort() {
        postAndExpect(toJson(new ProductInputDTO("1234", NAME_VALID)), 400, SKU_LENGTH);
    }

    @Test
    void shouldReturnErrorWhenSkuIsTooLong() {
        postAndExpect(toJson(new ProductInputDTO("1234567890123", NAME_VALID)), 400, SKU_LENGTH);
    }

    @Test
    void shouldReturnErrorWhenNameIsTooShort() {
        postAndExpect(toJson(new ProductInputDTO(SKU_VALID, "Pr")), 400, NAME_LENGTH);
    }

    @Test
    void shouldReturnErrorWhenNameIsTooLong() {
        postAndExpect(toJson(new ProductInputDTO(SKU_VALID, "Product 1234567890123456789012345678901234567890")), 400, NAME_LENGTH);
    }

    @Test
    void shouldReturnErrorWhenSkuIsInvalidFormat() {
        postAndExpect(toJson(new ProductInputDTO(SKU_INVALID, NAME_VALID)), 400, SKU_FORMAT);
    }

    @Test
    void shouldReturnErrorWhenNameIsInvalidFormat() {
        postAndExpect(toJson(new ProductInputDTO(SKU_VALID, NAME_INVALID)), 400, NAME_FORMAT);
    }

    @Test
    void shouldReturnSuccessWhenCreateWithMinLengthFields() {
        postAndExpect(toJson(new ProductInputDTO(SKU_MIN, NAME_MIN)), 201, SKU_MIN, NAME_MIN);
    }

    @Test
    void shouldReturnSuccessWhenCreateWithMaxLengthFields() {
        postAndExpect(toJson(new ProductInputDTO(SKU_MAX, NAME_MAX)), 201, SKU_MAX, NAME_MAX);
    }

    @Test
    void shouldReturnSuccessWhenCreateProduct() {
        postAndExpect(toJson(new ProductInputDTO(SKU_VALID, NAME_VALID)), 201, SKU_VALID, NAME_VALID);
    }

    @Test
    void shouldReturnErrorWhenSkuAlreadyExists() {
        postAndExpect(toJson(new ProductInputDTO(SKU_VALID, NAME_VALID)), 201, SKU_VALID);
        postAndExpect(toJson(new ProductInputDTO(SKU_VALID, "Outro Nome")), 409, "Já existe um produto com o SKU " + SKU_VALID);
    }

    @Test
    void shouldReturnErrorWhenNameAlreadyExists() {
        postAndExpect(toJson(new ProductInputDTO("SKU-UNICO-1", "NomeUnico")), 201, "SKU-UNICO-1");
        postAndExpect(toJson(new ProductInputDTO("SKU-UNICO-2", "NomeUnico")), 409, "Já existe um produto com o SKU SKU-UNICO-2");
    }

    @Test
    void shouldReturnErrorWhenGetNonexistentSku() {
        given().when().get(BASE_URL + "/SKU_INEXISTENTE").then().statusCode(404).body(containsString("Produto não encontrado"));
    }

    @Test
    void shouldReturnProductBySku() {
        postAndExpect(toJson(new ProductInputDTO(SKU_VALID, NAME_VALID)), 201, SKU_VALID, NAME_VALID);
        given().when().get(BASE_URL + "/" + SKU_VALID).then().statusCode(200).body(containsString(SKU_VALID)).body(containsString(NAME_VALID));
    }

    @Test
    void shouldReturnXTraceIdHeaderInResponse() {
        String traceId = "trace-id-teste-123";
        String json = toJson(new ProductInputDTO("SKU-TRACE-1", "Produto Trace"));
        given().contentType(MediaType.APPLICATION_JSON).header("X-Trace-Id", traceId).body(json).when().post(BASE_URL).then().statusCode(201).header("X-Trace-Id", traceId);
    }

    @Test
    void shouldReturnInternalServerErrorOnMalformedJson() {
        String malformedJson = "{\"sku\":\"12345\",\"name\":\"Produto\""; // falta fechar o JSON
        given().contentType(MediaType.APPLICATION_JSON).body(malformedJson).when().post(BASE_URL).then().statusCode(500).body(containsString("Erro interno do servidor"));
    }
}
