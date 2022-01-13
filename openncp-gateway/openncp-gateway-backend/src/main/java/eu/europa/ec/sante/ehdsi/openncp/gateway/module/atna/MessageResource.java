package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.MessageWrapper;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/atna")
public class MessageResource {

    private final MessageService messageService;

    public MessageResource(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping(path = "/messages")
    public ResponseEntity<Page<Message>> listMessages(@SortDefault(sort = "eventDateTime", direction = Sort.Direction.DESC)
                                                              Pageable pageable) {
        Page<Message> page = messageService.findMessages(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping(path = "/messages/{id}")
    public ResponseEntity<MessageWrapper> getMessage(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessage(id));
    }
}
