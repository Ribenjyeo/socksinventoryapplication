package ru.socks.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SockRequest {
    @NotBlank
    private String color;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer cottonContent;

    @NotNull
    @Positive
    private Integer quantity;
}
