package org.acme;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
