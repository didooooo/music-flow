package app.record.service;

import app.artist.model.Artist;
import app.artist.service.ArtistService;
import app.record.model.Format;
import app.record.model.Genre;
import app.record.model.Record;
import app.record.model.Type;
import app.record.repository.RecordRepository;
import app.review.model.Review;
import app.web.dto.RecordUpsertRequest;
import jakarta.transaction.Transactional;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public Page<Record> getRecordsWithGivenSize(PageRequest of, String sort) {
        if (sort == null) {
            return recordRepository.findAll(of);
        } else if (sort.equals("priceAsc")) {
            return recordRepository.findAll(PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.ASC, "price")));
        } else if (sort.equals("priceDesc")) {
            return recordRepository.findAll(PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.DESC, "price")));
        } else if (sort.equals("releaseDate")) {
            return recordRepository.findAll( PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.ASC, "releaseDate")));
        } else if (sort.equals("nameAsc")) {
            return recordRepository.findAll( PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.ASC, "title")));
        } else if (sort.equals("nameDesc")) {
            return recordRepository.findAll( PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.DESC, "title")));
        }
        return recordRepository.findAll(of);
    }

    @Transactional
    public void upsertRecord(RecordUpsertRequest input) {
        if (input.getId() != null) {
            Optional<Record> optionalRecord = recordRepository.findById(input.getId());
            List<Artist> artistList = addArtists(input);
            if (optionalRecord.isPresent()) {
                Record record = optionalRecord.get();
                if (!artistList.isEmpty()) {
                    for (int i = 0; i < artistList.size(); i++) {
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
        }
        List<Artist> artistList = addArtists(input);
        Record record = conversionService.convert(input, Record.class).toBuilder()
                .artists(artistList)
                .build();
        recordRepository.save(record);
    }

    private List<Artist> addArtists(RecordUpsertRequest input) {
        String[] artists = input.getArtistsInput().split(",");
        List<Artist> artistList = new ArrayList<>();
        for (String artistName : artists) {
            if (artistName != null && !artistName.isBlank()) {
                Artist artist = artistService.save(artistName);
                artistList.add(artist);
            }
        }
        return artistList;
    }

    public Record findById(UUID id) {
        return recordRepository.findById(id).orElseThrow(() -> new RuntimeException("Record not found"));
    }

    public Page<Record> getEightRecordsByName(PageRequest of, String name) {
        return recordRepository.findAllByTitleContainingIgnoreCase(name, of);
    }

    public Page<Record> getEightRecordsWithFilterParameters(PageRequest of, String sort, List<String> format, List<String> genre, List<String> type, BigDecimal maxPrice, BigDecimal minPrice) {
        Specification<Record> spec = Specification.where(null);
        spec = criteriaBuilder(format, genre, type, maxPrice, minPrice, spec);
        if (sort == null) {
            return recordRepository.findAll(spec, of);
        } else if (sort.equals("priceAsc")) {
            return recordRepository.findAll(spec, PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.ASC, "price")));
        } else if (sort.equals("priceDesc")) {
            return recordRepository.findAll(spec, PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.DESC, "price")));
        } else if (sort.equals("releaseDate")) {
            return recordRepository.findAll(spec, PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.ASC, "releaseDate")));
        } else if (sort.equals("nameAsc")) {
            return recordRepository.findAll(spec, PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.ASC, "title")));
        } else if (sort.equals("nameDesc")) {
            return recordRepository.findAll(spec, PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.DESC, "title")));
        }
        return recordRepository.findAll(spec, of);
    }

    private Specification<Record> criteriaBuilder(List<String> format, List<String> genre, List<String> type, BigDecimal maxPrice, BigDecimal minPrice, Specification<Record> spec) {
        if (!genre.isEmpty()) {
            List<Genre> genres = genre.stream().map(Genre::getByCode).collect(Collectors.toList());
            spec = spec.and((root, query, criteriaBuilder) -> {
                criteriaBuilder.in(root.get("genre")).value(genres);
                return criteriaBuilder.conjunction();
            });
        }
        if (!type.isEmpty()) {
            List<Type> types = type.stream().map(Type::getByCode).collect(Collectors.toList());
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("type")).value(types));
        }
        if (!format.isEmpty()) {
            List<Format> formats = format.stream().map(Format::getByCode).collect(Collectors.toList());
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("format")).value(formats));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        return spec;
    }

    public void addReviewToTheRecord(Record record,Review saved) {
        record.getReviews().add(saved);
        recordRepository.save(record);
    }
}
