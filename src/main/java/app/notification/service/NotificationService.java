package app.notification.service;

import app.exception.NotificationUpdateFailedException;
import app.notification.client.NotificationFeignClient;
import app.notification.dto.MessageType;
import app.notification.dto.Notification;
import app.user.model.User;
import app.notification.dto.SendEmailRequest;
import app.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {
    private final NotificationFeignClient notificationFeignClient;
    private final String EMAIL = "musicflowsuport@gmail.com";

    public NotificationService(NotificationFeignClient notificationFeignClient) {
        this.notificationFeignClient = notificationFeignClient;
    }

    public void createNotificationFromUserToAdmin(User user, SendEmailRequest sendEmailRequest) {
        sendEmailRequest.setReceiver(EMAIL);
        sendEmailRequest.setForAdmin(true);
        sendEmailRequest.setUserId(user.getId());
        sendEmailRequest.setMessageType(MessageType.SUPPORT);
        ResponseEntity<Void> responseEntity = notificationFeignClient.sendNotification(sendEmailRequest);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification sent successfully");
            return;
        }
        log.info("Notification sent failed");
    }

    public void createNotificationFromAdminToUser(SendEmailRequest emailRequest) {
        emailRequest.setForAdmin(false);
        emailRequest.setSenderEmail(EMAIL);
        ResponseEntity<Void> responseEntity = notificationFeignClient.sendEmail(emailRequest);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification sent successfully");
            return;
        }
        log.info("Notification sent failed");

    }

    public List<Notification> getAll() {
        ResponseEntity<List<Notification>> allNotifications = notificationFeignClient.getAllNotifications();
        if (allNotifications.getStatusCode().is2xxSuccessful()) {
            return allNotifications.getBody();
        }
        return new ArrayList<>();
    }

    public List<Notification> getAllRead(boolean read) {
        ResponseEntity<List<Notification>> allNotifications;
        if (read) {
            allNotifications = notificationFeignClient.getReadNotifications();
        } else {
            allNotifications = notificationFeignClient.getUnreadNotifications();
        }
        if (allNotifications.getStatusCode().is2xxSuccessful()) {
            return allNotifications.getBody();
        }
        return new ArrayList<>();
    }

    public void deleteNotificationById(UUID id) {
        ResponseEntity<Void> responseEntity = notificationFeignClient.deleteNotification(id);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification deleted successfully");
            return;
        }
        throw new NotificationUpdateFailedException("Notification delete failed");
    }

    public void updateNotification(UUID id) {
        ResponseEntity<Void> responseEntity = notificationFeignClient.updateNotification(id);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification updated successfully");
            return;
        }
        throw new NotificationUpdateFailedException("Notification update failed");

    }
}
