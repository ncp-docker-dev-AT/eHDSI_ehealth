package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.MessageWrapper;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping(path = "/api/atna")
public class MessageResource {

    private final MessageService messageService;

    public MessageResource(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping(path = "/messages")
    public ResponseEntity<Page<Message>> getMessages(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "eventDateTime,DESC") String[] sort) {

        Pageable pageable = PageRequest.of(pageNumber, size, getSort(sort));
        Page<Message> page = messageService.findMessages(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping(path = "/search_messages")
    public ResponseEntity<Page<Message>> searchMessages(
            @RequestParam(value = "searchEventId", required = false) String searchEventId,
            @RequestParam(value = "searchEventStartDate", required = false) Instant searchEventStartDate,
            @RequestParam(value = "searchEventEndDate", required = false) Instant searchEventEndDate,
            @RequestParam(value = "activeParticipantId", required = false) String activeParticipantId,
            @RequestParam(value = "activeTypeCode", required = false) String activeTypeCode,
            @RequestParam(value = "messageType", required = false) String messageType,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "eventDateTime,DESC") String[] sort) {

        Pageable pageable = PageRequest.of(pageNumber, size, getSort(sort));
        Page<Message> page = messageService.searchMessages(searchEventId, searchEventStartDate, searchEventEndDate, activeParticipantId, activeTypeCode, messageType, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping(path = "/messages/{id}")
    public ResponseEntity<MessageWrapper> getMessage(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessage(id));
    }

    private Sort getSort(String[] sort) {
        List<Sort.Order> orders = new ArrayList<>();

        if (sort[0].contains(",")) {
            // will sort more than 2 fields
            // sortOrder="field, direction"
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(Sort.Direction.fromString(_sort[1]), _sort[0]));
            }
        } else {
            // sort=[field, direction]
            orders.add(new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]));
        }
        return Sort.by(orders);
    }
}
