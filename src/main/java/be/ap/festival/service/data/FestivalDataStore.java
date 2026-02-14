package be.ap.festival.service.data;

import be.ap.festival.service.Artist;
import be.ap.festival.service.Festival;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class FestivalDataStore {

    private final Map<Long, Festival> festivals = new LinkedHashMap<>();
    private final Map<Long, Artist> artists = new LinkedHashMap<>();

    private final AtomicLong festivalIdSeq = new AtomicLong(1);
    private final AtomicLong artistIdSeq = new AtomicLong(1);

    @EventListener(ApplicationReadyEvent.class)
    public void load() throws IOException {
        // Load JSON from classpath
        ClassPathResource resource = new ClassPathResource("festivals.json");
        if (!resource.exists()) {
            return; // nothing to load
        }
        try (InputStream is = resource.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .findAndRegisterModules();

            RootDto root = mapper.readValue(is, RootDto.class);
            if (root == null || root.festivals == null) return;

            // First, index all artists found across festivals so shared artists map to same instance
            Map<Long, Artist> tempArtists = new LinkedHashMap<>();
            for (FestivalDto fDto : root.festivals) {
                if (fDto.lineup != null) {
                    for (Artist a : fDto.lineup) {
                        Long id = a.getId();
                        if (id == null) {
                            id = artistIdSeq.getAndIncrement();
                            a.setId(id);
                        }
                        tempArtists.putIfAbsent(id, a);
                    }
                }
            }
            // Put into main artists map and update sequence
            artists.clear();
            artists.putAll(tempArtists);
            if (!artists.isEmpty()) {
                long maxId = artists.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
                artistIdSeq.set(maxId + 1);
            }

            // Now build festivals, ensuring their lineup references the shared artist instances
            festivals.clear();
            for (FestivalDto fDto : root.festivals) {
                Festival f = new Festival();
                Long fid = fDto.id != null ? fDto.id : festivalIdSeq.getAndIncrement();
                f.setId(fid);
                f.setName(fDto.name);
                f.setPlace(fDto.place);
                f.setDate(fDto.date);
                f.setPrice(fDto.price);
                f.setPhotos(fDto.photos != null ? new ArrayList<>(fDto.photos) : new ArrayList<>());
                Set<Artist> lineup = new LinkedHashSet<>();
                if (fDto.lineup != null) {
                    for (Artist a : fDto.lineup) {
                        if (a.getId() != null && artists.containsKey(a.getId())) {
                            lineup.add(artists.get(a.getId()));
                        } else {
                            // If no ID, try to match by name
                            Optional<Artist> byName = artists.values().stream()
                                    .filter(x -> Objects.equals(x.getName(), a.getName()))
                                    .findFirst();
                            Artist ref = byName.orElseGet(() -> {
                                long newId = artistIdSeq.getAndIncrement();
                                Artist na = new Artist(a.getName(), a.getPhotoUrl(), a.getGenre());
                                na.setId(newId);
                                artists.put(newId, na);
                                return na;
                            });
                            lineup.add(ref);
                        }
                    }
                }
                f.setLineup(lineup);
                festivals.put(fid, f);
            }
            if (!festivals.isEmpty()) {
                long maxFid = festivals.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
                festivalIdSeq.set(maxFid + 1);
            }
        }
    }

    // ===== Public API for controllers =====
    public List<Festival> getFestivals() { return new ArrayList<>(festivals.values()); }

    public Optional<Festival> getFestival(Long id) { return Optional.ofNullable(festivals.get(id)); }

    public Festival createFestival(Festival f) {
        long id = festivalIdSeq.getAndIncrement();
        f.setId(id);
        // Normalize nulls
        if (f.getPhotos() == null) f.setPhotos(new ArrayList<>());
        if (f.getLineup() == null) f.setLineup(new LinkedHashSet<>());
        festivals.put(id, f);
        return f;
    }

    public Optional<Festival> updateFestival(Long id, Festival updated) {
        Festival existing = festivals.get(id);
        if (existing == null) return Optional.empty();
        existing.setName(updated.getName());
        existing.setPlace(updated.getPlace());
        existing.setDate(updated.getDate());
        existing.setPrice(updated.getPrice());
        existing.setPhotos(updated.getPhotos() != null ? new ArrayList<>(updated.getPhotos()) : new ArrayList<>());
        // Keep lineup unchanged here; dedicated endpoints may modify it
        return Optional.of(existing);
    }

    public void deleteFestival(Long id) { festivals.remove(id); }

    public Set<Artist> getLineup(Long festivalId) {
        return getFestival(festivalId).map(Festival::getLineup).orElseGet(LinkedHashSet::new);
    }

    public Optional<Festival> addPhoto(Long festivalId, String url) {
        Festival f = festivals.get(festivalId);
        if (f == null) return Optional.empty();
        List<String> photos = new ArrayList<>(Optional.ofNullable(f.getPhotos()).orElseGet(ArrayList::new));
        photos.add(url);
        f.setPhotos(photos);
        return Optional.of(f);
    }

    public Optional<Festival> removePhoto(Long festivalId, String url) {
        Festival f = festivals.get(festivalId);
        if (f == null) return Optional.empty();
        if (f.getPhotos() != null) {
            f.getPhotos().removeIf(p -> Objects.equals(p, url));
        }
        return Optional.of(f);
    }

    public List<Artist> getArtists() { return new ArrayList<>(artists.values()); }

    public Optional<Artist> getArtist(Long id) { return Optional.ofNullable(artists.get(id)); }

    public Artist createArtist(Artist a) {
        long id = artistIdSeq.getAndIncrement();
        a.setId(id);
        artists.put(id, a);
        return a;
    }

    public Optional<Artist> updateArtist(Long id, Artist updated) {
        Artist existing = artists.get(id);
        if (existing == null) return Optional.empty();
        existing.setName(updated.getName());
        existing.setGenre(updated.getGenre());
        existing.setPhotoUrl(updated.getPhotoUrl());
        return Optional.of(existing);
    }

    public void deleteArtist(Long id) {
        // Remove from festivals' lineups first
        for (Festival f : festivals.values()) {
            if (f.getLineup() != null) {
                f.getLineup().removeIf(a -> Objects.equals(a.getId(), id));
            }
        }
        artists.remove(id);
    }

    public Optional<Festival> addArtistToFestival(Long festivalId, Long artistId) {
        Festival f = festivals.get(festivalId);
        Artist a = artists.get(artistId);
        if (f == null || a == null) return Optional.empty();
        if (f.getLineup() == null) f.setLineup(new LinkedHashSet<>());
        f.getLineup().add(a);
        return Optional.of(f);
    }

    public Optional<Festival> removeArtistFromFestival(Long festivalId, Long artistId) {
        Festival f = festivals.get(festivalId);
        if (f == null) return Optional.empty();
        if (f.getLineup() != null) {
            f.getLineup().removeIf(a -> Objects.equals(a.getId(), artistId));
        }
        return Optional.of(f);
    }

    // ===== DTOs for JSON binding =====
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RootDto {
        public List<FestivalDto> festivals;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FestivalDto {
        public Long id;
        public String name;
        public String place;
        public LocalDate date;
        public BigDecimal price;
        public List<String> photos;
        public List<Artist> lineup;
    }
}
