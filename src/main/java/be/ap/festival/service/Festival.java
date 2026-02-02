package be.ap.festival.service;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String place;

    private LocalDate date;

    private BigDecimal price;

    @ElementCollection
    @CollectionTable(name = "festival_photos", joinColumns = @JoinColumn(name = "festival_id"))
    @Column(name = "photo_url")
    private List<String> photos = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "festival_artists",
            joinColumns = @JoinColumn(name = "festival_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id"))
    private Set<Artist> lineup = new HashSet<>();

    public Festival() {
    }

    public Festival(String name, String place, LocalDate date, BigDecimal price) {
        this.name = name;
        this.place = place;
        this.date = date;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public Set<Artist> getLineup() {
        return lineup;
    }

    public void setLineup(Set<Artist> lineup) {
        this.lineup = lineup;
    }
}