package com.nexushr.controller;

import com.nexushr.dto.NotificationDto;
import com.nexushr.security.EmployeePrincipal;
import com.nexushr.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notification Module", description = "Endpoints for creating, sending, listing, and reading system notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Create and dispatch system notification", description = "Privileged users only. Logs simulated delivery.")
    public ResponseEntity<NotificationDto.Response> createNotification(@Valid @RequestBody NotificationDto.Request request) {
        NotificationDto.Response response = notificationService.create(request);
        notificationService.sendNotification(response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get list of notifications for user", description = "Regular employees can only view their own notifications")
    public ResponseEntity<List<NotificationDto.Response>> getUserNotifications(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @PathVariable Long userId
    ) {
        Long targetUserId = userId;
        boolean isPrivileged = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_HR") ||
                        a.getAuthority().equals("ROLE_MANAGER"));

        if (!isPrivileged) {
            targetUserId = principal.getId();
        }

        return ResponseEntity.ok(notificationService.getUserNotifications(targetUserId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read", description = "Changes status to READ")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
