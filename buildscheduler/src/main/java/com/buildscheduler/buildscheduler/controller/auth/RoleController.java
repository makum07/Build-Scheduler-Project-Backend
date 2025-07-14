package com.buildscheduler.buildscheduler.controller.auth;

import com.buildscheduler.buildscheduler.dto.auth.RoleDto;
import com.buildscheduler.buildscheduler.mapper.RoleMapper;
import com.buildscheduler.buildscheduler.repository.RoleRepository;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleController(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        List<RoleDto> roles = roleRepository.findAll().stream().map(roleMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ofSuccess("Roles retrieved successfully", roles));
    }
}