package com.example.cvmanager.cv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.cvmanager.cv.dto.response.CvListItemResponse;
import com.example.cvmanager.cv.model.Cv;

public interface CvRepository extends JpaRepository<Cv, Long> {

    Optional<Cv> findByIdAndArchivedAtIsNull(Long id);

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
}
