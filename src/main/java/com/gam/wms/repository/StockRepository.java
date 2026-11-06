package com.gam.wms.repository;

import com.gam.wms.model.WarehouseStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<WarehouseStockEntity, Long> {

    Optional<WarehouseStockEntity> findByMaterialCodeAndStockTypeAndBatch(
            String materialCode,
            String stockType,
            String batch
    );

    default void addOrUpdateStock(String materialCode, String stockType, int quantity, String batch) {
        WarehouseStockEntity stock = findByMaterialCodeAndStockTypeAndBatch(materialCode, stockType, batch)
                .orElseGet(() -> {
                    WarehouseStockEntity newStock = new WarehouseStockEntity();
                    newStock.setMaterialCode(materialCode);
                    newStock.setStockType(stockType);
                    newStock.setBatch(batch);
                    newStock.setQuantity(0);
                    return newStock;
                });

        stock.setQuantity(stock.getQuantity() + quantity);
        save(stock);
    }
}