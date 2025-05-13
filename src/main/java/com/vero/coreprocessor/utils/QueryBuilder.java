package com.vero.coreprocessor.utils;

import com.vero.coreprocessor.config.*;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.*;
import org.springframework.data.domain.*;

import java.util.*;


public class
QueryBuilder<T, D> {
    protected final EntityManager entityManager;
    protected final CriteriaBuilder criteriaBuilder;
    protected final CriteriaQuery<D> criteriaQuery;

    private final CriteriaQuery<Long> countQuery;
    private final Root<T> countRoot;
    protected final Root<T> root;
    protected Class<T> entityClass;
    protected Class<D> responseClass;
    private final boolean isEntitySameAsResponse;
    protected Collection<Predicate> predicates = new ArrayList<>();
    protected final Collection<Predicate> countPredicates = new ArrayList<>();
    private final Map<String, JoinType> joins = new HashMap<>();


    private QueryBuilder(EntityManager entityManager, Class<T> entityClass, Class<D> responseClass, boolean responseClassAsSource) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
        this.responseClass = responseClass;
        criteriaBuilder = entityManager.getCriteriaBuilder();
        criteriaQuery = criteriaBuilder.createQuery(responseClass);
        root = criteriaQuery.from(entityClass);
        isEntitySameAsResponse = responseClassAsSource;

        countQuery = criteriaBuilder.createQuery(Long.class);
        countRoot = countQuery.from(entityClass);
    }

    public static <T, D> QueryBuilder<T, D> build(Class<T> entityClass, Class<D> responseClass) {
        return build(entityClass, responseClass, entityClass.equals(responseClass));
    }

    public static <T, D> QueryBuilder<T, D> build(Class<T> entityClass, Class<D> responseClass, boolean responseClassAsSource) {
        return new QueryBuilder<>(SpringContextConfig.getBean(EntityManager.class), entityClass, responseClass, responseClassAsSource);
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return criteriaBuilder;
    }

    public CriteriaQuery<D> getCriteriaQuery() {
        return criteriaQuery;
    }

    public Root<T> getRoot() {
        return root;
    }

    public Root<T> getCountRoot() {
        return countRoot;
    }

    public Predicate[] getPredicates() {
        return predicates.toArray(new Predicate[0]);
    }

    public void setPredicates(Predicate... predicates) {
        this.predicates = new ArrayList<>();
        Collections.addAll(this.predicates, predicates);
    }

    private TypedQuery<D> getQuery() {
        criteriaQuery.where(getPredicates());

        if (!isEntitySameAsResponse)
            criteriaQuery.select(criteriaBuilder.construct(responseClass, root));

        return entityManager.createQuery(criteriaQuery);
    }

    public long getRecordCount() {

        if (!joins.isEmpty()) {
            joins.forEach((model, joinType) -> join(countRoot, model, joinType));
        }

        countQuery.where(countPredicates.toArray(new Predicate[0]))
                .select(criteriaBuilder.count(countRoot));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    public List<D> getResult() {
        return getQuery().getResultList();
    }

    public Page<D> getResult(Pageable pageable) {
        if (!pageable.getSort().isEmpty())
            orderBy(pageable);

        TypedQuery<D> query = getQuery();
        int pageNumber = pageable.getPageNumber();
        query.setFirstResult(pageNumber * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, getRecordCount());
    }

    public D getSingleResult() {
        return getQuery().getSingleResult();
    }

    public QueryBuilder<T, D> addPredicate(Predicate predicate, boolean forCount) {

        final boolean b = forCount ? countPredicates.add(predicate) : predicates.add(predicate);

        return this;
    }

    public QueryBuilder<T, D> orderBy(String column) { // order by ASC
        return orderBy(column, false);
    }


    public QueryBuilder<T, D> orderBy(String column, boolean desc) {
        final Path<Object> objectPath = root.get(column);
        criteriaQuery.orderBy(desc ? criteriaBuilder.desc(objectPath) : criteriaBuilder.asc(objectPath));
        return this;
    }

    public QueryBuilder<T, D> orderBy(Order... orders) {
        criteriaQuery.orderBy(Arrays.stream(orders).map(order -> (order.isAsc) ?
                criteriaBuilder.asc(root.get(order.column)) : criteriaBuilder.desc(root.get(order.column))).toList());
        return this;
    }

    public QueryBuilder<T, D> orderBy(List<Order> orders) {
        criteriaQuery.orderBy(orders.stream().map(order -> (order.isAsc) ?
                criteriaBuilder.asc(root.get(order.column)) : criteriaBuilder.desc(root.get(order.column))).toList());
        return this;
    }

    private void orderBy(Pageable pageable) {
        final List<jakarta.persistence.criteria.Order> orders = pageable.getSort().stream().map(sortOrder -> {
            if (sortOrder.isAscending()) {
                return criteriaBuilder.asc(root.get(sortOrder.getProperty()));
            } else {
                return criteriaBuilder.desc(root.get(sortOrder.getProperty()));
            }
        }).toList();

        criteriaQuery.orderBy(orders);
    }

    public Join<Object, Object> join(String model, JoinType joinType) {
        joins.put(model, joinType);
        return join(root, model, joinType);
    }

//    public Join<Object, Object> join(Root<T> root, String model, JoinType joinType) {
//        Join<Object, Object> join = root.join(model, joinType);
//        join.on(criteriaBuilder.equal(root.get(model), join));
//
//        return join;
//    }

    public Join<Object, Object> join(Root<T> root, String model, JoinType joinType) {
        return root.join(model, joinType);
    }

    public QueryBuilder<T, D> equal(String column, Object value) {
        return addPredicate(criteriaBuilder.equal(root.get(column), value), false)
                .addPredicate(criteriaBuilder.equal(countRoot.get(column), value), true);
    }

    public QueryBuilder<T, D> defaultPredicate() {
        return addPredicate(criteriaBuilder.conjunction(),false)
                .addPredicate(criteriaBuilder.conjunction(),true);
    }


    public QueryBuilder<T, D> equal(String column, String key, Object value) {
        return addPredicate(criteriaBuilder.equal(root.get(column).get(key), value), false)
                .addPredicate(criteriaBuilder.equal(countRoot.get(column).get(key), value), true);
    }

    public QueryBuilder<T, D> equal(Expression<?> x, Object y) {
        return addPredicate(criteriaBuilder.equal(x, y), false);
    }

    public Predicate equals(Expression<?> x, Object y) {
        return criteriaBuilder.equal(x, y);
    }

    public QueryBuilder<T, D> notEqual(String column, Object value) {
        return addPredicate(criteriaBuilder.notEqual(root.get(column), value), false)
                .addPredicate(criteriaBuilder.notEqual(countRoot.get(column), value), true);
    }

    public QueryBuilder<T, D> or(String column, Object value) {
        Predicate predicate = criteriaBuilder.equal(root.get(column), value);
        predicates.add(predicate);
        return this;
    }

    public Predicate or2(String column, Object value) {
        return criteriaBuilder.equal(root.get(column), value);
    }


    public QueryBuilder<T, D> or(String column1, Object value1, String column2, Object value2) {
        Predicate predicate1 = criteriaBuilder.equal(root.get(column1), value1);
        Predicate predicate2 = criteriaBuilder.equal(root.get(column2), value2);
        Predicate orPredicate = criteriaBuilder.or(predicate1, predicate2);
        predicates.add(orPredicate);
        return this;
    }

    public QueryBuilder<T, D> like(String column, String value) {
        return addPredicate(criteriaBuilder.like(criteriaBuilder.lower(root.get(column)), value.toLowerCase()), false)
                .addPredicate(criteriaBuilder.like(criteriaBuilder.lower(countRoot.get(column)), value.toLowerCase()), true);
    }

    public <Y extends Comparable<? super Y>> QueryBuilder<T, D> lessThan(String column, Y value) {
        return addPredicate(criteriaBuilder.lessThan(root.get(column), value), false)
                .addPredicate(criteriaBuilder.lessThan(countRoot.get(column), value), true);
    }

    public <Y extends Comparable<? super Y>> QueryBuilder<T, D> lessThanOrEqualTo(String column, Y value) {
        return addPredicate(criteriaBuilder.lessThanOrEqualTo(root.get(column), value), false)
                .addPredicate(criteriaBuilder.lessThanOrEqualTo(countRoot.get(column), value), true);
    }

    public QueryBuilder<T, D> inAttributePath(String attributePath, List<?> values) {
        String[] attributePathParts = attributePath.split("\\.");

        Path<Object> path = (Path<Object>) root;
        Path<Object> countPath = (Path<Object>) countRoot;

        for (String attribute : attributePathParts) {
            path = path.get(attribute);
            countPath = countPath.get(attribute);
        }

        final CriteriaBuilder.In<Object> inClause = criteriaBuilder.in(path);
        final CriteriaBuilder.In<Object> countInClause = criteriaBuilder.in(countPath);

        for (Object value : values) {
            inClause.value(value);
            countInClause.value(value);
        }

        return addPredicate(inClause, false).addPredicate(countInClause, true);
    }


    public <Y extends Comparable<? super Y>> QueryBuilder<T, D> greaterThan(String column, Y value) {
        return addPredicate(criteriaBuilder.greaterThan(root.get(column), value), false)
                .addPredicate(criteriaBuilder.greaterThan(countRoot.get(column), value), true);
    }

    public <Y extends Comparable<? super Y>> QueryBuilder<T, D> greaterThanOrEqualTo(String column, Y value) {
        return addPredicate(criteriaBuilder.greaterThanOrEqualTo(root.get(column), value), false)
                .addPredicate(criteriaBuilder.greaterThanOrEqualTo(countRoot.get(column), value), true);
    }

    public <Y extends Comparable<? super Y>> QueryBuilder<T, D> greaterThanOrEqualTo(String column, String key, Y value) {
        return addPredicate(criteriaBuilder.greaterThanOrEqualTo(root.get(column).get(key), value), false)
                .addPredicate(criteriaBuilder.greaterThanOrEqualTo(countRoot.get(column).get(key), value), true);
    }

    public QueryBuilder<T, D> in(String column, List<?> values) {
        final CriteriaBuilder.In<Object> inClause = criteriaBuilder.in(root.get(column));
        final CriteriaBuilder.In<Object> countInClause = criteriaBuilder.in(countRoot.get(column));

        for (Object value : values) {
            inClause.value(value);
            countInClause.value(value);
        }

        return addPredicate(inClause, false).addPredicate(countInClause, true);
    }

    public QueryBuilder<T, D> orEqual(String column1, Object value1, String column2, Object value2) {
        Predicate predicate1 = criteriaBuilder.equal(root.get(column1), value1);
        Predicate predicate2 = criteriaBuilder.equal(root.get(column2), value2);
        Predicate orPredicate = criteriaBuilder.or(predicate1, predicate2);
        predicates.add(orPredicate);
        return this;
    }

    public QueryBuilder<T, D> orEqual(Path<?> path1, Object value1, Path<?> path2, Object value2) {
        Predicate predicate1 = criteriaBuilder.equal(path1, value1);
        Predicate predicate2 = criteriaBuilder.equal(path2, value2);
        Predicate orPredicate = criteriaBuilder.or(predicate1, predicate2);
         predicates.add(orPredicate);
        return this;

    }

    public <Y extends Comparable<Y>> QueryBuilder<T, D> between(String column, Y value1, Y value2) {
        return addPredicate(criteriaBuilder.between(root.get(column), value1, value2), false)
                .addPredicate(criteriaBuilder.between(countRoot.get(column), value1, value2), true);
    }

    public <S> QueryBuilder<T, D> exists(Subquery<S> subQuery) {
        return addPredicate(criteriaBuilder.exists(subQuery), false);
    }

    public <S> QueryBuilder<T, D> notExists(Subquery<S> subQuery) {
        return addPredicate(criteriaBuilder.not(criteriaBuilder.exists(subQuery)), false);
    }

    public QueryBuilder<T, D> groupBy(String... columns) {
        final Expression<?>[] expressions = Arrays.stream(columns).map(root::get).toList().toArray(new Expression<?>[0]);

        criteriaQuery.groupBy(expressions);
        return this;
    }

    public QueryBuilder<T, D> groupBy(String alias) {
        Expression<String> aliasExpression = criteriaBuilder.literal(alias);
        Expression<String> aliasExpression2 = criteriaBuilder.literal("paymentReference");

        List<Expression<?>> grouping = new ArrayList<>();
        grouping.add(aliasExpression);
        grouping.add(aliasExpression2);

        criteriaQuery.groupBy(grouping);
        return this;
    }


    public QueryBuilder<T, D> select(boolean distinct, Select... selects) {
        final Expression<?>[] expressions = Arrays.stream(selects).map(s -> {
            if (s.aggregate == null && s.function == null)
                return root.get(s.column());

            if (s.function != null) {
                criteriaBuilder.function(s.function.getFunction(), String.class, root.get(s.column)
                        , criteriaBuilder.literal(s.function.getLiteral())).alias(s.function.alias);

                Expression<?> expression = criteriaBuilder.function(
                        s.function.getFunction(),
                        String.class,
                        root.get(s.column),
                        criteriaBuilder.literal(s.function.getLiteral())
                );


            }

            switch (s.aggregate()) {
                case COUNT -> {
                    return criteriaBuilder.count(root.get(s.column()));
                }
                case COUNT_DISTINCT -> {
                    return criteriaBuilder.countDistinct(root.get(s.column()));
                }
                case SUM -> {
                    return criteriaBuilder.sum(root.get(s.column()));
                }
                case MAX -> {
                    return criteriaBuilder.max(root.get(s.column()));
                }
                case MIN -> {
                    return criteriaBuilder.min(root.get(s.column()));
                }
                default -> throw new RuntimeException("Aggregate function is not supported.");
            }


        }).toList().toArray(new Expression<?>[0]);
        if (distinct) {
            criteriaQuery.multiselect(expressions);
            criteriaQuery.distinct(true);
        } else {
            criteriaQuery.multiselect(expressions);
        }
        return this;
    }

    public record Select(Aggregate aggregate, String column, Function function) {
    }

    public enum Aggregate {
        SUM, COUNT, AVG, MAX, MIN, COUNT_DISTINCT
    }

    public record Order(String column, boolean isAsc) {
    }

    @Data
    public static class Function {
        private String function;
        private String literal;
        private String alias;
    }


}
