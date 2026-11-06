package com.gam.wms.repository;

import com.gam.wms.model.ReturnItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItemEntity, Long> {
    @Modifying
    @Query("""
    UPDATE ReturnItemEntity r 
    SET r.checkedQty = :checkedQty, r.damagedQty = :damagedQty 
    WHERE r.invoice.nfNumber = :nfNumber AND r.materialCode = :materialCode
""")
    void updateCheckedQtyByMaterialAndInvoice(
            @Param("nfNumber") String nfNumber,
            @Param("materialCode") String materialCode,
            @Param("checkedQty") Integer checkedQty,
            @Param("damagedQty") Integer damagedQty
    );

}
