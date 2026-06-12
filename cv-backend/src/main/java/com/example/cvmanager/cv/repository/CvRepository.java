package com.example.cvmanager.cv.repository;

import com.example.cvmanager.cv.model.Cv;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CvRepository extends JpaRepository<Cv, Long> {

    List<Cv> findByOwnerIdAndArchivedAtIsNullAndDeletedAtIsNullAndOwnerDeletedAtIsNull(Long ownerId, Sort sort);

    @Query("""
            SELECT cv FROM Cv cv
            JOIN cv.owner owner
            WHERE cv.archivedAt IS NULL
                AND cv.deletedAt IS NULL
                AND owner.deletedAt IS NULL
                AND (
                    lower(cv.title) LIKE lower(concat('%', :query, '%'))
                    OR lower(owner.email) LIKE lower(concat('%', :query, '%'))
                    OR lower(cv.summary) LIKE lower(concat('%', :query, '%'))
                )
            ORDER BY cv.updatedAt DESC
            """)
    List<Cv> search(@Param("query") String query);

    @Query("""
            SELECT cv FROM Cv cv
            JOIN cv.owner owner
            WHERE cv.archivedAt IS NULL
                AND cv.deletedAt IS NULL
                AND owner.deletedAt IS NULL
                AND cv.owner.id = :ownerId
                AND (
                    lower(cv.title) LIKE lower(concat('%', :query, '%'))
                    OR lower(owner.email) LIKE lower(concat('%', :query, '%'))
                )
            ORDER BY cv.updatedAt DESC
            """)
    List<Cv> searchByOwner(@Param("ownerId") Long ownerId, @Param("query") String query);

    List<Cv> findByArchivedAtIsNullAndDeletedAtIsNullAndOwnerDeletedAtIsNull(Sort sort);

    Optional<Cv> findByIdAndArchivedAtIsNullAndDeletedAtIsNullAndOwnerDeletedAtIsNull(Long id);

    @Modifying
    @Query("""
            UPDATE Cv cv
            SET cv.deletedAt = :deletedAt
            WHERE cv.owner.id = :ownerId
                AND cv.deletedAt IS NULL
            """)
    int markDeletedByOwnerId(@Param("ownerId") Long ownerId, @Param("deletedAt") LocalDateTime deletedAt);
}
