package com.example.cvmanager.cv.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.cvmanager.auth.security.AuthenticatedUser;
import com.example.cvmanager.cv.dto.request.CvCreateRequest;
import com.example.cvmanager.cv.dto.request.CvUpdateRequest;
import com.example.cvmanager.cv.dto.response.CvListItemResponse;
import com.example.cvmanager.cv.dto.response.CvResponse;
import com.example.cvmanager.cv.service.CvService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cvs")
public class CvController {
    private final CvService cvService;

    public CvController(CvService cvService) {
        this.cvService = cvService;
    }

    @GetMapping
    public List<CvListItemResponse> listCvs(@AuthenticationPrincipal AuthenticatedUser user) {
        return cvService.listCvs(user);
    }

    @GetMapping("/archived")
    public List<CvResponse> listArchivedCvs(@AuthenticationPrincipal AuthenticatedUser user) {
        return cvService.listArchivedCvs(user);
    }

    @GetMapping("/search")
    public List<CvListItemResponse> searchCvs(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam String q) {
        return cvService.searchCvs(user, q);
    }

    @GetMapping("/{id}")
    public CvResponse getCv(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        return cvService.getCv(user, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CvResponse createCv(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CvCreateRequest request) {
        return cvService.createCv(user, request);
    }

    @PutMapping("/{id}")
    public CvResponse updateCv(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody CvUpdateRequest request) {
        return cvService.updateCv(user, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDeleteCv(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        cvService.softDeleteCv(user, id);
    }

    @PostMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveCv(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        cvService.archiveCv(user, id);
    }

    @PostMapping("/{id}/unarchive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unarchiveCv(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        cvService.unarchiveCv(user, id);
    }
}
