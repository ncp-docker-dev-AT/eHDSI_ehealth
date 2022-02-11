package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.support;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SearchPredicatesBuilder {

    private final List<SearchCriteria> params;

    public SearchPredicatesBuilder() {
        params = new ArrayList<>();
    }

    public SearchPredicatesBuilder with(Class<?> rootClass, String key, String operation, Object value) {
        if (value != null && !StringUtils.isEmpty(value.toString())){
            params.add(new SearchCriteria(rootClass, key, operation, value));
        }
        return this;
    }

    public BooleanExpression build() {
        if (CollectionUtils.isEmpty(params)) {
            return null;
        }

        List<BooleanExpression> predicates = params.stream().map(param -> {
            SearchPredicate predicate = new SearchPredicate(param);
            return predicate.getPredicate();
        }).filter(Objects::nonNull).collect(Collectors.toList());

        BooleanExpression result = Expressions.asBoolean(true).isTrue();
        for (BooleanExpression predicate : predicates) {
            result = result.and(predicate);
        }
        return result;
    }
}
