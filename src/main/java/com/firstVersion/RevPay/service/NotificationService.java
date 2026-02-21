package com.firstVersion.RevPay.service;

import com.firstVersion.RevPay.entity.Notification;
import com.firstVersion.RevPay.entity.User;
import java.util.List;

public interface NotificationService {
    void sendNotification(User recipient, String message, Notification.NotificationType type);
    List<Notification> getUserNotifications(String email);
    void markAsRead(Long notificationId);
}