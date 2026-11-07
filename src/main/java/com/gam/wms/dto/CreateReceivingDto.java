package com.gam.wms.dto;


import java.util.List;

public record CreateReceivingDto(String receivingCode, List<VolumeDto> volumes) {}
