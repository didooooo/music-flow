package app.web.dto;

import app.notification.model.MessageType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class SendEmailRequest {
    private String from;
    private String to;
    private String senderUsername;
    private String subject;
    private String body;
    private boolean forAdmin;
    private MessageType messageType;
    private String attachmentUrl;
}
