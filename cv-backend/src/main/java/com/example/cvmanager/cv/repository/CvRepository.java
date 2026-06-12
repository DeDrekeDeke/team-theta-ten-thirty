package com.example.cvmanager.cv.repository;

import com.example.cvmanager.cv.dto.response.CvListItemResponse;
import com.example.cvmanager.cv.model.Cv;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CvRepository extends JpaRepository<Cv, Long> {

    @Query("""
            SELECT new com.example.cvmanager.cv.dto.response.CvListItemResponse(
                cv.id,
                owner.id,
                owner.email,
                cv.title,
                cv.summary,
                cv.createdAt,
                cv.updatedAt
            )
            FROM Cv cv
            JOIN cv.owner owner
            WHERE cv.archivedAt IS NULL
                AND cv.deletedAt IS NULL
                AND owner.deletedAt IS NULL
            ORDER BY cv.updatedAt DESC
            """)
    List<CvListItemResponse> findActiveListItems();

    @Query("""
            SELECT new com.example.cvmanager.cv.dto.response.CvListItemResponse(
                cv.id,
                owner.id,
                owner.email,
                cv.title,
                cv.summary,
                cv.createdAt,
                cv.updatedAt
            )
            FROM Cv cv
            JOIN cv.owner owner
            WHERE cv.archivedAt IS NULL
                AND cv.deletedAt IS NULL
                AND owner.deletedAt IS NULL
                AND owner.id = :ownerId
            ORDER BY cv.updatedAt DESC
            """)
    List<CvListItemResponse> findActiveListItemsByOwner(@Param("ownerId") Long ownerId);

    @Query("""
            SELECT new com.example.cvmanager.cv.dto.response.CvListItemResponse(
                cv.id,
                owner.id,
                owner.email,
                cv.title,
                cv.summary,
                cv.createdAt,
                cv.updatedAt
            )
            FROM Cv cv
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
    List<CvListItemResponse> searchActiveListItems(@Param("query") String query);

    @Query("""
            SELECT new com.example.cvmanager.cv.dto.response.CvListItemResponse(
                cv.id,
                owner.id,
                owner.email,
                cv.title,
                cv.summary,
                cv.createdAt,
                cv.updatedAt
            )
            FROM Cv cv
            JOIN cv.owner owner
            WHERE cv.archivedAt IS NULL
                AND cv.deletedAt IS NULL
                AND owner.deletedAt IS NULL
                AND owner.id = :ownerId
                AND (
                    lower(cv.title) LIKE lower(concat('%', :query, '%'))
                    OR lower(owner.email) LIKE lower(concat('%', :query, '%'))
                    OR lower(cv.summary) LIKE lower(concat('%', :query, '%'))
                )
            ORDER BY cv.updatedAt DESC
            """)
    List<CvListItemResponse> searchActiveListItemsByOwner(
            @Param("ownerId") Long ownerId,
            @Param("query") String query);

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
