package eu.europa.ec.sante.ehdsi.openncp.gateway.web.rest;

import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.Property;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api")
public class PropertyResource {

    private final PropertyService propertyService;

    public PropertyResource(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping(path = "/properties")
    public ResponseEntity<List<Property>> listProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }
}
