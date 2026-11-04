package com.gam.wms.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tb_manifest")
public class ManifestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="manfest_code" , nullable = false , unique = true)
    private String manifestCode;

    @Column(nullable = false)
    private String status;

    @Column(name="created_at")
    private LocalDateTime createdAt =  LocalDateTime.now();

    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceEntity> invoices;

    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimelineEntity> timelineHistories;

    public ManifestEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getManifestCode() {
        return manifestCode;
    }

    public void setManifestCode(String manfestCode) {
        this.manifestCode = manfestCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<InvoiceEntity> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<InvoiceEntity> invoices) {
        this.invoices = invoices;
    }

    public List<TimelineEntity> getTimelineHistories() {
        return timelineHistories;
    }

    public void setTimelineHistories(List<TimelineEntity> timelineHistories) {
        this.timelineHistories = timelineHistories;
    }
}
