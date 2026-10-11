package com.soa.borne.borne.api.dto;

import com.soa.borne.borne.domain.BorneStatus;
public record StatusResponse(String id, BorneStatus status) {}
