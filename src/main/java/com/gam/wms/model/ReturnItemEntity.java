package com.gam.wms.model;


import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_return_item")
public class ReturnItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private InvoiceEntity invoice;

    @Column(name = "material_code", nullable = false)
    private String materialCode;

    @Column(nullable = false)
    private String description;

    @Column(name = "note_qty", nullable = false)
    private Integer noteQty;

    @Column(name = "checked_qty")
    private Integer checkedQty = 0;

    @Column(name = "damaged_qty")
    private Integer damagedQty = 0;

    @Column(name = "specific_reason")
    private String specificReason;

    private String batch;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    private String disposition;

    // Construtores, Getters e Setters
    public ReturnItemEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InvoiceEntity getInvoice() { return invoice; }
    public void setInvoice(InvoiceEntity invoice) { this.invoice = invoice; }

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
}
