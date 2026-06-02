package hr.algebra.webshop.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Naziv je obavezan")
    private String name;

    private String description;

    @NotNull(message = "Cijena je obavezna")
    @DecimalMin(value = "0.01", message = "Cijena mora biti veća od 0")
    private BigDecimal price;

    @Min(value = 0, message = "Zaliha ne može biti negativna")
    private int stock;

    private String imageUrl;

    @NotNull(message = "Kategorija je obavezna")
    private Long categoryId;
}