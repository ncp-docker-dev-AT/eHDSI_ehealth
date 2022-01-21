package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.support;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.MessageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class MessagePredicate {
    private final SearchCriteria criteria;
    private static final Logger logger = LoggerFactory.getLogger((MessagePredicate.class));

    public MessagePredicate(SearchCriteria param) {
        criteria = param;
    }

    public BooleanExpression getPredicate() {
        PathBuilder<MessageEntity> entityPath = new PathBuilder<>(MessageEntity.class, "messageEntity");

        if(criteria.getKey().equals("eventId.code")){
            StringPath path = entityPath.getString(criteria.getKey());
            if (criteria.getOperation().equalsIgnoreCase(":")) {
                return path.containsIgnoreCase(criteria.getValue().toString());
            }
        } else if(criteria.getKey().equals("eventStartDate")){
            DatePath<Instant> path = entityPath.getDate("eventDateTime", Instant.class);
            if (criteria.getOperation().equalsIgnoreCase(":")) {
                return path.after((Instant) criteria.getValue());
            }
        } else if(criteria.getKey().equals("eventEndDate")){
            DatePath<Instant> path = entityPath.getDate("eventDateTime", Instant.class);
            if (criteria.getOperation().equalsIgnoreCase(":")) {
                return path.before((Instant) criteria.getValue());
            }
        }  else {
            logger.error("Search Criteria not handle : " + criteria.getKey());
        }
        return null;
    }


}
