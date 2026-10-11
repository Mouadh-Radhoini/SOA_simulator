package com.soa.borne.borne.api.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record ConsumeRequest(@DecimalMin("0.0") BigDecimal electricityKwh,
                              @DecimalMin("0.0") BigDecimal waterLiters) {}
