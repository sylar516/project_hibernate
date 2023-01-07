package entities;

import jakarta.persistence.*;

@Entity
@Table(name = "language", schema = "movie")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Byte id;
}
