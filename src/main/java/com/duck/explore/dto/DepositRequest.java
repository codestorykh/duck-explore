package com.duck.explore.dto;

import com.duck.explore.validation.ValidCurrency;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {

    @NotBlank(message = "{validation.account.blank}")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "{validation.account.format}")
    @Pattern(regexp = "^\\d{8,20}$", message = "{validation.account.digits}")
    private String accountNo;

    @NotNull(message = "{validation.amount.null}")
    @DecimalMin(value = "0.01", message = "{validation.amount.min}")
    @Digits(integer = 12, fraction = 2, message = "{validation.amount.fraction}")
    private BigDecimal amount;

    @NotBlank(message = "{validation.currency.invalid}")
    @ValidCurrency(message = "{validation.currency.invalid}")
    private String currency;

    private String referenceId;
}