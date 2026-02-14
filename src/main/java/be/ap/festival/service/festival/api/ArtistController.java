package be.ap.festival.service.festival.api;

import be.ap.festival.service.Artist;
import be.ap.festival.service.data.FestivalDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final FestivalDataStore dataStore;

    public ArtistController(FestivalDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @GetMapping
    public List<Artist> findAll() {
        return dataStore.getArtists();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Artist> findById(@PathVariable Long id) {
        return dataStore.getArtist(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Artist create(@RequestBody Artist artist) {
        artist.setId(null);
        return dataStore.createArtist(artist);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Artist> update(@PathVariable Long id, @RequestBody Artist updated) {
        return dataStore.updateArtist(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dataStore.deleteArtist(id);
    }

    // Extra: show or redirect to artist photo
    @GetMapping("/{id}/photo")
    public ResponseEntity<?> getPhoto(@PathVariable Long id) {
        return dataStore.getArtist(id)
                .map(artist -> {
                    String url = artist.getPhotoUrl();
                    if (url == null || url.isBlank()) {
                        return ResponseEntity.noContent().build();
                    }
                    RedirectView redirect = new RedirectView(url);
                    return new ResponseEntity<>(redirect, HttpStatus.SEE_OTHER);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}