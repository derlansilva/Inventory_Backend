package com.gam.wms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_receiving_item")
public class ReceivingItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receiving_invoice_id", nullable = false)
    private ReceivingInvoiceEntity receivingInvoice;

    @Column(name = "material_code", nullable = false)
    private String materialCode;

    @Column(name = "description")
    private String description;

    @Column(name = "note_quantity", nullable = false)
    private Integer noteQuantity;

    @Column(name = "batch")
    private String batch;

    // ADICIONE ESTE CAMPO (Inicia em zero)
    @Column(name = "scanned_quantity", nullable = false)
    private Integer scannedQuantity = 0;

    // ... getters e setters
    public Integer getScannedQuantity() { return scannedQuantity; }
    public void setScannedQuantity(Integer scannedQuantity) { this.scannedQuantity = scannedQuantity; }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ReceivingInvoiceEntity getReceivingInvoice() { return receivingInvoice; }
    public void setReceivingInvoice(ReceivingInvoiceEntity receivingInvoice) { this.receivingInvoice = receivingInvoice; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getNoteQuantity() { return noteQuantity; }
    public void setNoteQuantity(Integer noteQuantity) { this.noteQuantity = noteQuantity; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
}