package com.gam.wms.dto;

import java.util.List;

public class InvoiceDTO {

    private String nfNumber;
    private String customerName;
    private List<ReturnItemDTO> items;

    public String getNfNumber() {
        return nfNumber;
    }

    public void setNfNumber(String nfNumber) {
        this.nfNumber = nfNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<ReturnItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ReturnItemDTO> items) {
        this.items = items;
    }
}