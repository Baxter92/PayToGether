package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.CommissionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository pour l'entité Commission
 */
@Repository
public interface CommissionRepository extends JpaRepository<CommissionJpa, UUID> {
}
