package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static ua.com.fielden.platform.expression.ast.visitor.TypeEnforcementVisitor.resolveExpressionTypes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.type.Day;
import ua.com.fielden.platform.expression.type.Month;
import ua.com.fielden.platform.expression.type.Year;
import ua.com.fielden.platform.types.Money;

public class TypeEnforcementTypeResolutionTest {

    @Test
    public void String_types_resolve_to_type_String() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Optional<Class<?>> type = resolveExpressionTypes(String.class, String.class);
        assertEquals(String.class, type.get());
    }
    
    @Test
    public void Date_types_resolve_to_type_Date() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Optional<Class<?>> type = resolveExpressionTypes(Date.class, Date.class);
        assertEquals(Date.class, type.get());
    }
    
    @Test
    public void Boolean_types_resolve_to_type_boolean() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Optional<Class<?>> type = resolveExpressionTypes(Boolean.class, Boolean.class);
        assertEquals(boolean.class, type.get());
    }

    @Test
    public void boolean_types_resolve_to_type_boolean() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Optional<Class<?>> type = resolveExpressionTypes(boolean.class, boolean.class);
        assertEquals(boolean.class, type.get());
    }

    @Test
    public void Boolean_and_boolean_types_resolve_to_type_boolean() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(boolean.class, Boolean.class);
        assertEquals(boolean.class, type.get());

        type = resolveExpressionTypes(Boolean.class, boolean.class);
        assertEquals(boolean.class, type.get());
    }

    @Test
    public void BigDecimal_types_resolve_to_type_BigDecimal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Optional<Class<?>> type = resolveExpressionTypes(BigDecimal.class, BigDecimal.class);
        assertEquals(BigDecimal.class, type.get());
    }
    
    @Test
    public void Day_types_resolve_to_type_Day() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Optional<Class<?>> type = resolveExpressionTypes(Day.class, Day.class);
        assertEquals(Day.class, type.get());
    }

    @Test
    public void Month_types_resolve_to_type_Month() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Optional<Class<?>> type = resolveExpressionTypes(Month.class, Month.class);
        assertEquals(Month.class, type.get());
    }
    
    @Test
    public void Year_types_resolve_to_type_Year() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Optional<Class<?>> type = resolveExpressionTypes(Year.class, Year.class);
        assertEquals(Year.class, type.get());
    }


    @Test
    public void String_and_Date_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Date.class, String.class);
        assertFalse(type.isPresent());

        type = resolveExpressionTypes(String.class, Date.class);
        assertFalse(type.isPresent());
    }

    @Test
    public void Boolean_and_Ddate_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Date.class, Boolean.class);
        assertFalse(type.isPresent());

        type = resolveExpressionTypes(Boolean.class, Date.class);
        assertFalse(type.isPresent());
    }

    @Test
    public void BigDecimal_and_Date_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Date.class, BigDecimal.class);
        assertFalse(type.isPresent());

        type = resolveExpressionTypes(BigDecimal.class, Date.class);
        assertFalse(type.isPresent());
    }


    @Test
    public void Integer_and_Date_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Date.class, Integer.class);
        assertFalse(type.isPresent());

        type = resolveExpressionTypes(Integer.class, Date.class);
        assertFalse(type.isPresent());
    }
    
    @Test
    public void Money_and_Date_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Date.class, Money.class);
        assertFalse(type.isPresent());
        
        type = resolveExpressionTypes(Money.class, Date.class);
        assertFalse(type.isPresent());
    }

    @Test
    public void Integer_and_String_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Integer.class, String.class);
        assertFalse(type.isPresent());

        type = resolveExpressionTypes(String.class, Integer.class);
        assertFalse(type.isPresent());
    }

    @Test
    public void BigDecimal_and_String_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(BigDecimal.class, String.class);
        assertFalse(type.isPresent());

        type = resolveExpressionTypes(String.class, BigDecimal.class);
        assertFalse(type.isPresent());
    }
    
    @Test
    public void Money_and_String_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Money.class, String.class);
        assertFalse(type.isPresent());

        type = resolveExpressionTypes(String.class, Money.class);
        assertFalse(type.isPresent());
    }
    
    @Test
    public void Boolean_and_String_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Boolean.class, String.class);
        assertFalse(type.isPresent());

        type = resolveExpressionTypes(String.class, Boolean.class);
        assertFalse(type.isPresent());
    }
    
    @Test
    public void boolean_and_String_types_are_incompatible() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(boolean.class, String.class);
        assertFalse(type.isPresent());

        type = resolveExpressionTypes(String.class, boolean.class);
        assertFalse(type.isPresent());
    }
    
    @Test
    public void Integer_and_BigDecimal_types_resolve_to_BigDecimal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Integer.class, BigDecimal.class);
        assertEquals(BigDecimal.class, type.get());

        type = resolveExpressionTypes(BigDecimal.class, Integer.class);
        assertEquals(BigDecimal.class, type.get());
    }

    @Test
    public void Integer_and_Money_types_resolve_to_Money() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Integer.class, Money.class);
        assertEquals(Money.class, type.get());

        type = resolveExpressionTypes(Money.class, Integer.class);
        assertEquals(Money.class, type.get());
    }

    @Test
    public void Money_and_BigDecimal_types_resolve_to_Money() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        Optional<Class<?>> type = resolveExpressionTypes(Money.class, BigDecimal.class);
        assertEquals(Money.class, type.get());

        type = resolveExpressionTypes(BigDecimal.class, Money.class);
        assertEquals(Money.class, type.get());
    }

}
