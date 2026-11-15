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

    @Enumerated(EnumType.STRING)
    @Column(name = "xml_status", nullable = false)
    private StatusEnum xmlStatus; // WAITING_XML, IMPORTED

    @Column(name = "volume_quantity", nullable = false)
    private Integer volumeQuantity = 0; // Quantidade de caixas bipadas

    private String customerName;

    @OneToMany(mappedBy = "receivingInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceivingItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "receivingInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceHistoryEntity> events = new ArrayList<>();

    public List<InvoiceHistoryEntity> getEvents() { return events; }
    public void setEvents(List<InvoiceHistoryEntity> events) { this.events = events; }

    public void addEvent(InvoiceHistoryEntity event) {
        this.events.add(event);
        event.setReceivingInvoice(this);
    }




    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ReceivingEntity getReceiving() { return receiving; }
    public void setReceiving(ReceivingEntity receiving) { this.receiving = receiving; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public StatusEnum getXmlStatus() { return xmlStatus; }
    public void setXmlStatus(StatusEnum xmlStatus) { this.xmlStatus = xmlStatus; }
    public Integer getVolumeQuantity() { return volumeQuantity; }
    public void setVolumeQuantity(Integer volumeQuantity) { this.volumeQuantity = volumeQuantity; }
    public List<ReceivingItemEntity> getItems() { return items; }
    public void setItems(List<ReceivingItemEntity> items) { this.items = items; }
    public String getCustomerName() {return customerName;}
    public void setCustomerName(String customerName) {this.customerName = customerName;}
}