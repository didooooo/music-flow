package app.utils;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DateUtils {
    private String label;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
