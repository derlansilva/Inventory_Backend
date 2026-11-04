package com.gam.wms.repository;

import com.gam.wms.model.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity , Long> {

    Optional<InvoiceEntity> findByNfNumber(String nfNumber);
}
