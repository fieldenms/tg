package ua.com.fielden.platform.web_api;
import static graphql.schema.GraphQLScalarType.newScalar;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.joda.time.format.ISODateTimeFormat.basicDate;
import static org.joda.time.format.ISODateTimeFormat.date;
import static org.joda.time.format.ISODateTimeFormat.dateElementParser;
import static org.joda.time.format.ISODateTimeFormat.time;
import static org.joda.time.format.ISODateTimeFormat.timeElementParser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

import org.joda.time.DateTime;
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
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
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
    public static final GraphQLScalarType GraphQLMoney = newScalar().name("Money").description("Money type.").coercing(new Coercing<Money, BigDecimal>() {
        
        //////////////////////////////////////////////// SERIALISE RESULTS ////////////////////////////////////////////////
        
        private Optional<BigDecimal> convertDataFetcherResult(final Object input) {
            if (input instanceof Money) {
                return of(((Money) input).getAmount());
            } else {
                return empty();
            }
        }
        
        @Override
        public BigDecimal serialize(final Object dataFetcherResult) {
            return convertDataFetcherResult(dataFetcherResult).orElseThrow(() -> new CoercingSerializeException("Expected type 'Money' but was '" + typeName(dataFetcherResult) + "'."));
        }
        
        //////////////////////////////////////////////// PARSE ARGUMENT VARIABLES ////////////////////////////////////////////////
        
        private Optional<Money> convertVariableInput(final Object input) {
            if (input instanceof Number) {
                try {
                    return of(new Money(input.toString()));
                } catch (final NumberFormatException e) {
                    return empty();
                }
            } else {
                return empty();
            }
        }
        
        @Override
        public Money parseValue(final Object variableInput) {
            return convertVariableInput(variableInput).orElseThrow(() -> new CoercingParseValueException("Expected number-like 'Money' but was '" + typeName(variableInput) + "'."));
        }
        
        //////////////////////////////////////////////// PARSE ARGUMENT LITERALS ////////////////////////////////////////////////
        
        private Optional<Money> convertArgumentInput(final Object argumentInput) {
            if (argumentInput instanceof IntValue) {
                final BigInteger value = ((IntValue) argumentInput).getValue();
                return of(new Money(new BigDecimal(value)));
            } else if (argumentInput instanceof FloatValue) {
                return of(new Money(((FloatValue) argumentInput).getValue()));
            } else {
                return empty();
            }
        }
        
        @Override
        public Money parseLiteral(final Object argumentInput) {
            return convertArgumentInput(argumentInput).orElseThrow(() -> new CoercingParseLiteralException("Expected number-like 'Money' but was '" + typeName(argumentInput) + "'."));
        }
        
    }).build();
    
    public static final DateTimeFormatter basicDateParser = basicDate();
    public static final DateTimeFormatter dateTimeParser = new DateTimeFormatterBuilder()
        .append(dateElementParser())
        .appendOptional(new DateTimeFormatterBuilder()
            .appendLiteral(' ')
            .appendOptional(timeElementParser().getParser())
            .toParser())
        .toFormatter();
    public static final DateTimeFormatter dateTimePrinter = new DateTimeFormatterBuilder()
        .append(date())
        .appendLiteral(' ')
        .append(time()) // TODO consider removing Z or +13:00 ?
        .toFormatter();
    
    /**
     * Scalar type for dates.
     */
    public static final GraphQLScalarType GraphQLDate = newScalar().name("Date").description("Date type.").coercing(new Coercing<Date, String>() {
        
        //////////////////////////////////////////////// SERIALISE RESULTS ////////////////////////////////////////////////
        
        private Optional<String> convertDataFetcherResult(final Object input) {
            if (input instanceof DateTime) {
                return of(dateTimePrinter.print(((DateTime) input)));
            } else if (input instanceof Date) {
                return of(dateTimePrinter.print(new DateTime(input)));
            } else {
                return empty();
            }
        }
        
        @Override
        public String serialize(final Object dataFetcherResult) {
            return convertDataFetcherResult(dataFetcherResult).orElseThrow(() -> new CoercingSerializeException("Expected type 'Date' or 'DateTime' but was '" + typeName(dataFetcherResult) + "'."));
        }
        
        //////////////////////////////////////////////// PARSE ARGUMENT ... ////////////////////////////////////////////////
        
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
        
        //////////////////////////////////////////////// ... VARIABLES ////////////////////////////////////////////////
        
        private Optional<Date> convertVariableInput(final Object input) {
            if (input instanceof Number) {
                return basicDateFrom(input.toString());
            } else if (input instanceof String) {
                return dateTimeFrom((String) input);
            } else {
                return empty();
            }
        }
        
        @Override
        public Date parseValue(final Object variableInput) {
            return convertVariableInput(variableInput).orElseThrow(() -> new CoercingParseValueException("Expected number-like or string-based 'Date' but was '" + typeName(variableInput) + "'."));
        }
        
        //////////////////////////////////////////////// ... LITERALS ////////////////////////////////////////////////
        
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
            return convertArgumentInput(argumentInput).orElseThrow(() -> new CoercingParseLiteralException("Expected number-like or string-based 'Date' but was '" + typeName(argumentInput) + "'."));
        }
        
    }).build();
    
    /**
     * Scalar type for {@link Hyperlink}.
     */
    public static final GraphQLScalarType GraphQLHyperlink = newScalar().name("Hyperlink").description("Hyperlink type.").coercing(new Coercing<Hyperlink, String>() {
        
        private Optional<String> convertDataFetcherResult(final Object input) {
            if (input instanceof Hyperlink) {
                return of(((Hyperlink) input).value);
            } else {
                return empty();
            }
        }
        
        @Override
        public String serialize(final Object dataFetcherResult) {
            return convertDataFetcherResult(dataFetcherResult).orElseThrow(() -> new CoercingSerializeException("Expected type 'Hyperlink' but was '" + typeName(dataFetcherResult) + "'."));
        }
        
        @Override
        public Hyperlink parseValue(final Object variableInput) {
            throw new CoercingParseValueException("Hyperlink arguments not supported.");
        }
        
        @Override
        public Hyperlink parseLiteral(final Object argumentInput) {
            throw new CoercingParseLiteralException("Hyperlink arguments not supported.");
        }
        
    }).build();
    
    /**
     * Scalar type for {@link Colour}.
     */
    public static final GraphQLScalarType GraphQLColour = newScalar().name("Colour").description("Colour type.").coercing(new Coercing<Colour, String>() {
        
        private Optional<String> convertDataFetcherResult(final Object input) {
            if (input instanceof Colour) {
                return of(((Colour) input).getColourValue());
            } else {
                return empty();
            }
        }
        
        @Override
        public String serialize(final Object dataFetcherResult) {
            return convertDataFetcherResult(dataFetcherResult).orElseThrow(() -> new CoercingSerializeException("Expected type 'Colour' but was '" + typeName(dataFetcherResult) + "'."));
        }
        
        @Override
        public Colour parseValue(final Object variableInput) {
            throw new CoercingParseValueException("Colour arguments not supported.");
        }
        
        @Override
        public Colour parseLiteral(final Object argumentInput) {
            throw new CoercingParseLiteralException("Colour arguments not supported.");
        }
        
    }).build();
    
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
        
        System.out.println(dateTimePrinter.parseDateTime("2020-03-29 16:45:45.999Z"));
    }
    
    
}