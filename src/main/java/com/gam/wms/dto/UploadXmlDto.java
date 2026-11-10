package com.gam.wms.dto;

import java.util.List;

public record UploadXmlDto(String invoiceNumber, String customerName, List<ItemDto> items) {}