package org.acme.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInputDTO {

    @NotBlank(message = "{sku.obrigatorio}")
    @Size(min = 5, max = 12, message = "{sku.size}")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "{sku.pattern}")
    public String sku;

    @NotBlank(message = "{nome.obrigatorio}")
    @Size(min = 3, max = 40, message = "{nome.size}")
    @Pattern(regexp = "^[a-zA-Z0-9\\s-]+$", message = "{nome.pattern}")
    public String name;
}