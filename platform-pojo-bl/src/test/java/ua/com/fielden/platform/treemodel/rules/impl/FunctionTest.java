package ua.com.fielden.platform.treemodel.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;

import org.junit.Test;

import ua.com.fielden.platform.domain.tree.EntityWithStringKeyType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.treemodel.rules.Function;
import ua.com.fielden.platform.treemodel.rules.FunctionUtils;
import ua.com.fielden.platform.types.Money;

/**
 * Test for {@link Function}.
 *
 * @author TG Team
 *
 */
public class FunctionTest {
    @SuppressWarnings("serial")
    @Test
    public void test_what_functions_are_applicable_to_what_argument_types() {
	// entity functions
	assertEquals("Incorrect applicable functions.", new HashSet<Function>() { { add(Function.SELF); add(Function.COUNT_DISTINCT); add(Function.ALL); add(Function.ANY); } }, FunctionUtils.functionsFor(AbstractEntity.class));
	assertEquals("Incorrect applicable functions.", new HashSet<Function>() { { add(Function.SELF); add(Function.COUNT_DISTINCT); add(Function.ALL); add(Function.ANY); } }, FunctionUtils.functionsFor(EntityWithStringKeyType.class));
	// string functions
	assertEquals("Incorrect applicable functions.", new HashSet<Function>() { { add(Function.SELF); add(Function.MIN); add(Function.MAX); add(Function.ALL); add(Function.ANY); } }, FunctionUtils.functionsFor(String.class));
	// number functions
	final HashSet<Function> numberFunctions = new HashSet<Function>() { { add(Function.SELF); add(Function.MIN); add(Function.MAX); add(Function.SUM); add(Function.AVG); add(Function.ALL); add(Function.ANY); } };
	assertEquals("Incorrect applicable functions.", numberFunctions, FunctionUtils.functionsFor(Money.class));
	assertEquals("Incorrect applicable functions.", numberFunctions, FunctionUtils.functionsFor(Number.class));
	assertEquals("Incorrect applicable functions.", numberFunctions, FunctionUtils.functionsFor(Long.class));
	assertEquals("Incorrect applicable functions.", numberFunctions, FunctionUtils.functionsFor(BigDecimal.class));
	assertEquals("Incorrect applicable functions.", numberFunctions, FunctionUtils.functionsFor(BigInteger.class));
	assertEquals("Incorrect applicable functions.", new HashSet<Function>() { { add(Function.SELF); add(Function.COUNT_DISTINCT); add(Function.MIN); add(Function.MAX); add(Function.SUM); add(Function.AVG); add(Function.ALL); add(Function.ANY); } }, FunctionUtils.functionsFor(Integer.class)); // added COUNT_DISTINCT to the list of default number functions
	// boolean functions
	assertEquals("Incorrect applicable functions.", new HashSet<Function>() { { add(Function.SELF); add(Function.COUNT_DISTINCT); add(Function.ALL); add(Function.ANY); } }, FunctionUtils.functionsFor(boolean.class));
	// date functions
	assertEquals("Incorrect applicable functions.", new HashSet<Function>() { { add(Function.SELF); add(Function.MIN); add(Function.MAX); add(Function.YEAR); add(Function.MONTH); add(Function.DAY); add(Function.ALL); add(Function.ANY); } }, FunctionUtils.functionsFor(Date.class));
    }

    @Test
    public void test_that_functions_result_types_are_correct() {
	selfAllAnyFuncCheck(Function.SELF);

	dateFuncCheck(Function.YEAR);
	dateFuncCheck(Function.MONTH);
	dateFuncCheck(Function.DAY);

	countFuncCheck(Function.COUNT_DISTINCT);

	minMaxFuncCheck(Function.MIN);
	minMaxFuncCheck(Function.MAX);

	sumAvgFuncCheck(Function.AVG);
	sumAvgFuncCheck(Function.SUM);

	selfAllAnyFuncCheck(Function.ALL);
	selfAllAnyFuncCheck(Function.ANY);
    }

    protected void countFuncCheck(final Function func) {
	resultTypeFor(func, AbstractEntity.class, Integer.class);
	resultTypeFor(func, EntityWithStringKeyType.class, Integer.class);
	incompatibleArgTypeFor(func, String.class);
	incompatibleArgTypeFor(func, Money.class);
	incompatibleArgTypeFor(func, Number.class);
	incompatibleArgTypeFor(func, Long.class);
	incompatibleArgTypeFor(func, BigDecimal.class);
	incompatibleArgTypeFor(func, BigInteger.class);
	resultTypeFor(func, Integer.class, Integer.class);
	resultTypeFor(func, boolean.class, Integer.class);
	incompatibleArgTypeFor(func, Date.class);
    }


    protected void selfAllAnyFuncCheck(final Function func) {
	resultTypeFor(func, AbstractEntity.class, AbstractEntity.class);
	resultTypeFor(func, EntityWithStringKeyType.class, EntityWithStringKeyType.class);
	resultTypeFor(func, String.class, String.class);
	resultTypeFor(func, Money.class, Money.class);
	resultTypeFor(func, Number.class, Number.class);
	resultTypeFor(func, Long.class, Long.class);
	resultTypeFor(func, BigDecimal.class, BigDecimal.class);
	resultTypeFor(func, BigInteger.class, BigInteger.class);
	resultTypeFor(func, Integer.class, Integer.class);
	resultTypeFor(func, boolean.class, boolean.class);
	resultTypeFor(func, Date.class, Date.class);
    }

    protected void dateFuncCheck(final Function dateFunc) {
	incompatibleArgTypeFor(dateFunc, AbstractEntity.class);
	incompatibleArgTypeFor(dateFunc, EntityWithStringKeyType.class);
	incompatibleArgTypeFor(dateFunc, String.class);
	incompatibleArgTypeFor(dateFunc, Money.class);
	incompatibleArgTypeFor(dateFunc, Number.class);
	incompatibleArgTypeFor(dateFunc, Long.class);
	incompatibleArgTypeFor(dateFunc, BigDecimal.class);
	incompatibleArgTypeFor(dateFunc, BigInteger.class);
	incompatibleArgTypeFor(dateFunc, Integer.class);
	incompatibleArgTypeFor(dateFunc, boolean.class);
	resultTypeFor(dateFunc, Date.class, Integer.class);
    }

    protected void minMaxFuncCheck(final Function minMaxFunc) {
	incompatibleArgTypeFor(minMaxFunc, AbstractEntity.class);
	incompatibleArgTypeFor(minMaxFunc, EntityWithStringKeyType.class);
	resultTypeFor(minMaxFunc, String.class, String.class);
	resultTypeFor(minMaxFunc, Money.class, Money.class);
	resultTypeFor(minMaxFunc, Number.class, Number.class);
	resultTypeFor(minMaxFunc, Long.class, Long.class);
	resultTypeFor(minMaxFunc, BigDecimal.class, BigDecimal.class);
	resultTypeFor(minMaxFunc, BigInteger.class, BigInteger.class);
	resultTypeFor(minMaxFunc, Integer.class, Integer.class);
	incompatibleArgTypeFor(minMaxFunc, boolean.class);
	resultTypeFor(minMaxFunc, Date.class, Date.class);
    }

    protected void sumAvgFuncCheck(final Function minMaxFunc) {
	incompatibleArgTypeFor(minMaxFunc, AbstractEntity.class);
	incompatibleArgTypeFor(minMaxFunc, EntityWithStringKeyType.class);
	incompatibleArgTypeFor(minMaxFunc, String.class);
	resultTypeFor(minMaxFunc, Money.class, Money.class);
	resultTypeFor(minMaxFunc, Number.class, Number.class);
	resultTypeFor(minMaxFunc, Long.class, Long.class);
	resultTypeFor(minMaxFunc, BigDecimal.class, BigDecimal.class);
	resultTypeFor(minMaxFunc, BigInteger.class, BigInteger.class);
	resultTypeFor(minMaxFunc, Integer.class, Integer.class);
	incompatibleArgTypeFor(minMaxFunc, boolean.class);
	incompatibleArgTypeFor(minMaxFunc, Date.class);
    }

    protected void resultTypeFor(final Function f, final Class<?> argumentType, final Class<?> resultType) {
	assertEquals("Incorrect result type for function.", resultType, f.resultType(argumentType));
    }

    protected void incompatibleArgTypeFor(final Function f, final Class<?> argumentType) {
	try {
	    f.resultType(argumentType);
	    fail("Should be incompatible.");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_that_functions_query_strings_are_correct() {
	assertEquals("Incorrect query string for function.", "([prop1])", Function.SELF.equeryString("prop1"));
	assertEquals("Incorrect query string for function.", "YEAR([prop1])", Function.YEAR.equeryString("prop1"));
	assertEquals("Incorrect query string for function.", "YEAR([prop2]) * 100 + MONTH([prop2])", Function.MONTH.equeryString("prop2"));
	assertEquals("Incorrect query string for function.", "YEAR([prop3]) * 10000 + MONTH([prop3]) * 100 + DAY([prop3])", Function.DAY.equeryString("prop3"));

	assertEquals("Incorrect query string for function.", "COUNT(DISTINCT([prop1]))", Function.COUNT_DISTINCT.equeryString("prop1"));
	assertEquals("Incorrect query string for function.", "MIN([prop2])", Function.MIN.equeryString("prop2"));
	assertEquals("Incorrect query string for function.", "MAX([prop3])", Function.MAX.equeryString("prop3"));
	assertEquals("Incorrect query string for function.", "SUM([prop4])", Function.SUM.equeryString("prop4"));
	assertEquals("Incorrect query string for function.", "AVG([prop5])", Function.AVG.equeryString("prop5"));

	assertEquals("Incorrect query string for function.", "ALL([prop1])", Function.ALL.equeryString("prop1"));
	assertEquals("Incorrect query string for function.", "ANY([prop2])", Function.ANY.equeryString("prop2"));
    }
}
