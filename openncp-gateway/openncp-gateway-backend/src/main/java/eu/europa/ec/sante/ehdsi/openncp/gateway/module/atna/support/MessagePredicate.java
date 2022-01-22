package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.support;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.MessageEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class MessagePredicate {

    private final Logger logger = LoggerFactory.getLogger((MessagePredicate.class));
    private final SearchCriteria criteria;

    public MessagePredicate(SearchCriteria param) {
        criteria = param;
    }

    public BooleanExpression getPredicate() {

        PathBuilder<MessageEntity> entityPath = new PathBuilder<>(MessageEntity.class, "messageEntity");

        if (StringUtils.equals(criteria.getKey(), ("eventId.code"))) {
            StringPath path = entityPath.getString(criteria.getKey());
            if (StringUtils.equals(criteria.getOperation(), ":")) {
                return path.containsIgnoreCase(criteria.getValue().toString());
            }
        } else if (StringUtils.equals(criteria.getKey(), "eventStartDate")) {
            DatePath<Instant> path = entityPath.getDate("eventDateTime", Instant.class);
            if (StringUtils.equals(criteria.getOperation(), ":")) {
                return path.after((Instant) criteria.getValue());
            }
        } else if (criteria.getKey().equals("eventEndDate")) {
            DatePath<Instant> path = entityPath.getDate("eventDateTime", Instant.class);
            if (StringUtils.equals(criteria.getOperation(), ":")) {
                return path.before((Instant) criteria.getValue());
            }
        } else {
            logger.error("Search Criteria not handled: '{}'", criteria.getKey());
        }
        return null;
    }
}
