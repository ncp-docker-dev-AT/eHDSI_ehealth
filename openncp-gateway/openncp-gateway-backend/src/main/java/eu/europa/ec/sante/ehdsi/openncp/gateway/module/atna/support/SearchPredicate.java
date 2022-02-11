package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.support;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class SearchPredicate {

    private final Logger logger = LoggerFactory.getLogger((SearchPredicate.class));
    private final SearchCriteria criteria;

    public SearchPredicate(SearchCriteria param) {
        criteria = param;
    }

    public BooleanExpression getPredicate() {

        Class<?> entityClass = criteria.getRootClass();
        String className = Character.toLowerCase(entityClass.getSimpleName().charAt(0)) + entityClass.getSimpleName().substring(1);
        PathBuilder<?> entityPath = new PathBuilder<>(entityClass, className);

        if(criteria.getValue() instanceof String){
            StringPath path = entityPath.getString(criteria.getKey());
            if (StringUtils.equals(criteria.getOperation(), ":")) {
                return path.containsIgnoreCase(criteria.getValue().toString());
            }
        } else if(criteria.getValue() instanceof Instant){
            DatePath<Instant> path = entityPath.getDate(criteria.getKey(), Instant.class);
            if (StringUtils.equals(criteria.getOperation(), ">")) {
                return path.after((Instant) criteria.getValue());
            }else if(StringUtils.equals(criteria.getOperation(), "<")){
                return path.before((Instant) criteria.getValue());
            }
        }

        logger.error("Search Criteria not handled: '{}'", criteria.getKey());

        return null;
    }
}
