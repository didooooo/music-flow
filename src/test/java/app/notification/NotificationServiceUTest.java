package app.notification;

import app.exception.NotificationUpdateFailedException;
import app.notification.client.NotificationFeignClient;
import app.notification.dto.MessageType;
import app.notification.dto.Notification;
import app.notification.dto.SendEmailRequest;
import app.notification.service.NotificationService;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {
    @Mock
    private NotificationFeignClient notificationFeignClient;
    private final String EMAIL = "musicflowsuport@gmail.com";
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void givenUserAndSendEmailRequest_whenCreateNotificationFromUserToAdmin_thenNotificationSentSuccessfully() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();

        when(notificationFeignClient.sendNotification(any(SendEmailRequest.class))).thenReturn(responseEntity);

        // When
        notificationService.createNotificationFromUserToAdmin(user, sendEmailRequest);

        // Then
        assertEquals(EMAIL, sendEmailRequest.getReceiver());
        assertTrue(sendEmailRequest.isForAdmin());
        assertEquals(user.getId(), sendEmailRequest.getUserId());
        assertEquals(MessageType.SUPPORT, sendEmailRequest.getMessageType());
        verify(notificationFeignClient, times(1)).sendNotification(sendEmailRequest);
    }

    @Test
    void givenUserAndSendEmailRequest_whenCreateNotificationFails_thenLogFailure() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        ResponseEntity<Void> responseEntity = ResponseEntity.badRequest().build();

        when(notificationFeignClient.sendNotification(any(SendEmailRequest.class))).thenReturn(responseEntity);

        // When
        notificationService.createNotificationFromUserToAdmin(user, sendEmailRequest);

        // Then
        assertEquals(EMAIL, sendEmailRequest.getReceiver());
        assertTrue(sendEmailRequest.isForAdmin());
        assertEquals(user.getId(), sendEmailRequest.getUserId());
        assertEquals(MessageType.SUPPORT, sendEmailRequest.getMessageType());
        verify(notificationFeignClient, times(1)).sendNotification(sendEmailRequest);
    }

    @Test
    void givenEmailRequest_whenCreateNotificationFromAdminToUser_thenSendEmail() {
        // Given
        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        when(notificationFeignClient.sendEmail(sendEmailRequest)).thenReturn(ResponseEntity.ok().build());

        // When
        notificationService.createNotificationFromAdminToUser(sendEmailRequest);

        // Then
        assertFalse(sendEmailRequest.isForAdmin());
        assertEquals(EMAIL, sendEmailRequest.getSenderEmail());
        verify(notificationFeignClient, times(1)).sendEmail(sendEmailRequest);
    }

    @Test
    void givenUserAndEmailRequest_whenCreateNotificationFromUserToAdminFails_thenLogFailure() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        ResponseEntity<Void> responseEntity = ResponseEntity.badRequest().build();
        when(notificationFeignClient.sendEmail(sendEmailRequest)).thenReturn(responseEntity);

        // When
        notificationService.createNotificationFromAdminToUser(sendEmailRequest);

        // Then
        verify(notificationFeignClient, times(1)).sendEmail(sendEmailRequest);
    }

    @Test
    void givenSuccessfulResponse_whenGetAllNotifications_thenReturnListOfNotifications() {
        // Given
        List<Notification> notifications = new ArrayList<>();
        notifications.add(Notification.builder().description("Test").build());
        ResponseEntity<List<Notification>> responseEntity = new ResponseEntity<>(notifications, HttpStatus.OK);
        when(notificationFeignClient.getAllNotifications()).thenReturn(responseEntity);

        // When
        List<Notification> result = notificationService.getAll();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getDescription());
    }

    @Test
    void givenUnsuccessfulResponse_whenGetAllNotifications_thenReturnEmptyList() {
        // Given
        ResponseEntity<List<Notification>> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(notificationFeignClient.getAllNotifications()).thenReturn(responseEntity);

        // When
        List<Notification> result = notificationService.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenNotFoundResponse_whenGetAllNotifications_thenReturnEmptyList() {
        // Given
        ResponseEntity<List<Notification>> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        when(notificationFeignClient.getAllNotifications()).thenReturn(responseEntity);

        // When
        List<Notification> result = notificationService.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenBadRequestResponse_whenGetAllNotifications_thenReturnEmptyList() {
        // Given
        ResponseEntity<List<Notification>> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(notificationFeignClient.getAllNotifications()).thenReturn(responseEntity);

        // When
        List<Notification> result = notificationService.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    @Test
    void givenReadTrueAndSuccessfulResponse_whenGetAllRead_thenReturnListOfNotifications() {
        // Given
        List<Notification> notifications = new ArrayList<>();
        notifications.add(Notification.builder().description("Read notification").build());
        ResponseEntity<List<Notification>> responseEntity = new ResponseEntity<>(notifications, HttpStatus.OK);
        when(notificationFeignClient.getReadNotifications()).thenReturn(responseEntity);

        // When
        List<Notification> result = notificationService.getAllRead(true);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Read notification", result.get(0).getDescription());
    }

    @Test
    void givenReadTrueAndUnsuccessfulResponse_whenGetAllRead_thenReturnEmptyList() {
        // Given
        ResponseEntity<List<Notification>> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(notificationFeignClient.getReadNotifications()).thenReturn(responseEntity);

        // When
        List<Notification> result = notificationService.getAllRead(true);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenReadFalseAndSuccessfulResponse_whenGetAllRead_thenReturnListOfNotifications() {
        // Given
        List<Notification> notifications = new ArrayList<>();
        notifications.add(Notification.builder().description("Unread notification").build()); // Using builder here
        ResponseEntity<List<Notification>> responseEntity = new ResponseEntity<>(notifications, HttpStatus.OK);
        when(notificationFeignClient.getUnreadNotifications()).thenReturn(responseEntity);

        // When
        List<Notification> result = notificationService.getAllRead(false);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Unread notification", result.get(0).getDescription());
    }

    @Test
    void givenReadFalseAndUnsuccessfulResponse_whenGetAllRead_thenReturnEmptyList() {
        // Given
        ResponseEntity<List<Notification>> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(notificationFeignClient.getUnreadNotifications()).thenReturn(responseEntity);

        // When
        List<Notification> result = notificationService.getAllRead(false);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    @Test
    void givenValidId_whenDeleteNotificationById_thenNotificationDeletedSuccessfully() {
        // Given
        UUID notificationId = UUID.randomUUID();
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(notificationFeignClient.deleteNotification(notificationId)).thenReturn(responseEntity);

        // When & Then
        assertDoesNotThrow(() -> notificationService.deleteNotificationById(notificationId));
        verify(notificationFeignClient, times(1)).deleteNotification(notificationId);
    }

    @Test
    void givenInvalidId_whenDeleteNotificationById_thenThrowNotificationUpdateFailedException() {
        // Given
        UUID notificationId = UUID.randomUUID();
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(notificationFeignClient.deleteNotification(notificationId)).thenReturn(responseEntity);

        // When & Then
        assertThrows(NotificationUpdateFailedException.class, () -> notificationService.deleteNotificationById(notificationId));
        verify(notificationFeignClient, times(1)).deleteNotification(notificationId);
    }
    @Test
    void givenValidId_whenUpdateNotification_thenNotificationUpdatedSuccessfully() {
        // Given
        UUID notificationId = UUID.randomUUID();
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(notificationFeignClient.updateNotification(notificationId)).thenReturn(responseEntity);

        // When & Then
        assertDoesNotThrow(() -> notificationService.updateNotification(notificationId));
        verify(notificationFeignClient, times(1)).updateNotification(notificationId);
    }

    @Test
    void givenInvalidId_whenUpdateNotification_thenThrowNotificationUpdateFailedException() {
        // Given
        UUID notificationId = UUID.randomUUID();
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(notificationFeignClient.updateNotification(notificationId)).thenReturn(responseEntity);

        // When & Then
        assertThrows(NotificationUpdateFailedException.class, () -> notificationService.updateNotification(notificationId));
        verify(notificationFeignClient, times(1)).updateNotification(notificationId);
    }
}
