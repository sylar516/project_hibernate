package entities;

import enums.Rating;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "film", schema = "movie")
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "film_id")
    private Short id;

    @Column(name = "title", length = 128, nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "release_year")
    private Short releaseYear;

    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    //как указать поле original_language_id?

    @Column(name = "rental_duration", columnDefinition = "3", nullable = false)
    private Byte rentalDuration;

    @Column(name = "rental_rate", columnDefinition = "4.99")
    private BigDecimal rentalRate;

    @Column(name = "length")
    private Short length;

    @Column(name = "replacement_cost", columnDefinition = "19.99", nullable = false)
    private BigDecimal replacementCost;

    @Column(name = "rating", columnDefinition = "G")
    private Rating rating;

    //поле special_features

    @UpdateTimestamp
    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;
}
