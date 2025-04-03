package app.artist;

import app.artist.model.Artist;
import app.artist.repository.ArtistRepository;
import app.artist.service.ArtistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArtistServiceUTest {
    @Mock
    private ArtistRepository artistRepository;
    @InjectMocks
    private ArtistService artistService;

    @Test
    void givenExistingArtist_whenSave_thenReturnExistingArtist() {
        // Given
        String artistName = "Tate McRae";
        Artist existingArtist = Artist.builder().name(artistName).build();
        when(artistRepository.findByName(artistName)).thenReturn(Optional.of(existingArtist));

        // When
        Artist result = artistService.save(artistName);

        // Then
        assertEquals(existingArtist, result);
        verify(artistRepository, never()).save(any());
    }

    @Test
    void givenNewArtist_whenSave_thenSaveAndReturnNewArtist() {
        // Given
        String artistName = "Pink Floyd";
        Artist newArtist = Artist.builder().name(artistName).build();
        when(artistRepository.findByName(artistName)).thenReturn(Optional.empty());
        when(artistRepository.save(any())).thenReturn(newArtist);

        // When
        Artist result = artistService.save(artistName);

        // Then
        assertEquals(newArtist, result);
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    void whenFindAll_thenFindAll() {
        //Given
        String artistName = "Pink Floyd";
        Artist newArtist = Artist.builder().name(artistName).build();
        //When
        when(artistRepository.findAll()).thenReturn(List.of(newArtist));

        List<Artist> result = artistService.findAll();

        //Then
        assertEquals(newArtist, result.get(0));
        verify(artistRepository, times(1)).findAll();
    }

    @Test
    void whenFindAll_thenEmptyList() {
        //Given

        //When
        when(artistRepository.findAll()).thenReturn(new ArrayList<>());

        List<Artist> result = artistService.findAll();

        //Then
        assertEquals(0, result.size());
        verify(artistRepository, times(1)).findAll();
    }
}
