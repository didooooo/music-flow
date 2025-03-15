package app.web.converters;

import app.record.model.Format;
import app.record.model.Genre;
import app.record.model.Type;
import app.web.dto.RecordUpsertRequest;
import org.springframework.core.convert.converter.Converter;
import app.record.model.Record;
import org.springframework.stereotype.Component;

@Component
public class RecordUpsertRequestToRecord implements Converter<RecordUpsertRequest, Record> {
    @Override
    public Record convert(RecordUpsertRequest source) {
        return Record.builder()
                .image(source.getRecordCover())
                .title(source.getTitle())
                .description(source.getDescription())
                .price(source.getPrice())
                .duration(source.getDuration())
                .spotifyUrl(source.getSpotifyUrl())
                .totalTracks(source.getTotalTracks())
                .quantity(source.getQuantity())
                .genre(Genre.getByCode(source.getGenre().name()))
                .type(Type.getByCode(source.getType().name()))
                .format(Format.getByCode(source.getFormat().name()))
                .releaseDate(source.getReleaseDate())
                .build();
    }
}
