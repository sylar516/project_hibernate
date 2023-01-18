package com.javarush.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Set;

@Entity
@Table(schema = "movie", name = "film")
public class Film {
    @Id
    @Column(name = "film_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    private String title;

    //возможно ошибка
    @Column(columnDefinition = "LONGVARCHAR")
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    //возможно ошибка
    @Column(name = "release_year",columnDefinition = "year")
    @Type(type = "org.hibernate.type.ShortType")
    private Year releaseYear;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @ManyToOne
    @JoinColumn(name = "original_language_id")
    private Language originalLanguage;

    @Column(name = "rental_duration")
    private Byte rental_duration;

    @Column(name = "rental_rate")
    private BigDecimal rentalRate;

    private Short length;

    @Column(name = "replacement_cost")
    private BigDecimal replacementCost;

    //возможно ошибка
    @Column(columnDefinition = "enum('G', 'PG', 'PG-13', 'R', 'NC-17')")
    @Type(type = "enum('G', 'PG', 'PG-13', 'R', 'NC-17')")
    private String rating;

    //возможно ошибка
    @ElementCollection
    private Set<String> special_features;

    @Column(name = "last_update")
    @UpdateTimestamp
    private LocalDateTime lastUpdate;

    @ManyToMany
    @JoinTable(name="film_actor",
            joinColumns=  @JoinColumn(name="film_id", referencedColumnName="film_id"),
            inverseJoinColumns= @JoinColumn(name="actor_id", referencedColumnName="actor_id"))
    private Set<Actor> actors;

    @ManyToMany
    @JoinTable(name="film_category",
            joinColumns=  @JoinColumn(name="film_id", referencedColumnName="film_id"),
            inverseJoinColumns= @JoinColumn(name="category_id", referencedColumnName="category_id"))
    private Set<Category> categories;

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Year getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Year releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Language getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(Language originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public Byte getRental_duration() {
        return rental_duration;
    }

    public void setRental_duration(Byte rental_duration) {
        this.rental_duration = rental_duration;
    }

    public BigDecimal getRentalRate() {
        return rentalRate;
    }

    public void setRentalRate(BigDecimal rentalRate) {
        this.rentalRate = rentalRate;
    }

    public Short getLength() {
        return length;
    }

    public void setLength(Short length) {
        this.length = length;
    }

    public BigDecimal getReplacementCost() {
        return replacementCost;
    }

    public void setReplacementCost(BigDecimal replacementCost) {
        this.replacementCost = replacementCost;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Set<String> getSpecial_features() {
        return special_features;
    }

    public void setSpecial_features(Set<String> special_features) {
        this.special_features = special_features;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Set<Actor> getActors() {
        return actors;
    }

    public void setActors(Set<Actor> actors) {
        this.actors = actors;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }
}
