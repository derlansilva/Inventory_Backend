package com.gam.wms.repository;

import com.gam.wms.model.ReceivingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReceivingRepository extends JpaRepository<ReceivingEntity, Long> {
    Optional<ReceivingEntity> findByReceivingCode(String receivingCode);
}