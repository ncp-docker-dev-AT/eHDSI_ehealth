package eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.persistence.model.Transaction;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/api/eadc")
public class TransactionResource {

    private final TransactionService transactionService;
    private final ExportService exportService;

    public TransactionResource(TransactionService transactionService, ExportService exportService) {
        this.transactionService = transactionService;
        this.exportService = exportService;
    }

    @GetMapping(path = "/transactions")
    public ResponseEntity<List<Transaction>> listTransactions() {
        return ResponseEntity.ok(transactionService.findTransactions());
    }

    @GetMapping(path = "/transactions/{id}")
    public ResponseEntity<Transaction> listTransactions(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @GetMapping(path = "/transactions/exportFromTo")
    public ResponseEntity<ByteArrayResource> getMathResume(@RequestParam("fromDate")
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                           @RequestParam("toDate")
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        ByteArrayResource resource = new ByteArrayResource(exportService.export(fromDate, toDate));
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(resource);
    }
}
