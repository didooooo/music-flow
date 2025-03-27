package app.notification.service;

import app.notification.model.MessageType;
import app.notification.model.Notification;
import app.notification.repository.NotificationRepository;
import app.user.model.User;
import app.web.dto.SendEmailRequest;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotificationFromUserToAdmin(User user, SendEmailRequest sendEmailRequest) {
        Notification notification = Notification.builder()
                .forAdmin(true)
                .user(user)
                .isDeleted(false)
                .description(sendEmailRequest.getBody())
                .messageType(MessageType.SUPPORT)
                .isRead(false)
                .senderEmail(sendEmailRequest.getFrom())
                .receiver("our-email")
                .subject(sendEmailRequest.getSubject())
                .senderUsername(sendEmailRequest.getSenderUsername())
                .build();
        return notificationRepository.save(notification);
    }
}
