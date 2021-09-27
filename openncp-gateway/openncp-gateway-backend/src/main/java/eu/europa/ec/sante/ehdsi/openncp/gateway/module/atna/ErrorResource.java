package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.Error;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/atna")
public class ErrorResource {

    private final ErrorService errorService;

    public ErrorResource(ErrorService errorService) {
        this.errorService = errorService;
    }

    @GetMapping(path = "/errors")
    public ResponseEntity<List<Error>> listErrors() {
        return ResponseEntity.ok(errorService.findErrors());
    }
}
