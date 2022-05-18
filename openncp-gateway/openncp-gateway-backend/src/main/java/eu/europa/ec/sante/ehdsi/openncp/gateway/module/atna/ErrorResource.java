package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old.Message;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.Error;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<Page<Error>> getErrors(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<Error> page = errorService.findErrors(pageable);
        return ResponseEntity.ok(page);
    }

}
