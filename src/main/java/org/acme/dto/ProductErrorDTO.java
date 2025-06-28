package org.acme.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.exception.ApiException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductErrorDTO {
    private String message;
    private String errorCode;
    private List<String> details;

    public static ProductErrorDTO fromException(ApiException exception) {
        return new ProductErrorDTO(exception.getMessage(), exception.getErrorCode(), exception.getDetails());
    }
}
