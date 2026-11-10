package com.gam.wms.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tb_manifest")
public class ManifestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="manifest_code", nullable = false, unique = true)
    private String manifestCode;

    @Column(name = "carrier_name")
    private String carrierName;

    @Column(nullable = false)
    private StatusEnum status;

    @Column(name="created_at")
    private LocalDateTime createdAt ;

    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"manifest", "hibernateLazyInitializer", "handler"})
    private List<InvoiceEntity> invoices = new ArrayList<>();

    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimelineEntity> timelineHistories = new ArrayList<>();

    public ManifestEntity() {
    }


    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
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

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
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
