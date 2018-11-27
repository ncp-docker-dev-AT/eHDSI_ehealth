package se.sb.epsos.web.auth.jaas;

import org.junit.Test;

import java.security.Principal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JaasGrantedAuthorityPrincipalTest {

    @Test
    public void shouldReturnValidData() {
        Principal principal = () -> "Donald.Duck@apse.se;Donald;Duck;Apoteket;987123567123";
        JaasGrantedAuthorityPrincipal grantedAuthorityPrincipal = new JaasGrantedAuthorityPrincipal(principal);
        assertEquals(grantedAuthorityPrincipal.getId(), "Donald.Duck@apse.se");
        assertEquals(grantedAuthorityPrincipal.getFirstName(), "Donald");
        assertEquals(grantedAuthorityPrincipal.getLastName(), "Duck");
        assertEquals(grantedAuthorityPrincipal.getCommonName(), "Donald Duck");
        assertEquals(grantedAuthorityPrincipal.getOrganizationName(), "Apoteket");
        assertEquals(grantedAuthorityPrincipal.getOrganizationId(), "987123567123");
    }

    @Test
    public void shouldReturnInValidDataWhenTooFewFields() {
        Principal principal = () -> "Donald.Duck@apse.se;Donald;Duck;Apoteket";
        JaasGrantedAuthorityPrincipal grantedAuthorityPrincipal = new JaasGrantedAuthorityPrincipal(principal);
        assertNull(grantedAuthorityPrincipal.getId());
        assertNull(grantedAuthorityPrincipal.getFirstName());
        assertNull(grantedAuthorityPrincipal.getLastName());
        assertNull(grantedAuthorityPrincipal.getCommonName());
        assertNull(grantedAuthorityPrincipal.getOrganizationName());
    }

    @Test
    public void shouldReturnValidDataWhenTooManyFields() {
        Principal principal = () -> "Donald.Duck@apse.se;Donald;Duck;Apoteket;987123567123;extra field";
        JaasGrantedAuthorityPrincipal grantedAuthorityPrincipal = new JaasGrantedAuthorityPrincipal(principal);
        assertEquals(grantedAuthorityPrincipal.getId(), "Donald.Duck@apse.se");
        assertEquals(grantedAuthorityPrincipal.getFirstName(), "Donald");
        assertEquals(grantedAuthorityPrincipal.getLastName(), "Duck");
        assertEquals(grantedAuthorityPrincipal.getCommonName(), "Donald Duck");
        assertEquals(grantedAuthorityPrincipal.getOrganizationName(), "Apoteket");
        assertEquals(grantedAuthorityPrincipal.getOrganizationId(), "987123567123");
    }
}
