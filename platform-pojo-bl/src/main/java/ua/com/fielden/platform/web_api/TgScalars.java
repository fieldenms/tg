package ua.com.fielden.platform.web_api;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TgScalars {
    
    public static GraphQLScalarType GraphQLBigDecimal = new GraphQLScalarType("BigDecimal", "BigDecimal scalar type for TG platform", new Coercing() {
        @Override
        public Double serialize(final Object input) {
            if (input instanceof String) {
                return Double.parseDouble((String) input);
            } else if (input instanceof Double) {
                return (Double) input;
            } else if (input instanceof Float) {
                return (double) (Float) input;
            } else if (input instanceof Integer) {
                return (double) (Integer) input;
            // ADDED
            //    TODO consider re-using of Scalars.GraphQLFloat code here?
            } else if (input instanceof BigDecimal) {
                return ((BigDecimal) input).doubleValue();
            } else if (input instanceof BigInteger) {
                return ((BigInteger) input).doubleValue();
            // ADDED [end]
            } else {
                return null;
            }
        }

        @Override
        public Object parseValue(final Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(final Object input) {
            if (input instanceof IntValue) {
                return ((IntValue) input).getValue().doubleValue();
            } else if (input instanceof FloatValue) {
                return ((FloatValue) input).getValue().doubleValue();
            } else {
                return null;
            }
        }
    });
}