package be.ap.festival.service.festival.repo;

import be.ap.festival.service.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
}