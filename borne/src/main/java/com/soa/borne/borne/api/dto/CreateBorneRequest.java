package com.soa.borne.borne.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateBorneRequest(@NotBlank String id, @Positive double powerKw,
                                 @Positive double waterFlowLitersPerMinute) {}
