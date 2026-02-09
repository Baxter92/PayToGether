package com.ulr.paytogether.provider.repository;
import com.ulr.paytogether.provider.adapter.entity.PubliciteJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
/**
 * Repository pour l'entité Publicite
 */
@Repository
public interface PubliciteRepository extends JpaRepository<PubliciteJpa, UUID> {

    /**
     * Trouver les publicités actives
     */
    List<PubliciteJpa> findByActiveTrue();
}
