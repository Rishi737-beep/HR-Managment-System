package com.nexushr.service;

import com.nexushr.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    NotificationDto.Response create(NotificationDto.Request request);
    void sendNotification(Long id);
    List<NotificationDto.Response> getUserNotifications(Long recipientId);
    void markAsRead(Long id);
}
