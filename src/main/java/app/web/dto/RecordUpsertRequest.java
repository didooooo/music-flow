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
    @NotBlank
    private String title;
    private List<Artist> artists;
    private String artistsInput;
    @NotBlank
    private String duration;
    @NotBlank
    private String description;
    @NotBlank
    private String recordCover;
    @NotBlank
    private String spotifyUrl;
    @NotBlank
    private String totalTracks;
    @NotBlank
    private int quantity;
    @NotBlank
    private BigDecimal price;
    @NotNull
    private FormatRequest format;
    @NotNull
    private TypeRequest type;
    @NotNull
    private GenreRequest genre;
    @NotNull
    private LocalDate releaseDate;
}
