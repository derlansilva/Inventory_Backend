package com.gam.wms.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_stock_movement")
public class StockMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String materialCode;
    private String description;
    private Integer quantityMoved;

    private String movementType; // Ex: "DEVOLUCAO", "AVARIA"
    private String invoiceNumber;
    private String manifestCode; // Romaneio
    private String userName; // Operador
    private String status; // "FINALIZADO"

    private LocalDateTime movementDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_invoice_id") // Cria a coluna FK no banco
    private ReceivingInvoiceEntity receivingInvoice;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public Integer getQuantityMoved() {
        return quantityMoved;
    }

    public void setQuantityMoved(Integer quantityMoved) {
        this.quantityMoved = quantityMoved;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getManifestCode() {
        return manifestCode;
    }

    public void setManifestCode(String manifestCode) {
        this.manifestCode = manifestCode;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ReceivingInvoiceEntity getReceivingInvoice() {
        return receivingInvoice;
    }

    public void setReceivingInvoice(ReceivingInvoiceEntity receivingInvoice) {
        this.receivingInvoice = receivingInvoice;
    }
}
