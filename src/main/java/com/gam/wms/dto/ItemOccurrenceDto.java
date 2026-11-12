package com.gam.wms.dto;

public record ItemOccurrenceDto(
        String materialCode,
        int damagedQty,
        String specificReason
) {}