package se.sb.epsos.web.service;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AssertionHandlerConfigManagerTest {

    @Test
    public void testGetPermissionsPrefix() {
    }

    @Test
    public void testGetFacilityType_ROLE_PHARMACIST() {
        String role = AssertionHandlerConfigManager.getFacilityType("ROLE_PHARMACIST");
        assertNotNull(role);
        assertEquals("Pharmacy", role);
    }

    @Test
    public void testGetFacilityType_ROLE_DOCTOR() {
        String role = AssertionHandlerConfigManager.getFacilityType("ROLE_DOCTOR");
        assertNotNull(role);
        assertEquals("Hospital", role);
    }

    @Test
    public void testGetFacilityType_ROLE_NURSE() {
        String role = AssertionHandlerConfigManager.getFacilityType("ROLE_NURSE");
        assertNotNull(role);
        assertEquals("Hospital", role);
    }

    @Test
    public void testGetPurposeOfUse() {
        String s = AssertionHandlerConfigManager.getPurposeOfUse();
        assertEquals("TREATMENT", s);
    }

    @Test
    public void testGetXspaLocality() {

    }

    @Test
    public void testGetRole_ROLE_PHARMACIST() {
        String role = AssertionHandlerConfigManager.getRole("ROLE_PHARMACIST");
        assertNotNull(role);
        assertEquals("Licensed Health Care Providers", role);
    }

    @Test
    public void testGetRole_ROLE_DOCTOR() {
        String role = AssertionHandlerConfigManager.getRole("ROLE_DOCTOR");
        assertNotNull(role);
        assertEquals("Licensed Health Care Providers", role);
    }

    @Test
    public void testGetRole_ROLE_NURSE() {
        String role = AssertionHandlerConfigManager.getRole("ROLE_NURSE");
        assertNotNull(role);
        assertEquals("Licensed Health Care Providers", role);
    }

    @Test
    public void testGetFunctionalRole_ROLE_PHARMACIST() {
        String functionalRole = AssertionHandlerConfigManager.getFunctionalRole("ROLE_PHARMACIST");
        assertNotNull(functionalRole);
        assertEquals("Pharmacists", functionalRole);
    }

    @Test
    public void testGetFunctionalRole_ROLE_DOCTOR() {
        String functionalRole = AssertionHandlerConfigManager.getFunctionalRole("ROLE_DOCTOR");
        assertNotNull(functionalRole);
        assertEquals("Medical Doctors", functionalRole);
    }

    @Test
    public void testGetFunctionalRole_ROLE_NURSE() {
        String functionalRole = AssertionHandlerConfigManager.getFunctionalRole("ROLE_NURSE");
        assertNotNull(functionalRole);
        assertEquals("Nursing professionals", functionalRole);
    }

    @Test
    public void testGetPermissions() {
        Set<String> roles = AssertionHandlerConfigManager.getPermissions("ROLE_PHARMACIST");
        assertNotNull(roles);
        assertEquals(4, roles.size());
    }
}
