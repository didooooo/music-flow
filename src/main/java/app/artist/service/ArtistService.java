package app.artist.service;

import app.artist.model.Artist;
import app.artist.repository.ArtistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArtistService {
    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public List<Artist> findAll() {
        return artistRepository.findAll();
    }

    public Artist save(String artistName) {
        Optional<Artist> byName = artistRepository.findByName(artistName);
        if(byName.isPresent()) {
            return byName.get();
        }
        Artist artist = Artist.builder()
                .name(artistName)
                .build();
        return artistRepository.save(artist);
    }
}
