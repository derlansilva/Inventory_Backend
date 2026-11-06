package com.gam.wms.repository;

import com.gam.wms.model.ReceivingInvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceivingInvoiceRepository extends JpaRepository<ReceivingInvoiceEntity, Long> {
    Optional<ReceivingInvoiceEntity> findByReceivingIdAndInvoiceNumber(Long receivingId, String invoiceNumber);

    // O método que o seu Controller está tentando usar:
    Optional<ReceivingInvoiceEntity> findByInvoiceNumber(String invoiceNumber);


    List<ReceivingInvoiceEntity> findAllByInvoiceNumber(String invoiceNumber);

}