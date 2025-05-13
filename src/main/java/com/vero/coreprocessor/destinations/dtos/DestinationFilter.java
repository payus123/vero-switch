package com.vero.coreprocessor.destinations.dtos;


import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.utils.*;
import com.vero.coreprocessor.utils.filters.*;
import jakarta.persistence.criteria.*;
import lombok.*;
import org.apache.commons.lang3.*;

import java.util.*;

@Getter
@Setter
public class DestinationFilter extends BaseFilter {
    private String name;
    private String search;
    private String institutionId;


    @Override
    public <T, D> void filter(QueryBuilder<T, D> queryBuilder) {
        super.filter(queryBuilder);

        if (!StringUtils.isEmpty(name)) {
            queryBuilder.equal("destinationName", name);
        }


        Root<T> root =queryBuilder.getRoot();

        if (!StringUtils.isEmpty(institutionId)) {
            Join<Destination, InstitutionDestination> instDestJoin = root.join("institutionDestinations", JoinType.INNER);
            queryBuilder.equal(instDestJoin.get("institutionId"), institutionId);
        }



        filterByString(queryBuilder);
    }

    private <T, D> void filterByString(QueryBuilder<T, D> queryBuilder) {
        if (search != null && !StringUtils.isEmpty(search)) {
            final String[] filterables = search.trim().split("%20|\\s+");

            List<String> stringValues = new ArrayList<>();

            for (String value : filterables) {
                value = value.trim();

                stringValues.add(value);
            }

            Predicate existingPredicate = queryBuilder.getCriteriaBuilder().and(queryBuilder.getPredicates());
            queryBuilder.setPredicates(); // reset predicates

            List<String> stringFields = List.of("destinationName");

            // string query
            stringFields.forEach(field -> stringValues.forEach(value -> queryBuilder.like(field, "%" + value + "%")));


            Predicate searchPredicate = queryBuilder.getCriteriaBuilder().or(queryBuilder.getPredicates());

            queryBuilder.setPredicates(queryBuilder.getCriteriaBuilder().and(existingPredicate, searchPredicate));
        }
    }

}
