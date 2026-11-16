package com.openshelf.admin.controller;

import com.openshelf.admin.dto.UserDtos;
import com.openshelf.admin.service.AdminUserService;
import com.openshelf.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<List<UserDtos.UserResponse>> list() {
        return ApiResponse.ok(adminUserService.list());
    }

    @PostMapping
    public ApiResponse<UserDtos.UserResponse> create(@Valid @RequestBody UserDtos.CreateUserRequest request) {
        return ApiResponse.ok(adminUserService.create(request), "User created");
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDtos.UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserDtos.UpdateUserRequest request) {
        return ApiResponse.ok(adminUserService.update(id, request), "User updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return ApiResponse.ok(null, "User deleted");
    }
}
