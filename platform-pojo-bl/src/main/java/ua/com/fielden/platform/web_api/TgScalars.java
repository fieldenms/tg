package ua.com.fielden.platform.web_api;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.ISODateTimeFormat;

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
    
    public static final DateTimeFormatter basicDateParser = ISODateTimeFormat.basicDate();
    public static final DateTimeFormatter dateTimeParser = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.dateElementParser())
        .appendOptional(new DateTimeFormatterBuilder()
            .appendLiteral(' ')
            .appendOptional(ISODateTimeFormat.timeElementParser().getParser())
            .toParser())
        .toFormatter();
    public static final DateTimeFormatter dateTimePrinter = new DateTimeFormatterBuilder()
            .append(ISODateTimeFormat.date())
            .appendLiteral(' ')
            .append(ISODateTimeFormat.time())
            .toFormatter();
    
    public static final GraphQLScalarType GraphQLDate = new GraphQLScalarType("Date", "Date type.", new Coercing<Date, Date>() {
        private Optional<Date> convertDataFetcherResult(final Object input) {
            if (input instanceof String) {
                return parseFrom((String) input, dateTimePrinter);
//            } else if (input instanceof DateTime) {
//                return of(((DateTime) input).toDate());
//            } else if (input instanceof Date) {
//                return of((Date) input);
            } else {
                return empty();
            }
        }
        
        private Optional<Date> convertVariableInput(final Object input) {
            if (input instanceof Number) {
                return basicDateFrom(input.toString());
            } else if (input instanceof String) {
                return basicDateFrom((String) input);
            } else {
                return empty();
            }
        }
        
        private Optional<Date> parseFrom(final String input, final DateTimeFormatter formatter) {
            try {
                return of(formatter.parseDateTime(input).toDate()); // server time-zone is used here
            } catch (final IllegalArgumentException e) {
                return empty();
            }
        }
        
        private Optional<Date> basicDateFrom(final String input) {
            return parseFrom(input, basicDateParser);
        }
        
        private Optional<Date> dateTimeFrom(final String input) {
            return parseFrom(input, dateTimeParser);
        }
        
        @Override
        public Date serialize(final Object dataFetcherResult) {
            return convertDataFetcherResult(dataFetcherResult).orElseThrow(() -> new CoercingSerializeException("Expected type 'Date' but was '" + typeName(dataFetcherResult) + "'."));
        }
        
        @Override
        public Date parseValue(final Object variableInput) {
            return convertVariableInput(variableInput).orElseThrow(() -> new CoercingParseValueException("Expected type 'Date' but was '" + typeName(variableInput) + "'."));
        }
        
        private Optional<Date> convertArgumentInput(final Object argumentInput) {
            if (argumentInput instanceof IntValue) {
                final BigInteger value = ((IntValue) argumentInput).getValue();
                return basicDateFrom(value.toString());
            } else if (argumentInput instanceof StringValue) {
                return dateTimeFrom(((StringValue) argumentInput).getValue());
            } else {
                return empty();
            }
        }
        
        @Override
        public Date parseLiteral(final Object argumentInput) {
            return convertArgumentInput(argumentInput).orElseThrow(() -> new CoercingParseLiteralException("Expected type 'Date' but was '" + typeName(argumentInput) + "'."));
        }
    });
    
    
    public static void main(final String[] args) {
        final DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
        final DateTimeFormatter parserWithOffset = ISODateTimeFormat.dateTimeParser().withOffsetParsed();
        System.out.println(parser.parseDateTime("2020-01-08"));
        System.out.println(parserWithOffset.parseDateTime("2020-01-08"));
        
        System.out.println();
        //System.out.println(parser.parseDateTime("2020-01-08 13:45"));
        //System.out.println(parserWithOffset.parseDateTime("2020-01-08 13:45"));
        
        final DateTimeParser time = new DateTimeFormatterBuilder()
                .appendLiteral(' ')
                .appendOptional(ISODateTimeFormat.timeElementParser().getParser())
                .toParser();
        final DateTimeFormatter dateTimeParser = new DateTimeFormatterBuilder()
                .append(ISODateTimeFormat.dateElementParser())
                .appendOptional(time)
                .toFormatter();
        
        System.out.println(dateTimeParser.parseDateTime("2020-03-08 13:45:23.789"));
        System.out.println(dateTimeParser.parseDateTime("2020-03-08 13:45:23"));
        System.out.println(dateTimeParser.parseDateTime("2020-03-08 13:45"));
        System.out.println(dateTimeParser.parseDateTime("2020-03-08 13"));
        System.out.println(dateTimeParser.parseDateTime("2020-03-08"));
        System.out.println(dateTimeParser.parseDateTime("2020-03"));
        System.out.println(dateTimeParser.parseDateTime("2020"));
        
        System.out.println();
        System.out.println(dateTimeParser.parseDateTime("2020-03-08 13.125"));
        System.out.println(dateTimeParser.parseDateTime("2020-03-08 13:45.125"));
        
        System.out.println(dateTimeParser.parseDateTime("2020-03-08 13:45:3"));
        System.out.println(dateTimeParser.parseDateTime("2020-03-08 13:5:3"));
        System.out.println(dateTimeParser.parseDateTime("2020-03-08 3:5:3"));
        System.out.println(dateTimeParser.parseDateTime("2020-3-8 3:5:3"));
    }
    
    
}