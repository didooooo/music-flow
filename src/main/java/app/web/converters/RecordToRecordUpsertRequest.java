package app.web.converters;

import app.record.model.Record;
import app.web.dto.RecordUpsertRequest;
import app.web.dto.enums.FormatRequest;
import app.web.dto.enums.GenreRequest;
import app.web.dto.enums.TypeRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RecordToRecordUpsertRequest implements Converter<Record, RecordUpsertRequest> {
    @Override
    public RecordUpsertRequest convert(Record source) {
        return RecordUpsertRequest.builder()
                .artists(source.getArtists())
                .title(source.getTitle())
                .price(source.getPrice())
                .description(source.getDescription())
                .quantity(source.getQuantity())
                .releaseDate(source.getReleaseDate())
                .duration(source.getDuration())
                .type(TypeRequest.getByCode(source.getType().name()))
                .genre(GenreRequest.getByCode(source.getGenre().name()))
                .format(FormatRequest.getByCode(source.getFormat().name()))
                .spotifyUrl(source.getSpotifyUrl())
                .totalTracks(source.getTotalTracks())
                .build();
    }
}
