package com.soa.borne.web;

import com.soa.borne.domain.BorneStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull BorneStatus status) {
}
