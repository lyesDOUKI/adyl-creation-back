package ld.application.infra.db.jpa;

import ld.application.infra.db.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

    @Query("""
        select p.id
        from Product p
        where p.id in :ids
    """)
    Set<UUID> findExistingIds(Collection<UUID> ids);
}
