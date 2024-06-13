package ua.com.fielden.platform.web_api;
import static graphql.schema.GraphQLScalarType.newScalar;
import static java.lang.String.format;
import static org.joda.time.format.ISODateTimeFormat.basicDate;
import static org.joda.time.format.ISODateTimeFormat.date;
import static org.joda.time.format.ISODateTimeFormat.dateElementParser;
import static org.joda.time.format.ISODateTimeFormat.time;
import static org.joda.time.format.ISODateTimeFormat.timeElementParser;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.either.Either.right;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import com.google.inject.Inject;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLScalarType.Builder;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.utils.IDates;

/**
 * TG-specific GraphQL Web API scalar type implementations.
 * 
 * @author TG Team
 *
 */
public class GraphQLScalars {
    private static final Logger LOGGER = LogManager.getLogger(GraphQLScalars.class);
    private static final String UNEXPECTED_TYPE_ERROR = "Expected [%s] but was [%s].";

    @Inject
    private static IDates dates;
    
    /**
     * Creates builder for scalar type and assigns name and description derived from the specified {@code title}.
     * 
     * @param title
     * @return
     */
    private static Builder newScalarType(final String title) {
        return newScalar().name(title).description(format("%s type.", title));
    }
    
    /**
     * Returns left {@link Either} for unexpected data error showing the type name for that unexpected data.
     * 
     * @param expected
     * @param unexpected
     * @return
     */
    private static <R> Either<String, R> error(final String expected, final Object unexpected) {
        return left(format(UNEXPECTED_TYPE_ERROR, expected, unexpected.getClass().getSimpleName()));
    }
    
    /////////////////////////////////////////////////////////////// SCALAR TYPES WITHOUT ARGUMENTS ///////////////////////////////////////////////////////////////
    
    /**
     * Private interface for output scalar {@link Coercing} to increase level of abstraction for implementing scalars.
     * 
     * @author TG Team
     *
     * @param <O> -- output type to which fetched data converts
     */
    private static interface TgOutputCoercing<O> {
        
        /**
         * Scalar type title.
         * 
         * @return
         */
        String title();
        /**
         * Converts {@code dataFetcherResult} to either value of type {@code O} if successful or otherwise returns error message.
         * 
         * @param dataFetcherResult
         * @return
         */
        Either<String, O> convertDataFetcherResult(final Object dataFetcherResult);
        
    }
    
    /**
     * Private interface for scalar {@link Coercing} to increase level of abstraction for implementing scalars that do not support arguments.
     * 
     * @author TG Team
     *
     * @param <O> -- output type to which fetched data converts
     */
    private static interface TgCoercingNoArguments<O> extends Coercing<Object, O>, TgOutputCoercing<O> {
        
        @Override
        default O serialize(final Object dataFetcherResult) {
            return convertDataFetcherResult(dataFetcherResult).orElseThrow(error -> {
                final var ex = new CoercingSerializeException(error);
                LOGGER.error(ex.getMessage(), ex);
                return ex;
            });
        }
        
        @Override
        default Object parseValue(final Object variableInput) {
            final var ex = new CoercingParseValueException(format("%s argument variables not supported.", title()));
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
        
        @Override
        default Object parseLiteral(final Object argumentInput) {
            final var ex = new CoercingParseLiteralException(format("%s argument literals not supported.", title()));
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }
    
    /**
     * GraphQL scalar implementation for {@link Hyperlink} type.
     */
    public static final GraphQLScalarType GraphQLHyperlink = newScalarType("Hyperlink").coercing(new TgCoercingNoArguments<String>() {
        
        @Override
        public String title() {
            return "Hyperlink";
        };
        
        @Override
        public Either<String, String> convertDataFetcherResult(final Object dataFetcherResult) {
            if (dataFetcherResult instanceof Hyperlink) {
                return right(((Hyperlink) dataFetcherResult).value);
            } else {
                return error(title(), dataFetcherResult);
            }
        }
        
    }).build();
    
    /**
     * GraphQL scalar implementation for {@link Colour} type.
     */
    public static final GraphQLScalarType GraphQLColour = newScalarType("Colour").coercing(new TgCoercingNoArguments<String>() {
        
        @Override
        public String title() {
            return "Colour";
        };
        
        @Override
        public Either<String, String> convertDataFetcherResult(final Object dataFetcherResult) {
            if (dataFetcherResult instanceof Colour) {
                return right(((Colour) dataFetcherResult).getColourValue());
            } else {
                return error(title(), dataFetcherResult);
            }
        }
        
    }).build();
    
    /////////////////////////////////////////////////////////////// SCALAR TYPES ///////////////////////////////////////////////////////////////
    
    /**
     * Private interface for scalar {@link Coercing} to increase level of abstraction for implementing scalars.
     * 
     * @author TG Team
     *
     * @param <I> -- input type to which argument literals / variables convert
     * @param <O> -- output type to which fetched data converts
     */
    private static interface TgCoercing<I, O> extends Coercing<I, O>, TgOutputCoercing<O> {
        
        @Override
        default O serialize(final Object dataFetcherResult) {
            return convertDataFetcherResult(dataFetcherResult).orElseThrow(error -> {
                final var ex = new CoercingSerializeException(error);
                LOGGER.error(ex.getMessage(), ex);
                return ex;
            });
        }
        
        /**
         * Converts {@code variableInput} to either value of type {@code I} if successful or otherwise returns error message.
         * 
         * @param variableInput
         * @return
         */
        Either<String, I> convertVariableInput(final Object variableInput);
        
        @Override
        default I parseValue(final Object variableInput) {
            return convertVariableInput(variableInput).orElseThrow(error -> {
                final var ex = new CoercingParseValueException(error);
                LOGGER.error(ex.getMessage(), ex);
                return ex;
            });
        }
        
        /**
         * Converts {@code literalInput} to either value of type {@code I} if successful or otherwise returns error message.
         * 
         * @param literalInput
         * @return
         */
        Either<String, I> convertLiteralInput(final Object literalInput);
        
        @Override
        default I parseLiteral(final Object literalInput) {
            return convertLiteralInput(literalInput).orElseThrow(error -> {
                final var ex = new CoercingParseLiteralException(error);
                LOGGER.error(ex.getMessage(), ex);
                return ex;
            });
        }
        
    }
    
    /**
     * GraphQL scalar implementation for {@link Money} type.
     */
    public static final GraphQLScalarType GraphQLMoney = newScalarType("Money").coercing(new TgCoercing<Money, BigDecimal>() {
        
        @Override
        public String title() {
            return "Money";
        };
        
        //////////////////////////////////////////////// SERIALISE RESULTS ////////////////////////////////////////////////
        
        @Override
        public Either<String, BigDecimal> convertDataFetcherResult(final Object dataFetcherResult) {
            if (dataFetcherResult instanceof Money) {
                return right(((Money) dataFetcherResult).getAmount());
            } else {
                return error(title(), dataFetcherResult);
            }
        }
        
        //////////////////////////////////////////////// PARSE ARGUMENT VARIABLES ////////////////////////////////////////////////
        
        @Override
        public Either<String, Money> convertVariableInput(final Object variableInput) {
            if (variableInput instanceof Number) {
                try {
                    return right(new Money(variableInput.toString()));
                } catch (final NumberFormatException e) {
                    return error("number-like " + title(), variableInput);
                }
            } else {
                return error("number-like " + title(), variableInput);
            }
        }
        
        //////////////////////////////////////////////// PARSE ARGUMENT LITERALS ////////////////////////////////////////////////
        
        @Override
        public Either<String, Money> convertLiteralInput(final Object literalInput) {
            if (literalInput instanceof IntValue) {
                final BigInteger value = ((IntValue) literalInput).getValue();
                return right(new Money(new BigDecimal(value)));
            } else if (literalInput instanceof FloatValue) {
                return right(new Money(((FloatValue) literalInput).getValue()));
            } else {
                return error("number-like " + title(), literalInput);
            }
        }
        
    }).build();
    
    private static final DateTimeFormatter dateTimePrinter = new DateTimeFormatterBuilder()
        .append(date())
        .appendLiteral(' ')
        .append(time())
        .toFormatter();
    
    /**
     * Creates simplistic {@link Date} representation that has string-based 'value' and 'millis'.
     * 
     * @param date
     * @return
     */
    public static Map<String, Object> createDateRepr(final Date date) {
        return linkedMapOf(
            t2("value", dateTimePrinter.print(dates.zoned(date))),
            t2("millis", date.getTime())
        );
    }
    
    /**
     * GraphQL scalar implementation for {@link Date} type.
     */
    public static final GraphQLScalarType GraphQLDate = newScalar().name("Date")
            .description("Date type.\n\nInput formats:  \n20221002  \n\"2022\"  \n\"2022-10\"  \n\"2022-10-02\"  \n\"2022-10-02 14\"  \n\"2022-10-02 14:07\"  \n\"2022-10-02 14:07:19\"  \n\"2022-10-02 14:07:19.999\"")
            .coercing(new TgCoercing<Date, Map<String, Object>>() {
        private final DateTimeFormatter basicDateParser = basicDate();
        private final DateTimeFormatter dateTimeParser = new DateTimeFormatterBuilder()
            .append(dateElementParser())
            .appendOptional(new DateTimeFormatterBuilder()
                .appendLiteral(' ')
                .appendOptional(timeElementParser().getParser())
                .toParser())
            .toFormatter();
        
        @Override
        public String title() {
            return "Date";
        }
        
        //////////////////////////////////////////////// SERIALISE RESULTS ////////////////////////////////////////////////
        
        @Override
        public Either<String, Map<String, Object>> convertDataFetcherResult(final Object dataFetcherResult) {
            if (dataFetcherResult instanceof Date) {
                return right(createDateRepr((Date) dataFetcherResult)); // request time-zone is used here (or default for independent time-zone mode)
            } else {
                return error(title(), dataFetcherResult);
            }
        }
        
        //////////////////////////////////////////////// PARSE ARGUMENT ... ////////////////////////////////////////////////
        
        private Either<String, Date> parseFrom(final String input, final DateTimeFormatter formatter) {
            try {
                return right(formatter.withZone(dates.timeZone()).parseDateTime(input).toDate()); // request time-zone is used here (or default for independent time-zone mode)
            } catch (final IllegalArgumentException e) {
                return left(format(UNEXPECTED_TYPE_ERROR, "number-like or string-based " + title(), input));
            }
        }
        
        private Either<String, Date> basicDateFrom(final String input) {
            return parseFrom(input, basicDateParser);
        }
        
        private Either<String, Date> dateTimeFrom(final String input) {
            return parseFrom(input, dateTimeParser);
        }
        
        //////////////////////////////////////////////// ... VARIABLES ////////////////////////////////////////////////
        
        @Override
        public Either<String, Date> convertVariableInput(final Object variableInput) {
            if (variableInput instanceof Number) {
                return basicDateFrom(variableInput.toString());
            } else if (variableInput instanceof String) {
                return dateTimeFrom((String) variableInput);
            } else {
                return error("number-like or string-based " + title(), variableInput);
            }
        }
        
        //////////////////////////////////////////////// ... LITERALS ////////////////////////////////////////////////
        
        @Override
        public Either<String, Date> convertLiteralInput(final Object literalInput) {
            if (literalInput instanceof IntValue) {
                final BigInteger value = ((IntValue) literalInput).getValue();
                return basicDateFrom(value.toString());
            } else if (literalInput instanceof StringValue) {
                return dateTimeFrom(((StringValue) literalInput).getValue());
            } else {
                return error("number-like or string-based " + title(), literalInput);
            }
        }
        
    }).build();
    
}