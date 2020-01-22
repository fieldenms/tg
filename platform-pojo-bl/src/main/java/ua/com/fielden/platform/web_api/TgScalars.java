package ua.com.fielden.platform.web_api;
import java.math.BigDecimal;
import java.math.BigInteger;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import ua.com.fielden.platform.types.Money;

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
    
    private static boolean isNumberIsh(final Object input) {
        return input instanceof Number || input instanceof String;
    }
    
    private static String typeName(final Object input) {
        if (input == null) {
            return "null";
        }
        
        return input.getClass().getSimpleName();
    }
    
    /**
     * This represents the "Money" type which is a representation of {@link Money}.
     */
    public static final GraphQLScalarType GraphQLMoney = new GraphQLScalarType("Money", "Built-in ua.com.fielden.platform.types.Money", new Coercing<Money, Money>() {
        
        private Money convertImpl(final Object input) {
            if (isNumberIsh(input)) {
                try {
                    return new Money(input.toString());
                } catch (final NumberFormatException e) {
                    return null;
                }
            } else if (input instanceof Money) {
                return (Money) input;
            }
            return null;
            
        }
        
        @Override
        public Money serialize(final Object input) {
            final Money result = convertImpl(input);
            if (result == null) {
                throw new CoercingSerializeException(
                        "Expected type 'Money' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }
        
        @Override
        public Money parseValue(final Object input) {
            final Money result = convertImpl(input);
            if (result == null) {
                throw new CoercingParseValueException(
                        "Expected type 'Money' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }
        
        @Override
        public Money parseLiteral(final Object input) {
            if (input instanceof StringValue) {
                try {
                    return new Money(((StringValue) input).getValue());
                } catch (final NumberFormatException e) {
                    throw new CoercingParseLiteralException(
                            "Unable to turn AST input into a 'Money' : '" + String.valueOf(input) + "'"
                    );
                }
            } else if (input instanceof IntValue) {
                return new Money(new BigDecimal(((IntValue) input).getValue()));
            } else if (input instanceof FloatValue) {
                return new Money(((FloatValue) input).getValue());
            }
            throw new CoercingParseLiteralException(
                    "Expected AST type 'IntValue', 'StringValue' or 'FloatValue' but was '" + typeName(input) + "'."
            );
        }
    });
    
}