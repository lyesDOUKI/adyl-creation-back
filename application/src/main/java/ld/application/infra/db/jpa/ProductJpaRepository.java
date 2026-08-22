package ld.application.infra.db.jpa;

import ld.application.infra.db.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {}
