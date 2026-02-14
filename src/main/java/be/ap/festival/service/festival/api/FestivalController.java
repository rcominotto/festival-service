package be.ap.festival.service.festival.api;

import be.ap.festival.service.Artist;
import be.ap.festival.service.Festival;
import be.ap.festival.service.data.FestivalDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/festivals")
public class FestivalController {

    private final FestivalDataStore dataStore;

    public FestivalController(FestivalDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @GetMapping
    public List<Festival> findAll() {
        return dataStore.getFestivals();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Festival> findById(@PathVariable Long id) {
        return dataStore.getFestival(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Festival create(@RequestBody Festival festival) {
        festival.setId(null);
        return dataStore.createFestival(festival);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Festival> update(@PathVariable Long id, @RequestBody Festival updated) {
        return dataStore.updateFestival(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dataStore.deleteFestival(id);
    }

    // Photos helpers
    @PostMapping("/{id}/photos")
    public ResponseEntity<Festival> addPhoto(@PathVariable Long id, @RequestBody String photoUrl) {
        return dataStore.addPhoto(id, photoUrl)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/photos")
    public ResponseEntity<Festival> removePhoto(@PathVariable Long id, @RequestParam String url) {
        return dataStore.removePhoto(id, url)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Line-up management
    @GetMapping("/{id}/lineup")
    public ResponseEntity<Set<Artist>> getLineup(@PathVariable Long id) {
        return dataStore.getFestival(id)
                .map(f -> ResponseEntity.ok(f.getLineup()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{festivalId}/lineup/{artistId}")
    public ResponseEntity<Festival> addArtist(@PathVariable Long festivalId, @PathVariable Long artistId) {
        return dataStore.addArtistToFestival(festivalId, artistId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{festivalId}/lineup/{artistId}")
    public ResponseEntity<Festival> removeArtist(@PathVariable Long festivalId, @PathVariable Long artistId) {
        return dataStore.removeArtistFromFestival(festivalId, artistId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}