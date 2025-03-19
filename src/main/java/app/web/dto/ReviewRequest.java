package app.web.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ReviewRequest {
    private UUID userId;
    private UUID recordId;
    private int rating;
    private String comment;
}
