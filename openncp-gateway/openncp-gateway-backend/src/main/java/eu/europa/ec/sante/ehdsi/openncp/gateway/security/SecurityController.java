package eu.europa.ec.sante.ehdsi.openncp.gateway.security;

import eu.europa.ec.sante.ehdsi.openncp.gateway.domain.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class SecurityController {

    private final AuthenticationManager authenticationManager;

    private final TokenProvider tokenProvider;

    public SecurityController(AuthenticationManager authenticationManager, TokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping(path = "/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        String token = tokenProvider.createToken(authentication);
        AuthenticationResponse response = new AuthenticationResponse(token);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(response);
    }

    @GetMapping(path = "/account")
    public ResponseEntity<User> getAuthenticatedUser() {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = new User();
        user.setUsername(currentUser.getUsername());
        user.setRoles(currentUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
        return ResponseEntity.ok(user);
    }
}
