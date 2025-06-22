package org.acme;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductErroDTO {
    private String message;
    private String errorCode;
    private List<String> details;

    public static ProductErroDTO fromException(ApiException exception) {
        return new ProductErroDTO(exception.getMessage(), exception.getErrorCode(), exception.getDetails());
    }
}
