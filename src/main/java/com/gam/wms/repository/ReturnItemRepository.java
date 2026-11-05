package com.gam.wms.repository;

import com.gam.wms.model.ReturnItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItemEntity, Long> {


}
