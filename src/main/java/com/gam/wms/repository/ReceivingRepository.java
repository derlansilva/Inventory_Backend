package com.gam.wms.repository;

import com.gam.wms.model.ReceivingEntity;
import com.gam.wms.model.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceivingRepository extends JpaRepository<ReceivingEntity, Long> {
    Optional<ReceivingEntity> findByReceivingCode(String receivingCode);

    List<ReceivingEntity> findAllByManifestId(Long manifestId);

    Optional<ReceivingEntity> findByReceivingCodeAndStatus(String receivingCode, StatusEnum status);

}