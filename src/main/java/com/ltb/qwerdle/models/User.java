package com.ltb.qwerdle.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean enabled = true;

    // No real use in MVP, but remains for scalability; Spring Security expects it anyway
    @Column(nullable = false)
    private String role = "ROLE_USER";

    @Column
    private int currentStreak;

    @Column
    private int maxStreak;

    @Column
    private int gamesWon;

    @Column
    private int gamesLost;

    public int getTotalGames() {
        return gamesWon + gamesLost;
    }

    public double getWinRate() {
        int total = getTotalGames();
        return total == 0 ? 0.0 : (double) gamesWon / total * 100;
    }

}
