package app.web.dto;

import app.artist.model.Artist;
import app.web.dto.enums.FormatRequest;
import app.web.dto.enums.GenreRequest;
import app.web.dto.enums.TypeRequest;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RecordUpsertRequest {
    private UUID id;
    private String title;
    private List<Artist> artists;
    private String artistsInput;
    private String duration;
    private String description;
    private String recordCover;
    private String spotifyUrl;
    private String totalTracks;
    private int quantity;
    private BigDecimal price;
    private FormatRequest format;
    private TypeRequest type;
    private GenreRequest genre;
    private LocalDate releaseDate;
}
