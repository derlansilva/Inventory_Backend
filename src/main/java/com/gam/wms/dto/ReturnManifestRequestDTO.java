package com.gam.wms.dto;

import java.time.LocalDate;
import java.util.List;

public class ReturnManifestRequestDTO {

    private String manifestCode;
    private List<InvoiceDataDTO> invoices;

    public static class InvoiceDataDTO {
        private String nfNumber;
        private String customerName;
        private String salesOrg;
        private String distributionChannel;
        private List<ItemDataDTO> items;

        // Getters e Setters
        public String getNfNumber() { return nfNumber; }
        public void setNfNumber(String nfNumber) { this.nfNumber = nfNumber; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getSalesOrg() { return salesOrg; }
        public void setSalesOrg(String salesOrg) { this.salesOrg = salesOrg; }
        public String getDistributionChannel() { return distributionChannel; }
        public void setDistributionChannel(String distributionChannel) { this.distributionChannel = distributionChannel; }
        public List<ItemDataDTO> getItems() { return items; }
        public void setItems(List<ItemDataDTO> items) { this.items = items; }
    }

    public static class ItemDataDTO {
        private String materialCode;
        private String description;
        private Integer noteQty;
        private Integer checkedQty;
        private Integer damagedQty;
        private String specificReason;
        private String batch;
        private LocalDate expiryDate;
        private String disposition;

        private Integer availableQty; // Estoque Disponível
        private Integer reservedQty;  // Estoque Reservado

        // Getters e Setters
        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getNoteQty() { return noteQty; }
        public void setNoteQty(Integer noteQty) { this.noteQty = noteQty; }
        public Integer getCheckedQty() { return checkedQty; }
        public void setCheckedQty(Integer checkedQty) { this.checkedQty = checkedQty; }
        public Integer getDamagedQty() { return damagedQty; }
        public void setDamagedQty(Integer damagedQty) { this.damagedQty = damagedQty; }
        public String getSpecificReason() { return specificReason; }
        public void setSpecificReason(String specificReason) { this.specificReason = specificReason; }
        public String getBatch() { return batch; }
        public void setBatch(String batch) { this.batch = batch; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public String getDisposition() { return disposition; }
        public void setDisposition(String disposition) { this.disposition = disposition; }
        public Integer getAvailableQty() { return availableQty; }
        public void setAvailableQty(Integer availableQty) { this.availableQty = availableQty; }

        public Integer getReservedQty() { return reservedQty; }
        public void setReservedQty(Integer reservedQty) { this.reservedQty = reservedQty; }
    }

    // Getters e Setters principais
    public String getManifestCode() { return manifestCode; }
    public void setManifestCode(String manifestCode) { this.manifestCode = manifestCode; }
    public List<InvoiceDataDTO> getInvoices() { return invoices; }
    public void setInvoices(List<InvoiceDataDTO> invoices) { this.invoices = invoices; }
}
