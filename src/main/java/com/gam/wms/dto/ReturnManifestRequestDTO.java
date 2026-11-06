package com.gam.wms.dto;

import java.util.List;

public class ReturnManifestRequestDTO {

    private String manifestCode;
    private List<InvoiceDTO> invoices;

    public String getManifestCode() {
        return manifestCode;
    }

    public void setManifestCode(String manifestCode) {
        this.manifestCode = manifestCode;
    }

    public List<InvoiceDTO> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<InvoiceDTO> invoices) {
        this.invoices = invoices;
    }
}