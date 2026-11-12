package com.gam.wms.service;

import com.gam.wms.model.InvoiceHistoryEntity;
import com.gam.wms.model.ReceivingInvoiceEntity;
import com.gam.wms.repository.InvoiceHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class InvoiceLogService {

    private final InvoiceHistoryRepository historyRepository;

    public InvoiceLogService(InvoiceHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    // MÉTODO PADRÃO PARA GRAVAR LOG NA NF
    public void logEvent(ReceivingInvoiceEntity invoice, String status, String message, String userName) {
        InvoiceHistoryEntity event = new InvoiceHistoryEntity();
        event.setStatus(status);
        event.setMessage(message);
        event.setUserName(userName != null ? userName : "SISTEMA");

        // Amarra o evento à nota e salva
        invoice.addEvent(event);
        historyRepository.save(event);
    }
}