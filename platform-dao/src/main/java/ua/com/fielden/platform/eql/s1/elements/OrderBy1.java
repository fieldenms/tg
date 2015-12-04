package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.fluent.SortingOrderDirection;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;

public class OrderBy1 {
    private final ISingleOperand1<? extends ISingleOperand2> operand;
    private final String yieldName;
    private Yield1 yield;
    private final SortingOrderDirection sortingOrderDirection;

    @Override
    public String toString() {
        return (yieldName == null ? operand : yieldName) + " " + sortingOrderDirection;
    }

    public OrderBy1(final ISingleOperand1<? extends ISingleOperand2> operand, final SortingOrderDirection sortingOrderDirection) {
        super();
        this.operand = operand;
        this.yieldName = null;
        this.sortingOrderDirection = sortingOrderDirection;
    }

    public OrderBy1(final String yieldName, final SortingOrderDirection sortingOrderDirection) {
        super();
        this.operand = null;
        this.yieldName = yieldName;
        this.sortingOrderDirection = sortingOrderDirection;
    }

    public ISingleOperand1<? extends ISingleOperand2> getOperand() {
        return operand;
    }

    public String getYieldName() {
        return yieldName;
    }


    public Yield1 getYield() {
        return yield;
    }

    public void setYield(final Yield1 yield) {
        this.yield = yield;
    }

    public SortingOrderDirection getSortingOrderDirection() {
        return sortingOrderDirection;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        result = prime * result + ((sortingOrderDirection == null) ? 0 : sortingOrderDirection.hashCode());
        result = prime * result + ((yield == null) ? 0 : yield.hashCode());
        result = prime * result + ((yieldName == null) ? 0 : yieldName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OrderBy1)) {
            return false;
        }
        OrderBy1 other = (OrderBy1) obj;
        if (operand == null) {
            if (other.operand != null) {
                return false;
            }
        } else if (!operand.equals(other.operand)) {
            return false;
        }
        if (sortingOrderDirection != other.sortingOrderDirection) {
            return false;
        }
        if (yield == null) {
            if (other.yield != null) {
                return false;
            }
        } else if (!yield.equals(other.yield)) {
            return false;
        }
        if (yieldName == null) {
            if (other.yieldName != null) {
                return false;
            }
        } else if (!yieldName.equals(other.yieldName)) {
            return false;
        }
        return true;
    }
}