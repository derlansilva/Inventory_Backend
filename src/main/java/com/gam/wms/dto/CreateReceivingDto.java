package com.gam.wms.dto;


import java.util.List;

public record CreateReceivingDto(
        String receivingCode,
        String manifestCode, // <-- ADICIONE ESTA LINHA
        List<VolumeDto> volumes
) {}