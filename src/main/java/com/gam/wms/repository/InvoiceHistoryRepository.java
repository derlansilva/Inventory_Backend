package com.gam.wms.repository;

import com.gam.wms.model.InvoiceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceHistoryRepository  extends JpaRepository<InvoiceHistoryEntity, Long> {
}
