package app.notification.model;

import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "notifications")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String senderEmail;
    private String senderUsername;
    @Column(nullable = false)
    private String receiver;
    @Column(nullable = false)
    private String subject;
    @Column(nullable = false)
    private String description;
    private boolean forAdmin;
    private boolean isRead;
    private boolean isDeleted;
    private String attachmentUrl;
    @ManyToOne
    private User user;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

}
