package eu.europa.ec.sante.ehdsi.openncp.gateway.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import eu.europa.ec.sante.ehdsi.openncp.gateway.config.ApplicationProperties;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Component
public class TokenProvider {

    private final ApplicationProperties applicationProperties;

    private String secret;

    public TokenProvider(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @PostConstruct
    public void init() {
        secret = applicationProperties.getSecurity().getJwt().getSecret();
    }

    public String createToken(Authentication authentication) {
        String[] authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);


        return JWT.create()
                .withSubject(authentication.getName())
                .withArrayClaim("authorities", authorities)
                .withExpiresAt(DateUtils.addHours(new Date(), 24))
                .sign(Algorithm.HMAC512(secret));
    }

    public DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secret))
                .build();
        return verifier.verify(token);
    }
}
