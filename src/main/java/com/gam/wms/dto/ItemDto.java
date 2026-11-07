package com.gam.wms.dto;

public record ItemDto(String code , String ean, String desc, Integer noteQty, String batch) {
}
