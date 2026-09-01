package ld.application.infra.db.jpa;

import ld.application.infra.db.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
    boolean existsByName(final String name);

    @Query("""
    select distinct p
    from ProductEntity p
    left join fetch p.colors
    left join fetch p.photos
    where p.id in :ids
""")
    List<ProductEntity> findAllWithColorsAndPhotosByIdIn(@Param("ids") Collection<UUID> ids);

    @Query("""
    select p
    from ProductEntity p
    left join fetch p.colors
    left join fetch p.photos
    where p.id = :id
    """)
    Optional<ProductEntity> findByIdWithColorsAndPhotos(@Param("id") UUID id);
}
