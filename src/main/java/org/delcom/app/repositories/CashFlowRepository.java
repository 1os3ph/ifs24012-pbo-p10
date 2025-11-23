package org.delcom.app.repositories;

import org.delcom.app.entities.CashFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CashFlowRepository extends JpaRepository<CashFlow, UUID> {
    
    // Ambil semua milik user tertentu
    List<CashFlow> findByUserId(UUID userId);

    // Ambil detail spesifik milik user tertentu (Security)
    Optional<CashFlow> findByUserIdAndId(UUID userId, UUID id);

    // Fitur Search (Mencari berdasarkan Label atau Description milik user tertentu)
    @Query("SELECT c FROM CashFlow c WHERE c.userId = :userId AND " +
           "(LOWER(c.label) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<CashFlow> findByKeyword(@Param("userId") UUID userId, @Param("keyword") String keyword);

    // Ambil daftar Label unik milik user tertentu
    @Query("SELECT DISTINCT c.label FROM CashFlow c WHERE c.userId = :userId")
    List<String> findDistinctLabelsByUserId(@Param("userId") UUID userId);
}