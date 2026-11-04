package com.gam.wms.dto;

import java.time.LocalDate;

public  class ItemDataDTO {
    private String materialCode;
    private String description;
    private Integer noteQty;
    private Integer availableQty; // Estoque Disponível
    private Integer damagedQty;   // Estoque Avariado
    private Integer reservedQty;  // Estoque Reservado
    private String specificReason;
    private String batch;
    private LocalDate expiryDate;
    private String disposition;



    // Getters e Setters
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getNoteQty() { return noteQty; }
    public void setNoteQty(Integer noteQty) { this.noteQty = noteQty; }
    public Integer getAvailableQty() { return availableQty; }
    public void setAvailableQty(Integer availableQty) { this.availableQty = availableQty; }
    public Integer getDamagedQty() { return damagedQty; }
    public void setDamagedQty(Integer damagedQty) { this.damagedQty = damagedQty; }
    public Integer getReservedQty() { return reservedQty; }
    public void setReservedQty(Integer reservedQty) { this.reservedQty = reservedQty; }
    public String getSpecificReason() { return specificReason; }
    public void setSpecificReason(String specificReason) { this.specificReason = specificReason; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getDisposition() { return disposition; }
    public void setDisposition(String disposition) { this.disposition = disposition; }


}
