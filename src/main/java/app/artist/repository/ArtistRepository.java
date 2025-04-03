package app.artist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.artist.model.Artist;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {
    Optional<Artist> findByName(String name);
}
