// NotificationRepository.java
package com.firstVersion.RevPay.repository;

import com.firstVersion.RevPay.entity.Notification;
import com.firstVersion.RevPay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
}