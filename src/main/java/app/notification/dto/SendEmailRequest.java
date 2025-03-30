package app.notification.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class SendEmailRequest {
    private String senderEmail;
    private String receiver;
    private String senderUsername;
    private String subject;
    private String description;
    private boolean forAdmin;
    private UUID userId;
    private MessageType messageType;
}
