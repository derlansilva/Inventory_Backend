package com.gam.wms.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_invoice")
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nf_number")
    private String nfNumber;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "sales_org")
    private String salesOrg;

    @Column(name = "distribution_channel")
    private String distributionChannel;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ReturnItemEntity> items = new ArrayList<>();

    // EVITA A RECURSÃO INFINITA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    @JsonIgnore
    private ManifestEntity manifest;

    // Construtores, Getters e Setters
    public InvoiceEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNfNumber() { return nfNumber; }
    public void setNfNumber(String nfNumber) { this.nfNumber = nfNumber; }

    public ManifestEntity getManifest() { return manifest; }
    public void setManifest(ManifestEntity manifest) { this.manifest = manifest; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getSalesOrg() { return salesOrg; }
    public void setSalesOrg(String salesOrg) { this.salesOrg = salesOrg; }

    public String getDistributionChannel() { return distributionChannel; }
    public void setDistributionChannel(String distributionChannel) { this.distributionChannel = distributionChannel; }

    public List<ReturnItemEntity> getItems() { return items; }
    public void setItems(List<ReturnItemEntity> items) { this.items = items; }
}
