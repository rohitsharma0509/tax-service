package com.scb.rider.tax.util;

import com.scb.rider.tax.constants.SearchConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SearchUtil {

    private SearchUtil() {}

    public static List<Sort.Order> getSortedOrderList(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();
        log.info("Sort List Before Modification" + pageable.getSort());
        pageable.getSort().forEach(sortOrder -> {
            if(!(sortOrder.getProperty().equals("DESC") || sortOrder.getProperty().equals("ASC"))){
                if (sortOrder.getProperty().equals(SearchConstants.DESC) || sortOrder.getProperty().equals(SearchConstants.ASC)) {
                    Sort.Order orderLastElement = orders.get(orders.size() - 1);
                    Sort.Direction direction = sortOrder.getProperty().equals(SearchConstants.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
                    orders.set(orders.size() - 1, getSortedField(orderLastElement.getProperty(), direction));
                } else {
                    orders.add(getSortedField(sortOrder.getProperty(), sortOrder.getDirection()));
                }
                log.info(String.format("Sorting field %s order %s", sortOrder.getProperty(), sortOrder.getDirection()));
            }
        });
        if (ObjectUtils.isEmpty(orders)) {
            log.info(String.format("Sorting field %s order %s", SearchConstants.INVOICE_NUMBER, "ASC"));
            orders.add(new Sort.Order(Sort.Direction.ASC, SearchConstants.INVOICE_NUMBER));
        }
        return orders;
    }

    private static Sort.Order getSortedField(String fieldName, Sort.Direction Direction) {
        return Direction.equals(Sort.Direction.ASC) ? new Sort.Order(Sort.Direction.ASC, fieldName)
                : new Sort.Order(Sort.Direction.DESC, fieldName);
    }

    public static Criteria getCriteria(String invoiceNumber, String fileName, String dateOfRun, String startTime, String endTime, String status, String reason) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList = createCriteriaAndFilter(invoiceNumber, SearchConstants.INVOICE_NUMBER, StringUtils.EMPTY, criteriaList);
        criteriaList = createCriteriaOrFilter(dateOfRun, SearchConstants.PAYMENT_DATE_TIME, SearchConstants.DATE_FORMAT2, SearchConstants.DATE_FORMAT3, StringUtils.EMPTY, criteriaList);
        criteriaList = createCriteriaAndFilter(startTime, SearchConstants.START_TIME, StringUtils.EMPTY, criteriaList);
        criteriaList = createCriteriaAndFilter(endTime, SearchConstants.END_TIME, StringUtils.EMPTY, criteriaList);
        criteriaList = createCriteriaAndFilter(reason, SearchConstants.REASON, StringUtils.EMPTY, criteriaList);
        criteriaList = createCriteriaAndFilter(fileName, SearchConstants.FILE_NAME, StringUtils.EMPTY, criteriaList);
        criteriaList = createCriteriaAndFilter(status, SearchConstants.STATUS, StringUtils.EMPTY, criteriaList);
        if (CollectionUtils.isEmpty(criteriaList)) {
            criteriaList.add(Criteria.where(SearchConstants.INVOICE_NUMBER).regex(StringUtils.EMPTY, SearchConstants.INCLUDE));
        }
        Criteria criteria = new Criteria();
        criteria.andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
        return criteria;
    }

    private static List<Criteria> createCriteriaOrFilter(String fieldValue, String columnOne,
                                                  String columnTwo, String columnThree, String startsWith, List<Criteria> criteriaList) {
        if (!StringUtils.isEmpty(fieldValue)) {
            criteriaList.add(
                    new Criteria().orOperator(Criteria.where(columnOne).regex(startsWith + fieldValue, SearchConstants.INCLUDE),
                            Criteria.where(columnTwo).regex(startsWith + fieldValue, SearchConstants.INCLUDE),
                            Criteria.where(columnThree).regex(startsWith + fieldValue, SearchConstants.INCLUDE)));
        }
        return criteriaList;
    }

    private static List<Criteria> createCriteriaAndFilter(String fieldValue, String columnOne,
                                                   String startsWith, List<Criteria> criteriaList) {
        if (!StringUtils.isEmpty(fieldValue) && !StringUtils.isEmpty(startsWith)) {
            criteriaList.add(Criteria.where(columnOne).regex(startsWith + fieldValue, SearchConstants.INCLUDE));
        } else if (!StringUtils.isEmpty(fieldValue)) {
            criteriaList.add(Criteria.where(columnOne).regex(fieldValue, SearchConstants.INCLUDE));
        }
        return criteriaList;
    }
}
