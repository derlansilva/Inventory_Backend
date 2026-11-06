package com.gam.wms.repository;

import com.gam.wms.model.ManifestEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManifestRepository extends JpaRepository<ManifestEntity, Long> {
    Optional<ManifestEntity> findByManifestCode(String manifestCode);
}