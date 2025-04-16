package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.Date;

@KeyType(DynamicEntityKey.class)
@KeyTitle("Inventory Issue")
@CompanionObject(TgInventoryIssueCo.class)
@MapEntityTo
public class TgInventoryIssue extends ActivatableAbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgInventoryBin bin;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private Date issueDate;

    @IsProperty
    @MapTo
    private TgInventory supersededInventory;

    @IsProperty
    @MapTo
    private Integer qty;

    @Override
    @Observable
    public TgInventoryIssue setActive(boolean active) {
        super.setActive(active);
        return this;
    }

    public Integer getQty() {
        return qty;
    }

    @Observable
    public TgInventoryIssue setQty(final Integer qty) {
        this.qty = qty;
        return this;
    }

    @Observable
    public TgInventoryIssue setIssueDate(final Date issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public TgInventory getSupersededInventory() {
        return supersededInventory;
    }

    @Observable
    public TgInventoryIssue setSupersededInventory(final TgInventory supersededInventory) {
        this.supersededInventory = supersededInventory;
        return this;
    }

    @Observable
    public TgInventoryIssue setBin(final TgInventoryBin bin) {
        this.bin = bin;
        return this;
    }

    public TgInventoryBin getBin() {
        return bin;
    }

}
