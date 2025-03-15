package app.record.service;

import app.artist.model.Artist;
import app.artist.service.ArtistService;
import app.record.model.Format;
import app.record.model.Genre;
import app.record.model.Record;
import app.record.model.Type;
import app.record.repository.RecordRepository;
import app.web.dto.RecordUpsertRequest;
import jakarta.transaction.Transactional;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecordService {
    private final RecordRepository recordRepository;
    private final ConversionService conversionService;
    private final ArtistService artistService;

    public RecordService(RecordRepository recordRepository, ConversionService conversionService, ArtistService artistService) {
        this.recordRepository = recordRepository;
        this.conversionService = conversionService;
        this.artistService = artistService;
    }

    public List<Record> getAllRecords() {
        return recordRepository.findAll();
    }

    public Page<Record> getEightRecords(PageRequest of) {
        return recordRepository.findAll(of);
    }

    @Transactional
    public void upsertRecord(RecordUpsertRequest input) {
        Optional<Record> optionalRecord = recordRepository.findById(input.getId());
        String[] artists = input.getArtistsInput().split(",");
        List<Artist> artistList = new ArrayList<>();
        for (String artistName : artists) {
            if (artistName != null && !artistName.isBlank()) {
                Artist artist = artistService.save(artistName);
                artistList.add(artist);
            }
        }
        if (optionalRecord.isPresent()) {
            Record record = optionalRecord.get();
            if (!artistList.isEmpty()) {
                for (int i = 0; i <artistList.size(); i++) {
                    int finalI = i;
                    if (record.getArtists().stream().noneMatch(a -> a.getName().equals(artistList.get(finalI).getName()))) {
                        record.getArtists().add(artistList.get(i));
                    }
                }
            }
            if (input.getRecordCover() != null && !input.getRecordCover().isBlank()) {
                record.setImage(input.getRecordCover());
            }
            record.setFormat(Format.getByCode(input.getFormat().name()));
            record.setType(Type.getByCode(input.getType().name()));
            record.setGenre(Genre.getByCode(input.getGenre().name()));
            record.setDescription(input.getDescription());
            record.setDuration(input.getDuration());
            record.setQuantity(input.getQuantity());
            record.setPrice(input.getPrice());
            record.setReleaseDate(input.getReleaseDate());
            record.setSpotifyUrl(input.getSpotifyUrl());
            record.setTotalTracks(input.getTotalTracks());
            recordRepository.save(record);
            return;
        }
        Record record = conversionService.convert(input, Record.class).toBuilder()
                .artists(artistList)
                .build();
        recordRepository.save(record);
    }

    public Record findById(UUID id) {
        return recordRepository.findById(id).orElseThrow(() -> new RuntimeException("Record not found"));
    }

    public Page<Record> getEightRecordsByName(PageRequest of, String name) {
        return recordRepository.findAllByTitleContainingIgnoreCase(name,of);
    }
}
