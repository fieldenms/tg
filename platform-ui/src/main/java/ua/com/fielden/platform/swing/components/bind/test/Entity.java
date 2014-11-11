package ua.com.fielden.platform.swing.components.bind.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdesktop.swingx.calendar.DateUtils;
import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.types.Money;

/**
 * Entity class used for binding testing.
 *
 * @author Jhou
 *
 */
@KeyType(String.class)
@DisplayDescription
public class Entity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    /**
     * 3 textField properties, 1 checkBox property and 1 radioButton property and 2 autocompleter properties
     */
    @IsProperty
    private Integer number; //  = new Integer(299)
    @IsProperty
    private BigDecimal bigDecimal = new BigDecimal(2889);
    @IsProperty
    @Dependent("string")
    private Money money = new Money(new BigDecimal(244.4));
    @IsProperty
    private Double doubleProperty = new Double(211.1);
    @IsProperty
    @Dependent({ "money", "doubleProperty" })
    private String string = "Machine";
    @IsProperty
    private String password;
    @IsProperty
    private Boolean bool = true;
    @IsProperty
    private Strategy strategy = Strategy.COMMIT;
    // NOTE : the initial value have to be not null, to correctly handle MetaProperty.setPrevValue()
    @IsProperty(DemoAbstractEntity.class)
    private ArrayList<DemoAbstractEntity> list = new ArrayList<DemoAbstractEntity>();
    @IsProperty
    private DemoAbstractEntity demoEntity;
    @IsProperty
    private DateTime dateTime;
    @IsProperty
    //    @Required
    private Date date = DateUtils.startOfDay(new Date());//new Date();
    // NOTE : the initial value have to be not null, to correctly handle MetaProperty.setPrevValue()
    @IsProperty(String.class)
    private ArrayList<String> stringList = new ArrayList<String>();
    @IsProperty
    private String stringDemoEntity;
    @IsProperty(Bicycle.class)
    private List<Bicycle> bicycles = new ArrayList<Bicycle>();

    /**
     * Enum for radioButton property Strategy
     *
     * @author jhou
     *
     */
    public enum Strategy {
        COMMIT("commit"), REVERT("revert"), REVERT_ON_INVALID("revert on invalid"), COMMIT_ON_VALID("commit on valid");

        @Override
        public String toString() {
            return this.s;
        }

        private String s;

        Strategy(final String s) {
            this.s = s;
        }

    }

    /**
     * PropertyNames - use it always for getting simple refactoring
     */
    public final static String PROPERTY_STRING = "string";
    public final static String PROPERTY_PASSWORD = "password";
    public final static String PROPERTY_NUMBER = "number";
    public final static String PROPERTY_BIG_DECIMAL = "bigDecimal";
    public final static String PROPERTY_DOUBLE = "doubleProperty";
    public final static String PROPERTY_MONEY = "money";
    public final static String PROPERTY_BOOL = "bool";
    public final static String PROPERTY_STRATEGY = "strategy";
    public final static String PROPERTY_LIST = "list";
    public final static String PROPERTY_DEMO_ENTITY = "demoEntity";
    public final static String PROPERTY_DATE_TIME = "dateTime";
    public final static String PROPERTY_DATE = "date";
    public final static String PROPERTY_LIST_OF_STRINGS = "stringList";
    public final static String PROPERTY_STRING_DEMO_ENTITY = "stringDemoEntity";
    public final static String PROPERTY_BICYCLES = "bicycles";

    protected Entity() {
    }

    // Getters and setters. Can be non-javaBean-conventional, setters HAVE TO BE @Observable
    public String getString() {
        return string;
    }

    @Observable
    @DomainValidation
    public Entity setString(final String string) {
        this.string = string;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setString: " + string);
        return this;
    }

    public String getPassword() {
        return password;
    }

    @Observable
    public Entity setPassword(final String password) {
        this.password = password;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setPassword: " + password);
        return this;
    }

    public Integer getNumber() {
        return number;
    }

    @Observable
    //        @NotNull
    //    @GreaterOrEqual(50)
    //    @Max(200)
    @DomainValidation
    public Entity setNumber(final Integer number) throws Result {
        if (number != null && number > 1000) {
            throw new Result(this, new Exception("The value " + number + " is > 1000."));
        }
        this.number = number;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setNumber: " + number);
        if (new Integer(777).equals(number)) {
            throw new Warning(this, "Dynamic validation warning : The value " + number + " is dangerous.");
        }
        return this;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    @Observable
    @GreaterOrEqual(-100)
    @Max(2000)
    @DomainValidation
    public Entity setBigDecimal(final BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setBigDecimal: " + bigDecimal);
        return this;
    }

    public Double getDoubleProperty() {
        return doubleProperty;
    }

    @Observable
    @GreaterOrEqual(-100)
    @Max(2000)
    @DomainValidation
    public Entity setDoubleProperty(final Double doubleProperty) {
        this.doubleProperty = doubleProperty;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setDoubleProperty: " + doubleProperty);
        return this;
    }

    public Money getMoney() {
        return money;
    }

    @Observable
    @GreaterOrEqual(-100)
    @Max(2000)
    @DomainValidation
    public Entity setMoney(final Money money) {
        this.money = money;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setMoney: " + money);
        return this;
    }

    public Boolean getBool() {
        return bool;
    }

    @Observable
    @DomainValidation
    public Entity setBool(final Boolean bool) {
        this.bool = bool;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setBool: " + bool);
        return this;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    @Observable
    @DomainValidation
    public Entity setStrategy(final Strategy strategy) {
        this.strategy = strategy;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setStrategy: " + strategy);
        return this;
    }

    public ArrayList<DemoAbstractEntity> getList() {
        return list;
    }

    @Observable
    @DomainValidation
    // IMPORTANT : EntityExists annotation should be defined! to get appropriate autocompleter behaviour
    //@EntityExists(entityType = DemoEntity.class, keyType = String.class)
    public Entity setList(final ArrayList<DemoAbstractEntity> list) throws Result {
        //list.get(10).set("fgyegf", null); // throws unhandled exception
        if (list.size() == 3) {
            throw new Result(this, new Exception("you cannot set the list with 3 elements:)"));
        }
        this.list = list;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setList: " + list);
        return this;
    }

    public DemoAbstractEntity getDemoEntity() {
        return demoEntity;
    }

    @Observable
    @DomainValidation
    // IMPORTANT : EntityExists annotation should be defined! to get appropriate autocompleter behaviour
    //@EntityExists(entityType = DemoEntity.class, keyType = String.class)
    @EntityExists(DemoAbstractEntity.class)
    public Entity setDemoEntity(final DemoAbstractEntity demoEntity) {
        this.demoEntity = demoEntity;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setDemoEntity : " + demoEntity);
        return this;
    }

    public List<Bicycle> getBicycles() {
        return bicycles;
    }

    @Observable
    @DomainValidation
    // IMPORTANT : EntityExists annotation should be defined! to get appropriate autocompleter behaviour
    //@EntityExists(entityType = DemoEntity.class, keyType = String.class)
    public Entity setBicycles(final List<Bicycle> bicycles) {
        this.bicycles.clear();
        this.bicycles.addAll(bicycles);
        System.out.print("entity = " + getString() + " ");
        System.out.println("setBicycles : " + this.bicycles);
        return this;
    }

    @Observable
    public Entity addToBicycles(final Bicycle value) {
        bicycles.add(value);
        return this;
    }

    @Observable
    @DomainValidation
    public Entity removeFromBicycles(final Bicycle value) {
        bicycles.remove(value);
        return this;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    @Observable
    public Entity setDateTime(final DateTime dateTime) {
        this.dateTime = dateTime;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setDateTime : " + dateTime);
        return this;
    }

    public Date getDate() {
        return date;
    }

    public Long getDateMilliSeconds() {
        return date.getTime();
    }

    @Observable
    @DomainValidation
    public Entity setDate(final Date date) {
        this.date = date;
        System.err.print("entity = " + getString() + " ");
        System.err.println("setDate : " + date);
        return this;
    }

    public ArrayList<String> getStringList() {
        return stringList;
    }

    @Observable
    // need not any validation! because cannot set null, only can set empty list!
    public Entity setStringList(final ArrayList<String> stringList) {
        this.stringList = stringList;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setStringList : " + stringList);
        return this;
    }

    public String getStringDemoEntity() {
        return stringDemoEntity;
    }

    @Observable
    //@EntityExists(entityType = DemoAbstractEntity.class, keyType = String.class)
    public Entity setStringDemoEntity(final String stringDemoEntity) {
        this.stringDemoEntity = stringDemoEntity;
        System.out.print("entity = " + getString() + " ");
        System.out.println("setStringDemoEntity : " + stringDemoEntity);
        return this;
    }
}