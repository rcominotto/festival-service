package be.ap.festival.service.festival.api;

import be.ap.festival.service.Artist;
import be.ap.festival.service.Festival;
import be.ap.festival.service.festival.repo.ArtistRepository;
import be.ap.festival.service.festival.repo.FestivalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/festivals")
public class FestivalController {

    private final FestivalRepository festivalRepository;
    private final ArtistRepository artistRepository;

    public FestivalController(FestivalRepository festivalRepository, ArtistRepository artistRepository) {
        this.festivalRepository = festivalRepository;
        this.artistRepository = artistRepository;
    }

    @GetMapping
    public List<Festival> findAll() {
        return festivalRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Festival> findById(@PathVariable Long id) {
        return festivalRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Festival create(@RequestBody Festival festival) {
        festival.setId(null);
        return festivalRepository.save(festival);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Festival> update(@PathVariable Long id, @RequestBody Festival updated) {
        return festivalRepository.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setPlace(updated.getPlace());
                    existing.setDate(updated.getDate());
                    existing.setPrice(updated.getPrice());
                    existing.setPhotos(updated.getPhotos());
                    return ResponseEntity.ok(festivalRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        festivalRepository.deleteById(id);
    }

    // Photos helpers
    @PostMapping("/{id}/photos")
    public ResponseEntity<Festival> addPhoto(@PathVariable Long id, @RequestBody String photoUrl) {
        return festivalRepository.findById(id)
                .map(f -> {
                    List<String> photos = new ArrayList<>(Optional.ofNullable(f.getPhotos()).orElseGet(ArrayList::new));
                    photos.add(photoUrl);
                    f.setPhotos(photos);
                    return ResponseEntity.ok(festivalRepository.save(f));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/photos")
    public ResponseEntity<Festival> removePhoto(@PathVariable Long id, @RequestParam String url) {
        return festivalRepository.findById(id)
                .map(f -> {
                    f.getPhotos().removeIf(p -> Objects.equals(p, url));
                    return ResponseEntity.ok(festivalRepository.save(f));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Line-up management
    @GetMapping("/{id}/lineup")
    public ResponseEntity<Set<Artist>> getLineup(@PathVariable Long id) {
        return festivalRepository.findById(id)
                .map(f -> ResponseEntity.ok(f.getLineup()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{festivalId}/lineup/{artistId}")
    public ResponseEntity<Festival> addArtist(@PathVariable Long festivalId, @PathVariable Long artistId) {
        Optional<Festival> fOpt = festivalRepository.findById(festivalId);
        Optional<Artist> aOpt = artistRepository.findById(artistId);
        if (fOpt.isEmpty() || aOpt.isEmpty()) return ResponseEntity.notFound().build();
        Festival f = fOpt.get();
        f.getLineup().add(aOpt.get());
        return ResponseEntity.ok(festivalRepository.save(f));
    }

    @DeleteMapping("/{festivalId}/lineup/{artistId}")
    public ResponseEntity<Festival> removeArtist(@PathVariable Long festivalId, @PathVariable Long artistId) {
        Optional<Festival> fOpt = festivalRepository.findById(festivalId);
        Optional<Artist> aOpt = artistRepository.findById(artistId);
        if (fOpt.isEmpty() || aOpt.isEmpty()) return ResponseEntity.notFound().build();
        Festival f = fOpt.get();
        f.getLineup().remove(aOpt.get());
        return ResponseEntity.ok(festivalRepository.save(f));
    }
}