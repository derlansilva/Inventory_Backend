package com.gam.wms.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_receiving_invoice")
public class ReceivingInvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receiving_id", nullable = false)
    private ReceivingEntity receiving;

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(name = "xml_status", nullable = false)
    private String xmlStatus; // WAITING_XML, IMPORTED

    @Column(name = "volume_quantity", nullable = false)
    private Integer volumeQuantity = 0; // Quantidade de caixas bipadas

    @OneToMany(mappedBy = "receivingInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceivingItemEntity> items = new ArrayList<>();

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ReceivingEntity getReceiving() { return receiving; }
    public void setReceiving(ReceivingEntity receiving) { this.receiving = receiving; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getXmlStatus() { return xmlStatus; }
    public void setXmlStatus(String xmlStatus) { this.xmlStatus = xmlStatus; }
    public Integer getVolumeQuantity() { return volumeQuantity; }
    public void setVolumeQuantity(Integer volumeQuantity) { this.volumeQuantity = volumeQuantity; }
    public List<ReceivingItemEntity> getItems() { return items; }
    public void setItems(List<ReceivingItemEntity> items) { this.items = items; }
}