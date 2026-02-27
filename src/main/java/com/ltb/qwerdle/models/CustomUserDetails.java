package com.ltb.qwerdle.models;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    // Expose user statistics
    public int getGamesWon() {
        return user.getGamesWon();
    }

    public int getGamesLost() {
        return user.getGamesLost();
    }

    public int getCurrentStreak() {
        return user.getCurrentStreak();
    }

    public int getMaxStreak() {
        return user.getMaxStreak();
    }

    public int getTotalGames() {
        return user.getTotalGames();
    }

    public double getWinRate() {
        return user.getWinRate();
    }
}