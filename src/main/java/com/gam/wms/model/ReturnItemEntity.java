package com.gam.wms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "return_item")
public class ReturnItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id") // ou o nome da coluna correspondente na tua tabela tb_return_item
    private InvoiceEntity invoice;

    @Column(name = "material_code", nullable = false)
    private String materialCode;

    @Column(name = "note_qty", nullable = false)
    private Integer noteQty;

    @Column(name = "checked_qty", nullable = false)
    private Integer checkedQty = 0;

    @Column(name = "damaged_qty", nullable = false)
    private Integer damagedQty = 0;

    @Column(name = "batch")
    private String batch;

    @Column(name = "specific_reason")
    private String specificReason;

    public String getSpecificReason() { return specificReason; }
    public void setSpecificReason(String specificReason) { this.specificReason = specificReason; }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InvoiceEntity getInvoice() { return invoice; }
    public void setInvoice(InvoiceEntity invoice) { this.invoice = invoice; }

    // Compatibilidade com o teu Service (linha 53): aceita o ID ou a Invoice
    public void setInvoiceId(Long invoiceId) {
        if (invoiceId != null) {
            InvoiceEntity inv = new InvoiceEntity();
            inv.setId(invoiceId);
            this.invoice = inv;
        }
    }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public Integer getNoteQty() { return noteQty; }
    public void setNoteQty(Integer noteQty) { this.noteQty = noteQty; }

    public Integer getCheckedQty() { return checkedQty; }
    public void setCheckedQty(Integer checkedQty) { this.checkedQty = checkedQty; }

    public Integer getDamagedQty() { return damagedQty; }
    public void setDamagedQty(Integer damagedQty) { this.damagedQty = damagedQty; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
}