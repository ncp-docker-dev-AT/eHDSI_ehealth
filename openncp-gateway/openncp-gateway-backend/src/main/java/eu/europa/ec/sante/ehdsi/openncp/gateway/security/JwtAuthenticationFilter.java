package eu.europa.ec.sante.ehdsi.openncp.gateway.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.startsWith(authorization, "Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = StringUtils.remove(authorization, "Bearer ");
        DecodedJWT jwt = tokenProvider.verifyToken(token);
        Set<GrantedAuthority> authorities = jwt.getClaim("authorities").asList(String.class).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        User user = new User(jwt.getSubject(), "", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, token, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
