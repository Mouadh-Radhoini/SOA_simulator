package com.soa.borne.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.soa.borne.domain.BorneType;

public record CreateBorneRequest(
        @NotBlank String id,
        @NotNull BorneType type,
        @Positive double rate) {
}
