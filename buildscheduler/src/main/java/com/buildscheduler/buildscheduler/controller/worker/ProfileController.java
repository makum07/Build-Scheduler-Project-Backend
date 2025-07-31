package com.buildscheduler.buildscheduler.controller.worker;

import com.buildscheduler.buildscheduler.dto.worker.*;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.custom.ProfileService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/worker/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // Add your @PreAuthorize checks for "WORKER" here if needed

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillDto>> addSkill(@RequestBody SkillDto dto) {
        SkillDto result = profileService.addSkill(dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Skill added successfully", result));
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Void>> removeSkill(@PathVariable Long skillId) {
        profileService.removeSkill(skillId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Skill removed successfully", null));
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillDto>>> getAllSkills() {
        List<SkillDto> skills = profileService.getAllSkills();
        return ResponseEntity.ok(ApiResponse.ofSuccess("Skills retrieved successfully", skills));
    }

    @PostMapping("/certifications")
    public ResponseEntity<ApiResponse<Void>> addCertification(@RequestBody String certification) {
        profileService.addCertification(certification);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Certification added successfully", null));
    }

    @DeleteMapping("/certifications/{certification}")
    public ResponseEntity<ApiResponse<Void>> removeCertification(@PathVariable String certification) {
        profileService.removeCertification(certification);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Certification removed successfully", null));
    }

    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<AvailabilitySlotDto>> addAvailabilitySlot(@RequestBody AvailabilitySlotDto dto) {
        AvailabilitySlotDto result = profileService.addAvailabilitySlot(dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Availability slot added successfully", result));
    }

    @PutMapping("/availability/bulk")
    public ResponseEntity<ApiResponse<List<AvailabilitySlotDto>>> updateAvailabilitySlots(@RequestBody BulkAvailabilityDto dto) {
        List<AvailabilitySlotDto> result = profileService.updateAvailabilitySlots(dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Availability slots updated successfully", result));
    }

    @DeleteMapping("/availability/{slotId}")
    public ResponseEntity<ApiResponse<Void>> removeAvailabilitySlot(@PathVariable Long slotId) {
        profileService.removeAvailabilitySlot(slotId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Availability slot removed successfully", null));
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<List<AvailabilitySlotDto>>> getAvailabilitySlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<AvailabilitySlotDto> slots = profileService.getAvailabilitySlots(start, end);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Availability slots retrieved", slots));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileDto>> getUserProfile() {
        ProfileDto profile = profileService.getUserProfile();
        return ResponseEntity.ok(ApiResponse.ofSuccess("User profile retrieved", profile));
    }

    @GetMapping("/my-skills")
    public ResponseEntity<ApiResponse<List<SkillDto>>> getMySkills() {
        List<SkillDto> skills = profileService.getMySkills();
        return ResponseEntity.ok(ApiResponse.ofSuccess("User skills retrieved successfully", skills));
    }
    @GetMapping("/certifications")
    public ResponseEntity<ApiResponse<Set<String>>> getMyCertifications() {
        Set<String> certifications = profileService.getMyCertifications();
        return ResponseEntity.ok(ApiResponse.ofSuccess("User certifications retrieved successfully", certifications));
    }
    @GetMapping("/availability/all")
    public ResponseEntity<ApiResponse<List<AvailabilitySlotDto>>> getAllAvailabilitySlots() {
        List<AvailabilitySlotDto> slots = profileService.getAllAvailabilitySlots();
        return ResponseEntity.ok(ApiResponse.ofSuccess("All availability slots retrieved", slots));
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<Set<WorkerAssignmentDetailsDto>>> getMyAssignments() {
        Set<WorkerAssignmentDetailsDto> assignments = profileService.getMyAssignments();
        return ResponseEntity.ok(ApiResponse.ofSuccess("User assignments retrieved successfully", assignments));
    }
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<ProjectForWorkerDto>>> getMyProjects() {
        List<ProjectForWorkerDto> projects = profileService.getWorkerProjectsWithCompletion();
        return ResponseEntity.ok(ApiResponse.ofSuccess("Projects for the worker retrieved successfully", projects));
    }

}
