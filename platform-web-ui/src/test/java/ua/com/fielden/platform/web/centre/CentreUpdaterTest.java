package ua.com.fielden.platform.web.centre;

import static java.math.RoundingMode.HALF_UP;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.AND_BEFORE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.DATE_MNEMONIC;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.DATE_PREFIX;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.EXCLUSIVE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.EXCLUSIVE2;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.NOT;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.OR_NULL;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.VALUE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.VALUE2;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.web.centre.CentreUpdater.createEmptyDifferences;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockNotFoundEntity;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createNotFoundMockString;
import static ua.com.fielden.snappy.DateRangePrefixEnum.NEXT;
import static ua.com.fielden.snappy.DateRangePrefixEnum.PREV;
import static ua.com.fielden.snappy.MnemonicEnum.MONTH;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisationPersistentChild;
import ua.com.fielden.platform.types.Money;

/**
 * Unit tests for {@link CentreUpdater} API methods, particularly for
 * <p>
 * 1. {@link CentreUpdater#createDifferences(ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer, ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer, Class)}<br>
 * 2. {@link CentreUpdater#applyDifferences(ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer, java.util.Map, Class)}
 * 
 * @author TG Team
 *
 */
public class CentreUpdaterTest extends CentreUpdaterTestMixin {
    private static final BigDecimal ONE_AND_LITTLE = new BigDecimal(1.01).setScale(3, HALF_UP);
    private static final Money ONE_AND_LITTLE_MONEY = new Money(ONE_AND_LITTLE, Currency.getInstance("UAH"));
    private static final Date d2018_time = new DateTime(2018, 1, 1, 5, 6).toDate();
    private static final Date d2019_time = new DateTime(2019, 1, 1, 5, 6).toDate();
    private static final Date d2020 = new DateTime(2020, 1, 1, 0, 0).toDate();
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @Test
    public void no_mutations() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {}, createEmptyDifferences());
    }
    
    @Test
    public void mutate_2_the_same_values() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setOrNull(ROOT, "datePropDefault", true);
            centre.getFirstTick().setNot(ROOT, "datePropDefault", true);
            centre.getFirstTick().setExclusive(ROOT, "datePropDefault", true);
            centre.getFirstTick().setExclusive2(ROOT, "datePropDefault", true);
            centre.getFirstTick().setDatePrefix(ROOT, "datePropDefaultMnemonics", NEXT);
            centre.getFirstTick().setDateMnemonic(ROOT, "datePropDefaultMnemonics", MONTH);
            centre.getFirstTick().setAndBefore(ROOT, "datePropDefaultMnemonics", true);
            centre.getFirstTick().setValue(ROOT, "datePropDefault", d2018);
        }, createEmptyDifferences());
    }
    
    // missing value
    
    @Test
    public void missing_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setOrNull(ROOT, "dateProp", true), expectedDiffWithValue("dateProp", OR_NULL.name(), true));
    }
    
    @Test
    public void default_missing_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setOrNull(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", OR_NULL.name(), null));
    }
    
    // negation
    
    @Test
    public void negation() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setNot(ROOT, "dateProp", true), expectedDiffWithValue("dateProp", NOT.name(), true));
    }
    
    @Test
    public void default_negation() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setNot(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", NOT.name(), null));
    }
    
    // exclusiveness
    
    @Test
    public void left_exclusive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setExclusive(ROOT, "dateProp", true), expectedDiffWithValue("dateProp", EXCLUSIVE.name(), true));
    }
    
    @Test
    public void default_left_exclusive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setExclusive(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", EXCLUSIVE.name(), null));
    }
    
    @Test
    public void right_exclusive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setExclusive2(ROOT, "dateProp", true), expectedDiffWithValue("dateProp", EXCLUSIVE2.name(), true));
    }
    
    @Test
    public void default_right_exclusive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setExclusive2(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", EXCLUSIVE2.name(), null));
    }
    
    // date prefixes and mnemonics
    
    @Test
    public void date_prefix_and_mnemonic() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "dateProp", PREV);
            centre.getFirstTick().setDateMnemonic(ROOT, "dateProp", MONTH);
        }, expectedDiffWithValues("dateProp", t2(DATE_PREFIX.name(), PREV.name()), t2(DATE_MNEMONIC.name(), MONTH.name())));
    }
    
    @Test
    public void default_date_prefix_and_mnemonic_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "datePropDefaultMnemonics", null);
            centre.getFirstTick().setDateMnemonic(ROOT, "datePropDefaultMnemonics", null);
        }, expectedDiffWithValues("datePropDefaultMnemonics", t2(DATE_PREFIX.name(), null), t2(DATE_MNEMONIC.name(), null)));
    }
    
    @Test
    public void default_date_prefix_and_mnemonic_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "datePropDefaultMnemonics", PREV); // previous value: NEXT MONTH AND BEFORE 
        }, expectedDiffWithValue("datePropDefaultMnemonics", DATE_PREFIX.name(), PREV.name())); // new value: PREV MONTH AND BEFORE 
    }
    
    // date "and before"
    
    @Test
    public void date_AND_BEFORE_2_true() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "dateProp", PREV);
            centre.getFirstTick().setDateMnemonic(ROOT, "dateProp", MONTH);
            centre.getFirstTick().setAndBefore(ROOT, "dateProp", true);
        }, expectedDiffWithValues("dateProp", t2(DATE_PREFIX.name(), PREV.name()), t2(DATE_MNEMONIC.name(), MONTH.name()), t2(AND_BEFORE.name(), true)));
    }
    
    @Test
    public void date_AND_BEFORE_2_false() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "dateProp", PREV);
            centre.getFirstTick().setDateMnemonic(ROOT, "dateProp", MONTH);
            centre.getFirstTick().setAndBefore(ROOT, "dateProp", false);
        }, expectedDiffWithValues("dateProp", t2(DATE_PREFIX.name(), PREV.name()), t2(DATE_MNEMONIC.name(), MONTH.name()), t2(AND_BEFORE.name(), false)));
    }
    
    @Test
    public void default_date_AND_BEFORE_2_false() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setAndBefore(ROOT, "datePropDefaultMnemonics", false); // previous value: NEXT MONTH AND BEFORE 
        }, expectedDiffWithValue("datePropDefaultMnemonics", AND_BEFORE.name(), false)); // new value: NEXT MONTH AND AFTER
    }
    
    @Test
    public void default_date_AND_BEFORE_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setAndBefore(ROOT, "datePropDefaultMnemonics", null); // previous value: NEXT MONTH AND BEFORE 
        }, expectedDiffWithValue("datePropDefaultMnemonics", AND_BEFORE.name(), null)); // new value: NEXT MONTH
    }
    
    // date values
    
    @Test
    public void left_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "dateProp", d2018), expectedDiffWithValue("dateProp", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void default_left_date_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", VALUE.name(), null));
    }
    
    @Test
    public void default_left_date_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropDefault", d2019), expectedDiffWithValue("datePropDefault", VALUE.name(), Long.toString(d2019.getTime())));
    }
    
    @Test
    public void left_crit_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropCrit", d2018), expectedDiffWithValue("datePropCrit", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void crit_single_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropCritSingle", d2018), expectedDiffWithValue("datePropCritSingle", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void left_UTC_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropUtc", d2018), expectedDiffWithValue("datePropUtc", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void left_dateOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropDateOnly", d2018), expectedDiffWithValue("datePropDateOnly", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void left_timeOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropTimeOnly", d2018_time), expectedDiffWithValue("datePropTimeOnly", VALUE.name(), Long.toString(d2018_time.getTime())));
    }
    
    @Test
    public void right_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "dateProp", d2018), expectedDiffWithValue("dateProp", VALUE2.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void default_right_date_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", VALUE2.name(), null));
    }
    
    @Test
    public void default_right_date_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropDefault", d2020), expectedDiffWithValue("datePropDefault", VALUE2.name(), Long.toString(d2020.getTime())));
    }
    
    @Test
    public void right_crit_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropCrit", d2019), expectedDiffWithValue("datePropCrit", VALUE2.name(), Long.toString(d2019.getTime())));
    }
    
    @Test
    public void right_UTC_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropUtc", d2019), expectedDiffWithValue("datePropUtc", VALUE2.name(), Long.toString(d2019.getTime())));
    }
    
    @Test
    public void right_dateOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropDateOnly", d2019), expectedDiffWithValue("datePropDateOnly", VALUE2.name(), Long.toString(d2019.getTime())));
    }
    
    @Test
    public void right_timeOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropTimeOnly", d2019_time), expectedDiffWithValue("datePropTimeOnly", VALUE2.name(), Long.toString(d2019_time.getTime())));
    }
    
    // please note that DateTime-typed properties is not used in practice and thus will not be tested and supported (however, see EntityWithDateTimeProp / WorkbookExporterTest and EntityWithRangeProperties for [perhaps] artificial examples of such properties)
    
    // entity values
    
    @Test
    public void entity_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "entityProp", listOf("A*", "*B")), expectedDiffWithValue("entityProp", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void default_entity_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "entityPropDefault", listOf()), expectedDiffWithValue("entityPropDefault", VALUE.name(), listOf()));
    }
    
    @Test
    public void default_entity_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "entityPropDefault", listOf("A*", "*B")), expectedDiffWithValue("entityPropDefault", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void crit_entity_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "entityPropCrit", listOf("A*", "*B")), expectedDiffWithValue("entityPropCrit", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void propertyDescriptor_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "propertyDescriptorProp", listOf("A*", "*B")), expectedDiffWithValue("propertyDescriptorProp", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void crit_propertyDescriptor_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "propertyDescriptorPropCrit", listOf("A*", "*B")), expectedDiffWithValue("propertyDescriptorPropCrit", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void crit_single_propertyDescriptor_value() {
        final PropertyDescriptor<TgCentreDiffSerialisationPersistentChild> propertyVal = new PropertyDescriptor<TgCentreDiffSerialisationPersistentChild>(TgCentreDiffSerialisationPersistentChild.class, "stringProp");
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "propertyDescriptorPropCritSingle", propertyVal), expectedDiffWithValue("propertyDescriptorPropCritSingle", VALUE.name(), propertyVal.toString()));
    }
    
    @Test
    public void crit_single_propertyDescriptor_value_notFound() {
        final PropertyDescriptor<TgCentreDiffSerialisationPersistentChild> propertyVal = (PropertyDescriptor<TgCentreDiffSerialisationPersistentChild>) createMockNotFoundEntity(PropertyDescriptor.class, "UNKNOWN");
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "propertyDescriptorPropCritSingle", propertyVal), expectedDiffWithValue("propertyDescriptorPropCritSingle", VALUE.name(), createNotFoundMockString("UNKNOWN")));
    }
    
    // string values
    
    @Test
    public void string_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "stringProp", "A*,*B"), expectedDiffWithValue("stringProp", VALUE.name(), "A*,*B"));
    }
    
    @Test
    public void default_string_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "stringPropDefault", null), expectedDiffWithValue("stringPropDefault", VALUE.name(), null));
    }
    
    @Test
    public void default_string_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "stringPropDefault", "A*,*B"), expectedDiffWithValue("stringPropDefault", VALUE.name(), "A*,*B"));
    }
    
    @Test
    public void crit_string_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "stringPropCrit", "A*,*B"), expectedDiffWithValue("stringPropCrit", VALUE.name(), "A*,*B"));
    }
    
    @Test
    public void crit_single_string_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "stringPropCritSingle", "VAL"), expectedDiffWithValue("stringPropCritSingle", VALUE.name(), "VAL"));
    }
    
    // boolean values
    
    @Test
    public void left_boolean_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "booleanProp", false), expectedDiffWithValue("booleanProp", VALUE.name(), false));
    }
    
    @Test
    public void default_left_boolean_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "booleanPropDefault", true), expectedDiffWithValue("booleanPropDefault", VALUE.name(), true));
    }
    
    @Test
    public void crit_left_boolean_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "booleanPropCrit", false), expectedDiffWithValue("booleanPropCrit", VALUE.name(), false));
    }
    
    @Test
    public void crit_single_boolean_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "booleanPropCritSingle", false), expectedDiffWithValue("booleanPropCritSingle", VALUE.name(), false));
    }
    
    @Test
    public void right_boolean_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "booleanProp", false), expectedDiffWithValue("booleanProp", VALUE2.name(), false));
    }
    
    @Test
    public void default_right_boolean_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "booleanPropDefault", true), expectedDiffWithValue("booleanPropDefault", VALUE2.name(), true));
    }
    
    @Test
    public void crit_right_boolean_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "booleanPropCrit", false), expectedDiffWithValue("booleanPropCrit", VALUE2.name(), false));
    }
    
    // integer values
    
    @Test
    public void left_integer_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerProp", new Integer(0)), expectedDiffWithValue("integerProp", VALUE.name(), "0"));
    }
    
    @Test
    public void default_left_integer_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerPropDefault", null), expectedDiffWithValue("integerPropDefault", VALUE.name(), null));
    }
    
    @Test
    public void default_left_integer_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerPropDefault", new Integer(1)), expectedDiffWithValue("integerPropDefault", VALUE.name(), "1"));
    }
    
    @Test
    public void left_crit_integer_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerPropCrit", new Integer(0)), expectedDiffWithValue("integerPropCrit", VALUE.name(), "0"));
    }
    
    @Test
    public void crit_single_integer_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerPropCritSingle", new Integer(0)), expectedDiffWithValue("integerPropCritSingle", VALUE.name(), "0"));
    }
    
    @Test
    public void right_integer_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "integerProp", new Integer(0)), expectedDiffWithValue("integerProp", VALUE2.name(), "0"));
    }
    
    @Test
    public void default_right_integer_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "integerPropDefault", null), expectedDiffWithValue("integerPropDefault", VALUE2.name(), null));
    }
    
    @Test
    public void default_right_integer_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "integerPropDefault", new Integer(1)), expectedDiffWithValue("integerPropDefault", VALUE2.name(), "1"));
    }
    
    @Test
    public void right_crit_integer_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "integerPropCrit", new Integer(0)), expectedDiffWithValue("integerPropCrit", VALUE2.name(), "0"));
    }
    
    @Test
    public void left_integer_value_byte() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerProp", new Integer(127)), expectedDiffWithValue("integerProp", VALUE.name(), "127"));
    }
    
    @Test
    public void left_integer_value_byte_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerProp", new Integer(-127)), expectedDiffWithValue("integerProp", VALUE.name(), "-127"));
    }
    
    @Test
    public void left_integer_value_short() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerProp", new Integer(32767)), expectedDiffWithValue("integerProp", VALUE.name(), "32767"));
    }
    
    @Test
    public void left_integer_value_short_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerProp", new Integer(-32768)), expectedDiffWithValue("integerProp", VALUE.name(), "-32768"));
    }
    
    @Test
    public void left_integer_value_int() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerProp", new Integer(2147483647)), expectedDiffWithValue("integerProp", VALUE.name(), "2147483647"));
    }
    
    @Test
    public void left_integer_value_int_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerProp", new Integer(-2147483648)), expectedDiffWithValue("integerProp", VALUE.name(), "-2147483648"));
    }
    
    @Test
    public void left_integer_value_primitive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "integerProp", 32767), expectedDiffWithValue("integerProp", VALUE.name(), "32767"));
    }
    
    // long values
    
    @Test
    public void left_long_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", new Long(0L)), expectedDiffWithValue("longProp", VALUE.name(), "0"));
    }
    
    @Test
    public void default_left_long_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longPropDefault", null), expectedDiffWithValue("longPropDefault", VALUE.name(), null));
    }
    
    @Test
    public void default_left_long_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longPropDefault", new Long(1L)), expectedDiffWithValue("longPropDefault", VALUE.name(), "1"));
    }
    
    @Test
    public void left_crit_long_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longPropCrit", new Long(0L)), expectedDiffWithValue("longPropCrit", VALUE.name(), "0"));
    }
    
    @Test
    public void crit_single_long_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longPropCritSingle", new Long(0L)), expectedDiffWithValue("longPropCritSingle", VALUE.name(), "0"));
    }
    
    @Test
    public void right_long_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "longProp", new Long(0L)), expectedDiffWithValue("longProp", VALUE2.name(), "0"));
    }
    
    @Test
    public void default_right_long_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "longPropDefault", null), expectedDiffWithValue("longPropDefault", VALUE2.name(), null));
    }
    
    @Test
    public void default_right_long_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "longPropDefault", new Long(1L)), expectedDiffWithValue("longPropDefault", VALUE2.name(), "1"));
    }
    
    @Test
    public void right_crit_long_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "longPropCrit", new Long(0L)), expectedDiffWithValue("longPropCrit", VALUE2.name(), "0"));
    }
    
    @Test
    public void left_long_value_byte() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", new Long(127L)), expectedDiffWithValue("longProp", VALUE.name(), "127"));
    }
    
    @Test
    public void left_long_value_byte_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", new Long(-127L)), expectedDiffWithValue("longProp", VALUE.name(), "-127"));
    }
    
    @Test
    public void left_long_value_short() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", new Long(32767L)), expectedDiffWithValue("longProp", VALUE.name(), "32767"));
    }
    
    @Test
    public void left_long_value_short_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", new Long(-32768L)), expectedDiffWithValue("longProp", VALUE.name(), "-32768"));
    }
    
    @Test
    public void left_long_value_int() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", new Long(2147483647L)), expectedDiffWithValue("longProp", VALUE.name(), "2147483647"));
    }
    
    @Test
    public void left_long_value_int_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", new Long(-2147483648L)), expectedDiffWithValue("longProp", VALUE.name(), "-2147483648"));
    }
    
    @Test
    public void left_long_value_long() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", new Long(9223372036854775807L)), expectedDiffWithValue("longProp", VALUE.name(), "9223372036854775807"));
    }
    
    @Test
    public void left_long_value_long_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", new Long(-9223372036854775808L)), expectedDiffWithValue("longProp", VALUE.name(), "-9223372036854775808"));
    }
    
    @Test
    public void left_long_value_primitive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "longProp", 32767L), expectedDiffWithValue("longProp", VALUE.name(), "32767"));
    }
    
    // big decimal values
    
    @Test
    public void left_bigDecimal_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", ONE_AND_LITTLE), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "1.010"));
    }
    
    @Test
    public void default_left_bigDecimal_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalPropDefault", null), expectedDiffWithValue("bigDecimalPropDefault", VALUE.name(), null));
    }
    
    @Test
    public void default_left_bigDecimal_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalPropDefault", ONE_AND_LITTLE), expectedDiffWithValue("bigDecimalPropDefault", VALUE.name(), "1.010"));
    }
    
    @Test
    public void left_crit_bigDecimal_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalPropCrit", ONE_AND_LITTLE), expectedDiffWithValue("bigDecimalPropCrit", VALUE.name(), "1.010"));
    }
    
    @Test
    public void crit_single_bigDecimal_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalPropCritSingle", ONE_AND_LITTLE), expectedDiffWithValue("bigDecimalPropCritSingle", VALUE.name(), "1.010"));
    }
    
    @Test
    public void right_bigDecimal_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "bigDecimalProp", ONE_AND_LITTLE), expectedDiffWithValue("bigDecimalProp", VALUE2.name(), "1.010"));
    }
    
    @Test
    public void default_right_bigDecimal_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "bigDecimalPropDefault", null), expectedDiffWithValue("bigDecimalPropDefault", VALUE2.name(), null));
    }
    
    @Test
    public void default_right_bigDecimal_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "bigDecimalPropDefault", ONE_AND_LITTLE), expectedDiffWithValue("bigDecimalPropDefault", VALUE2.name(), "1.010"));
    }
    
    @Test
    public void right_crit_bigDecimal_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "bigDecimalPropCrit", ONE_AND_LITTLE), expectedDiffWithValue("bigDecimalPropCrit", VALUE2.name(), "1.010"));
    }
    
    @Test
    public void left_bigDecimal_value_byte() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal(127)), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "127"));
    }
    
    @Test
    public void left_bigDecimal_value_byte_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal(-127)), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "-127"));
    }
    
    @Test
    public void left_bigDecimal_value_short() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal(32767)), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "32767"));
    }
    
    @Test
    public void left_bigDecimal_value_short_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal(-32768)), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "-32768"));
    }
    
    @Test
    public void left_bigDecimal_value_int() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal(2147483647)), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "2147483647"));
    }
    
    @Test
    public void left_bigDecimal_value_int_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal(-2147483648)), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "-2147483648"));
    }
    
    @Test
    public void left_bigDecimal_value_long() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal(9223372036854775807L)), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "9223372036854775807"));
    }
    
    @Test
    public void left_bigDecimal_value_long_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal(-9223372036854775808L)), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "-9223372036854775808"));
    }
    
    @Test
    public void left_bigDecimal_value_bigDecimal() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal("2222222222222222222222222.100200")), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "2222222222222222222222222.100200"));
    }
    
    @Test
    public void left_bigDecimal_value_bigDecimal_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "bigDecimalProp", new BigDecimal("-2222222222222222222222222.100200")), expectedDiffWithValue("bigDecimalProp", VALUE.name(), "-2222222222222222222222222.100200"));
    }
    
    // money values (please remember that inner representation of Money.amount is 4-scaled BigDecimal)
    
    @Test
    public void left_money_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", ONE_AND_LITTLE_MONEY), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "1.0100"), t2("currency", "UAH"))));
    }
    
    @Test
    public void default_left_money_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyPropDefault", null), expectedDiffWithValue("moneyPropDefault", VALUE.name(), null));
    }
    
    @Test
    public void default_left_money_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyPropDefault", ONE_AND_LITTLE_MONEY), expectedDiffWithValue("moneyPropDefault", VALUE.name(), mapOf(t2("amount", "1.0100"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_crit_money_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyPropCrit", ONE_AND_LITTLE_MONEY), expectedDiffWithValue("moneyPropCrit", VALUE.name(), mapOf(t2("amount", "1.0100"), t2("currency", "UAH"))));
    }
    
    @Test
    public void crit_single_money_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyPropCritSingle", ONE_AND_LITTLE_MONEY), expectedDiffWithValue("moneyPropCritSingle", VALUE.name(), mapOf(t2("amount", "1.0100"), t2("currency", "UAH"))));
    }
    
    @Test
    public void right_money_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "moneyProp", ONE_AND_LITTLE_MONEY), expectedDiffWithValue("moneyProp", VALUE2.name(), mapOf(t2("amount", "1.0100"), t2("currency", "UAH"))));
    }
    
    @Test
    public void default_right_money_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "moneyPropDefault", null), expectedDiffWithValue("moneyPropDefault", VALUE2.name(), null));
    }
    
    @Test
    public void default_right_money_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "moneyPropDefault", ONE_AND_LITTLE_MONEY), expectedDiffWithValue("moneyPropDefault", VALUE2.name(), mapOf(t2("amount", "1.0100"), t2("currency", "UAH"))));
    }
    
    @Test
    public void right_crit_money_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "moneyPropCrit", ONE_AND_LITTLE_MONEY), expectedDiffWithValue("moneyPropCrit", VALUE2.name(), mapOf(t2("amount", "1.0100"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_byte() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal(127), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "127.0000"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_byte_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal(-127), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "-127.0000"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_short() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal(32767), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "32767.0000"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_short_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal(-32768), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "-32768.0000"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_int() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal(2147483647), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "2147483647.0000"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_int_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal(-2147483648), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "-2147483648.0000"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_long() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal(9223372036854775807L), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "9223372036854775807.0000"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_long_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal(-9223372036854775808L), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "-9223372036854775808.0000"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_bigDecimal() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal("2222222222222222222222222.100200"), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "2222222222222222222222222.1002"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_bigDecimal_negative() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal("-2222222222222222222222222.100200"), Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "-2222222222222222222222222.1002"), t2("currency", "UAH"))));
    }
    
    @Test
    public void left_money_value_with_taxPercent() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "moneyProp", new Money(new BigDecimal("3.1002"), 20, Currency.getInstance("UAH"))), expectedDiffWithValue("moneyProp", VALUE.name(), mapOf(t2("amount", "3.1002"), t2("taxPercent", "20"), t2("currency", "UAH"))));
    }
    
}