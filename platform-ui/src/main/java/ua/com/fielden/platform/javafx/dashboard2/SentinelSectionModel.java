package ua.com.fielden.platform.javafx.dashboard2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.types.Money;

public class SentinelSectionModel {
    // private boolean isLighting = false;
    private BigInteger count = new BigInteger("0");
    private Money money = new Money(new BigDecimal(0.0));
    private BigDecimal decimal = new BigDecimal(0.0);
    private final List<Runnable> afterChangeActions = new ArrayList<>();
    private final String desc;

    public SentinelSectionModel(final String desc) {
	this.desc = desc;
    }

    public boolean isLighting() {
	return count.longValue() > 0; // TODO narrowing conversion!
    }

    public void addAfterChangeAction(final Runnable action) {
	afterChangeActions.add(action);
    }

    public SentinelSectionModel setCountAndMoneyAndDecimal(final BigInteger count, final Money money, final BigDecimal decimal) {
	this.count = count;
	this.money = money;
	this.decimal = decimal;

	for (final Runnable afterChange : afterChangeActions) {
	    afterChange.run();
	}
	return this;
    }

    public BigInteger getCount() {
	return count;
    }

    public Money getMoney() {
	return money;
    }

    public String getDesc() {
	return desc;
    }

    public BigDecimal getDecimal() {
	return decimal;
    }
}
