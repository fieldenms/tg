package ua.com.fielden.platform.eql.retrieval.records;

import org.hibernate.type.Type;

/**
 * Contains data to be provided for each column retrieved within Hibernate Native Query execution.
 * 
 *  @param column -- column name
 *  
 *  @param hibType -- instance of Hibernate converter
 * 
 * @author TG Team
 *
 */
public record HibernateScalar(
        String column, 
        Type hibType) {
}