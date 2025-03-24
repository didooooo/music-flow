package app.web.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserFilterRequest {
    private String searchedName;
    private String searchedStatus;
    private String searchedSort;
}
