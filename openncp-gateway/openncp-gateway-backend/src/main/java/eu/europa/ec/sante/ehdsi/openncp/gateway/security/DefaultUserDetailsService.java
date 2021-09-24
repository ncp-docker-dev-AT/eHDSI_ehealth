package eu.europa.ec.sante.ehdsi.openncp.gateway.security;

import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;
import java.util.stream.Collectors;

public class DefaultUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DefaultUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findWithRolesByUsername(username)
                .map(user -> {
                    Set<GrantedAuthority> authorities = user.getRoles().stream()
                            .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                            .collect(Collectors.toSet());
                    return new User(user.getUsername(), user.getPassword(), true, true, true, true, authorities);
                })
                .orElseThrow(() -> new UsernameNotFoundException("Username '" + username + "' not found"));
    }
}
