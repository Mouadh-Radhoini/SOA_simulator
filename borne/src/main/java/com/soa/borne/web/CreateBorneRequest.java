package com.soa.borne.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateBorneRequest(
        @NotBlank String id,
        @Positive double batteryCapacityKwh,
        @Positive double powerKw) {
}
