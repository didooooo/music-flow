package app.notification.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    private UUID id;
    private String senderEmail;
    private String senderUsername;
    private String receiver;
    private String subject;
    private String description;
    private boolean forAdmin;
    private boolean readNotification;
//    private boolean isDeleted;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MessageType messageType;

}
