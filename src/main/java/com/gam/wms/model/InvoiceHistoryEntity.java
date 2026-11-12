package com.gam.wms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_invoice_history")
public class InvoiceHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status; // Ex: "CRIADA", "XML_IMPORTADO", "EM_CONFERENCIA", "FINALIZADA"
    private String message; // Ex: "Recebimento cego iniciado", "Conferência finalizada com 2 faltas"
    private String userName; // Ex: "KETELIN.ANTONELO"

    private LocalDateTime eventDate = LocalDateTime.now();

    // Relacionamento: Muitos eventos pertencem a uma NF
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_invoice_id")
    private ReceivingInvoiceEntity receivingInvoice;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public ReceivingInvoiceEntity getReceivingInvoice() { return receivingInvoice; }
    public void setReceivingInvoice(ReceivingInvoiceEntity receivingInvoice) { this.receivingInvoice = receivingInvoice; }
}