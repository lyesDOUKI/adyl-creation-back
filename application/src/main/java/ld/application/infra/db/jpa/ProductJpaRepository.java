package ld.application.infra.db.jpa;

import ld.application.infra.db.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
    boolean existsByName(final String name);

    @Query("""
        select distinct p
        from Product p
        left join fetch p.colors
        where p.id in :ids
    """)
    List<Product> findAllWithColorsByIdIn(@Param("ids") Collection<UUID> ids);
}
