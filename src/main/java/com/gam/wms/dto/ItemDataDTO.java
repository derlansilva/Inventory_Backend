package com.gam.wms.dto;

import java.time.LocalDate;

public class ItemDataDTO {
    // Campos que o FRONT (CentralSac.tsx) envia:
    private String code;
    private String desc;

    // Campos originais do seu sistema:
    private String materialCode;
    private String description;
    private Integer noteQty;
    private Integer damagedQty;
    private String batch;
    private String specificReason; // Front envia isso

    // Getters e Setters para o FRONT
    public String getCode() { return code != null ? code : materialCode; }
    public void setCode(String code) { this.code = code; this.materialCode = code; }

    public String getDesc() { return desc != null ? desc : description; }
    public void setDesc(String desc) { this.desc = desc; this.description = desc; }

    // Getters e Setters originais
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getNoteQty() { return noteQty; }
    public void setNoteQty(Integer noteQty) { this.noteQty = noteQty; }

    public Integer getDamagedQty() { return damagedQty; }
    public void setDamagedQty(Integer damagedQty) { this.damagedQty = damagedQty; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getSpecificReason() { return specificReason; }
    public void setSpecificReason(String specificReason) { this.specificReason = specificReason; }
}