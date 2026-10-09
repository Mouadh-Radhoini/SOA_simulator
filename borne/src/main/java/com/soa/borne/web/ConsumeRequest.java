package com.soa.borne.web;

import jakarta.validation.constraints.PositiveOrZero;

public record ConsumeRequest(@PositiveOrZero double amount) {
}
