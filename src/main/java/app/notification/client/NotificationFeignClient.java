package app.notification.client;

import app.notification.dto.Notification;
import app.notification.dto.SendEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notification-svc", url = "http://localhost:8081/notifications/")
public interface NotificationFeignClient {
    @PostMapping
    ResponseEntity<Void> sendNotification(@RequestBody SendEmailRequest request);

    @PostMapping("email")
    ResponseEntity<Void> sendEmail(@RequestBody SendEmailRequest request);

    @GetMapping
    ResponseEntity<List<Notification>> getAllNotifications();

    @GetMapping("/read")
    ResponseEntity<List<Notification>> getReadNotifications();

    @GetMapping("/unread")
    ResponseEntity<List<Notification>> getUnreadNotifications();

    @GetMapping("/emails")
    ResponseEntity<List<Notification>> getSendEmails();

    @PutMapping("/{id}")
    ResponseEntity<Void> updateNotification(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteNotification(@PathVariable UUID id);
}
