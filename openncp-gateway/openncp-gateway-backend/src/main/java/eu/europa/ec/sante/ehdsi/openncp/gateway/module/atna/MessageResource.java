package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.MessageWrapper;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/atna")
public class MessageResource {

    private final MessageService messageService;

    public MessageResource(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping(path = "/messages")
    public ResponseEntity<Page<Message>> listMessages(@RequestParam(defaultValue = "0") int pageNumber,
                                                      @RequestParam(defaultValue = "20") int size) {
        Page<Message> page = messageService.findMessages(PageRequest.of(pageNumber, size, Sort.by("eventDateTime").descending()));
        return ResponseEntity.ok(page);
    }

    @GetMapping(path = "/messages/{id}")
    public ResponseEntity<MessageWrapper> getMessage(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessage(id));
    }
}
