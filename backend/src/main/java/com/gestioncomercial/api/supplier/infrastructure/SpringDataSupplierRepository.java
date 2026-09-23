package com.gestioncomercial.api.supplier.infrastructure;

import com.gestioncomercial.api.supplier.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataSupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {
    boolean existsByRuc(String ruc);
    boolean existsByRucAndIdNot(String ruc, long id);
}
