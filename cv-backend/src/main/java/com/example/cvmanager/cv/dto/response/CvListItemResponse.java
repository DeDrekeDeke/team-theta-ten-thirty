package com.example.cvmanager.cv.dto.response;

import java.time.LocalDateTime;

public record CvListItemResponse(
        Long id,
        Long ownerUserId,
        String ownerEmail,
        String title,
        String summary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
