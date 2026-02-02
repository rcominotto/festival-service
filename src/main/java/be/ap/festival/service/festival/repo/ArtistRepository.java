package be.ap.festival.service.festival.repo;

import be.ap.festival.service.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
}