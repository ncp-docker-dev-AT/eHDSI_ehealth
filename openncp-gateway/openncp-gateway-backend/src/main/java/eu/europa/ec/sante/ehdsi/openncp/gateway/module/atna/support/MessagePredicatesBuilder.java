package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.support;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MessagePredicatesBuilder {

    private final List<SearchCriteria> params;

    public MessagePredicatesBuilder() {
        params = new ArrayList<>();
    }

    public MessagePredicatesBuilder with(String key, String operation, Object value) {
        if (value != null) {
            params.add(new SearchCriteria(key, operation, value));
        }
        return this;
    }

    public BooleanExpression build() {
        if (CollectionUtils.isEmpty(params)) {
            return null;
        }

        List<BooleanExpression> predicates = params.stream().map(param -> {
            MessagePredicate predicate = new MessagePredicate(param);
            return predicate.getPredicate();
        }).filter(Objects::nonNull).collect(Collectors.toList());

        BooleanExpression result = Expressions.asBoolean(true).isTrue();
        for (BooleanExpression predicate : predicates) {
            result = result.and(predicate);
        }
        return result;
    }
}
