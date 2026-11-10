package com.gam.wms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_receiving")
public class ReceivingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vínculo com o Manifesto da Transportadora (MDF-e / Caminhão)
    @ManyToOne
    @JoinColumn(name = "manifest_id")
    private ManifestEntity manifest;

    @Column(name = "receiving_code", unique = true, nullable = false)
    private String receivingCode; // Ex: REC-984512

    @Column(name = "status", nullable = false)
    private StatusEnum status; // OPEN, CLOSED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "receiving", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceivingInvoiceEntity> invoices = new ArrayList<>();

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ManifestEntity getManifest() {
        return manifest;
    }

    public void setManifest(ManifestEntity manifest) {
        this.manifest = manifest;
    }

    public String getReceivingCode() { return receivingCode; }
    public void setReceivingCode(String receivingCode) { this.receivingCode = receivingCode; }
    public StatusEnum getStatus() { return status; }
    public void setStatus(StatusEnum status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ReceivingInvoiceEntity> getInvoices() { return invoices; }
    public void setInvoices(List<ReceivingInvoiceEntity> invoices) { this.invoices = invoices; }
}