package com.nexushr.service.impl;

import com.nexushr.dto.NotificationDto;
import com.nexushr.entity.Employee;
import com.nexushr.entity.Notification;
import com.nexushr.entity.NotificationStatus;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.repository.NotificationRepository;
import com.nexushr.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, EmployeeRepository employeeRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public NotificationDto.Response create(NotificationDto.Request request) {
        Employee recipient = employeeRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient employee not found with ID: " + request.getRecipientId()));

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .recipient(recipient)
                .status(NotificationStatus.UNREAD)
                .build();

        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    @Override
    public void sendNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        log.info("Notification sent to recipient ID: {}. Title: {}", notification.getRecipient().getId(), notification.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto.Response> getUserNotifications(Long recipientId) {
        if (!employeeRepository.existsById(recipientId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + recipientId);
        }
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

    private NotificationDto.Response mapToResponse(Notification notification) {
        return NotificationDto.Response.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .recipientId(notification.getRecipient().getId())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
