package com.gam.wms.repository;

import com.gam.wms.model.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, Integer> {
}

