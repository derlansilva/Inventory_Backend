package com.gam.wms.repository;


import com.gam.wms.model.WarehouseStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseStockRepository extends JpaRepository<WarehouseStockEntity, Long> {
    // Busca a linha exata do produto com o tipo de estoque (ex: 05-DEVOLUCAO)
    List<WarehouseStockEntity> findByMaterialCodeAndStockType(String materialCode, String stockType);
}