package com.gam.wms.dto;

import java.util.List;

public record SaveOccurrenceDto(
        String invoiceNumber,
        String reason,
        List<ItemOccurrenceDto> items
) {}