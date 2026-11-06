package com.gam.wms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse_stock")
public class WarehouseStockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_code", nullable = false)
    private String materialCode;

    @Column(name = "stock_type", nullable = false)
    private String stockType; // Ex: '01-DISPONIVEL', '05-DEVOLUCAO'

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column
    private String batch;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getStockType() { return stockType; }
    public void setStockType(String stockType) { this.stockType = stockType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
}