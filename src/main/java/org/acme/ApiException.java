package org.acme;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Exceção base para erros de API, permitindo código de erro e detalhes customizados.
 * Pode ser estendida para exceções específicas do domínio.
 */
public abstract class ApiException extends WebApplicationException {
    /** Código de erro customizado para rastreamento. */
    private final String errorCode;
    /** Detalhes adicionais do erro. */
    private final List<String> details;

    /**
     * Construtor completo.
     * @param message mensagem de erro
     * @param errorCode código de erro
     * @param details detalhes adicionais
     * @param status status HTTP
     */
    public ApiException(String message, String errorCode, List<String> details, Response.Status status) {
        super(message, status);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Construtor sem detalhes adicionais.
     * @param message mensagem de erro
     * @param errorCode código de erro
     * @param status status HTTP
     */
    public ApiException(String message, String errorCode, Response.Status status) {
        this(message, errorCode, null, status);
    }

    /**
     * Retorna o código de erro customizado.
     */
    public String getErrorCode() { return errorCode; }
    /**
     * Retorna detalhes adicionais do erro.
     */
    public List<String> getDetails() { return details; }

    @Override
    public String toString() {
        return "ApiException{" +
                "errorCode='" + errorCode + '\'' +
                ", details='" + details.toString() + '\'' +
                ", message='" + getMessage() + '\'' +
                ", status=" + getResponse().getStatus() +
                '}';
    }
}
