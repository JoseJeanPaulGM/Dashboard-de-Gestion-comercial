package com.gestioncomercial.api.purchase.infrastructure;

import com.gestioncomercial.api.purchase.domain.Purchase;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataPurchaseRepository extends JpaRepository<Purchase, Long>, JpaSpecificationExecutor<Purchase> {
    @Override
    @EntityGraph(attributePaths = {"supplier", "lines", "lines.product"})
    Optional<Purchase> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Purchase p where p.id = :id")
    Optional<Purchase> findByIdForUpdate(@Param("id") long id);

    @Override
    @EntityGraph(attributePaths = "supplier")
    Page<Purchase> findAll(Specification<Purchase> specification, Pageable pageable);
}
