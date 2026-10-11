package com.soa.borne.pricing.domain;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculate(BigDecimal quantity);
}
