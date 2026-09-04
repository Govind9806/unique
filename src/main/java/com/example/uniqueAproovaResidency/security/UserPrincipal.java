package com.example.uniqueAproovaResidency.security;

import com.example.uniqueAproovaResidency.module.role.Role;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private String id;
    private String email;
    private String password;
    private String name;
    private String flatId;
    private String flatNumber;
    private Role role;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        String flatId = user.getFlat() != null ? user.getFlat().getId() : null;
        String flatNumber = user.getFlat() != null ? user.getFlat().getFlatNumber() : null;

        List<GrantedAuthority> authorities = new ArrayList<>();
        Role r = user.getRole();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + r.name()));

        if (r == Role.MAINTENANCE_FLAT || r == Role.MAINTENANCE_MEMBER || r == Role.TREASURER) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MAINTENANCE_FLAT"));
            authorities.add(new SimpleGrantedAuthority("ROLE_MAINTENANCE_MEMBER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_TREASURER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_FLAT_MEMBER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_RESIDENT"));
        } else if (r == Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_MAINTENANCE_FLAT"));
            authorities.add(new SimpleGrantedAuthority("ROLE_MAINTENANCE_MEMBER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_TREASURER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_FLAT_MEMBER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_RESIDENT"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_FLAT_MEMBER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_RESIDENT"));
        }

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getName(),
                flatId,
                flatNumber,
                user.getRole(),
                authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
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
        return true;
    }
}
