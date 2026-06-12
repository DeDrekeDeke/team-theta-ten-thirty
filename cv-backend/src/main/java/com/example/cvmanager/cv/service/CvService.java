package com.example.cvmanager.cv.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cvmanager.auth.security.AuthenticatedUser;
import com.example.cvmanager.common.exception.NotFoundException;
import com.example.cvmanager.common.security.AdminAccessService;
import com.example.cvmanager.cv.dto.request.CvCreateRequest;
import com.example.cvmanager.cv.dto.request.CvEducationEntryRequest;
import com.example.cvmanager.cv.dto.request.CvLanguageRequest;
import com.example.cvmanager.cv.dto.request.CvLinkRequest;
import com.example.cvmanager.cv.dto.request.CvPersonalDetailsRequest;
import com.example.cvmanager.cv.dto.request.CvSkillRequest;
import com.example.cvmanager.cv.dto.request.CvUpdateRequest;
import com.example.cvmanager.cv.dto.request.CvWorkExperienceEntryRequest;
import com.example.cvmanager.cv.dto.response.CvListItemResponse;
import com.example.cvmanager.cv.dto.response.CvResponse;
import com.example.cvmanager.cv.mapper.CvMapper;
import com.example.cvmanager.cv.model.Cv;
import com.example.cvmanager.cv.model.CvEducationEntry;
import com.example.cvmanager.cv.model.CvLanguage;
import com.example.cvmanager.cv.model.CvLink;
import com.example.cvmanager.cv.model.CvPersonalDetails;
import com.example.cvmanager.cv.model.CvSkill;
import com.example.cvmanager.cv.model.CvWorkExperienceEntry;
import com.example.cvmanager.cv.repository.CvRepository;
import com.example.cvmanager.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CvService {

    private final CvRepository cvRepository;
    private final UserRepository userRepository;
    private final CvMapper cvMapper;
    private final AdminAccessService adminAccessService;

    @Transactional(readOnly = true)
    public List<CvListItemResponse> listCvs(AuthenticatedUser user) {
        if (user.admin()) {
            return cvRepository.findActiveListItems();
        }

        return cvRepository.findActiveListItemsByOwner(user.userId());
    }

    @Transactional
    public void archiveCv(AuthenticatedUser user, Long id) {
        Cv cv = findAuthorizedCv(user, id);
        cv.archive();
        cvRepository.save(cv);
    }

    @Transactional(readOnly = true)
    public CvResponse getCv(AuthenticatedUser user, Long id) {
        return cvMapper.toResponse(findAuthorizedCv(user, id));
    }

    @Transactional(readOnly = true)
    public List<CvListItemResponse> searchCvs(AuthenticatedUser user, String q) {
        if (q == null || q.isBlank()) {
            return listCvs(user);
        }

        String query = q.trim();
        if (user.admin()) {
            return cvRepository.searchActiveListItems(query);
        }
        return cvRepository.searchActiveListItemsByOwner(user.userId(), query);
    }

    @Transactional
    public CvResponse createCv(AuthenticatedUser user, CvCreateRequest request) {
        adminAccessService.requireOwnerOrAdmin(user, request.ownerUserId());
        var owner = userRepository.findById(request.ownerUserId())
                .orElseThrow(() -> new NotFoundException("Owner user not found", "USER_NOT_FOUND"));

        Cv cv = new Cv(owner, request.title());
        cv.setSummary(request.summary());
        applyStructuredFields(cv, request.personalDetails(), request.educationEntries(), request.workExperienceEntries(),
                request.skills(), request.languages(), request.links());
        return cvMapper.toResponse(cvRepository.save(cv));
    }

    @Transactional
    public CvResponse updateCv(AuthenticatedUser user, Long id, CvUpdateRequest request) {
        Cv cv = findAuthorizedCv(user, id);
        cv.setTitle(request.title());
        cv.setSummary(request.summary());
        applyStructuredFields(cv, request.personalDetails(), request.educationEntries(), request.workExperienceEntries(),
                request.skills(), request.languages(), request.links());
        return cvMapper.toResponse(cvRepository.save(cv));
    }

    @Transactional(readOnly = true)
    public Cv findCv(Long id) {
        return cvRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("CV not found", "CV_NOT_FOUND"));
    }

    @Transactional(readOnly = true)
    public Cv findAuthorizedCv(AuthenticatedUser user, Long id) {
        Cv cv = findCv(id);
        adminAccessService.requireOwnerOrAdmin(user, cv.getOwner().getId());
        return cv;
    }

    private void applyStructuredFields(
            Cv cv,
            CvPersonalDetailsRequest personalDetails,
            List<CvEducationEntryRequest> educationEntries,
            List<CvWorkExperienceEntryRequest> workExperienceEntries,
            List<CvSkillRequest> skills,
            List<CvLanguageRequest> languages,
            List<CvLinkRequest> links) {
        if (personalDetails != null) {
            applyPersonalDetails(cv, personalDetails);
        }
        if (educationEntries != null) {
            replaceEducationEntries(cv, educationEntries);
        }
        if (workExperienceEntries != null) {
            replaceWorkExperienceEntries(cv, workExperienceEntries);
        }
        if (skills != null) {
            replaceSkills(cv, skills);
        }
        if (languages != null) {
            replaceLanguages(cv, languages);
        }
        if (links != null) {
            replaceLinks(cv, links);
        }
    }

    private void applyPersonalDetails(Cv cv, CvPersonalDetailsRequest request) {
        CvPersonalDetails personalDetails = cv.getPersonalDetails();
        if (personalDetails == null) {
            personalDetails = new CvPersonalDetails(cv);
            cv.setPersonalDetails(personalDetails);
        }

        personalDetails.setFullName(request.fullName());
        personalDetails.setEmail(request.email());
        personalDetails.setPhone(request.phone());
        personalDetails.setLocation(request.location());
        personalDetails.setHeadline(request.headline());
    }

    private void replaceEducationEntries(Cv cv, List<CvEducationEntryRequest> requests) {
        cv.getEducationEntries().clear();
        for (CvEducationEntryRequest request : requests) {
            CvEducationEntry entry = new CvEducationEntry(cv, request.institution());
            entry.setDegree(request.degree());
            entry.setFieldOfStudy(request.fieldOfStudy());
            entry.setStartDate(request.startDate());
            entry.setEndDate(request.endDate());
            entry.setDescription(request.description());
            entry.setDisplayOrder(request.displayOrder());
            cv.getEducationEntries().add(entry);
        }
    }

    private void replaceWorkExperienceEntries(Cv cv, List<CvWorkExperienceEntryRequest> requests) {
        cv.getWorkExperienceEntries().clear();
        for (CvWorkExperienceEntryRequest request : requests) {
            CvWorkExperienceEntry entry = new CvWorkExperienceEntry(cv, request.employer(), request.jobTitle());
            entry.setLocation(request.location());
            entry.setStartDate(request.startDate());
            entry.setEndDate(request.endDate());
            entry.setDescription(request.description());
            entry.setDisplayOrder(request.displayOrder());
            cv.getWorkExperienceEntries().add(entry);
        }
    }

    private void replaceSkills(Cv cv, List<CvSkillRequest> requests) {
        cv.getSkills().clear();
        for (CvSkillRequest request : requests) {
            CvSkill skill = new CvSkill(cv, request.name());
            skill.setCategory(request.category());
            skill.setProficiency(request.proficiency());
            skill.setDisplayOrder(request.displayOrder());
            cv.getSkills().add(skill);
        }
    }

    private void replaceLanguages(Cv cv, List<CvLanguageRequest> requests) {
        cv.getLanguages().clear();
        for (CvLanguageRequest request : requests) {
            CvLanguage language = new CvLanguage(cv, request.name());
            language.setProficiency(request.proficiency());
            language.setDisplayOrder(request.displayOrder());
            cv.getLanguages().add(language);
        }
    }

    private void replaceLinks(Cv cv, List<CvLinkRequest> requests) {
        cv.getLinks().clear();
        for (CvLinkRequest request : requests) {
            CvLink link = new CvLink(cv, request.label(), request.url());
            link.setDisplayOrder(request.displayOrder());
            cv.getLinks().add(link);
        }
    }
}
