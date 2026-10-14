package com.gestioncomercial.api.sale.infrastructure;

import com.gestioncomercial.api.sale.domain.Sale;
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

interface SpringDataSaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {
    @Override
    @EntityGraph(attributePaths = {"customer", "lines", "lines.product"})
    Optional<Sale> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Sale p where p.id = :id")
    Optional<Sale> findByIdForUpdate(@Param("id") long id);

    @Override
    @EntityGraph(attributePaths = "customer")
    Page<Sale> findAll(Specification<Sale> specification, Pageable pageable);
}
