package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.PointFort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité PointFort
 */
@Repository
public interface PointFortRepository extends JpaRepository<PointFort, UUID> {

    /**
     * Recherche tous les points forts d'un deal
     * @param dealUuid l'UUID du deal
     * @return la liste des points forts
     */
    List<PointFort> findByDealUuid(UUID dealUuid);
}
