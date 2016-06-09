package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.User;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgSubSystem.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgSubSystem extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title("Category")
    private TgCategory firstCategory;

    @IsProperty
    @MapTo
    @Title(value = "Second Category", desc = "Desc")
    private TgCategory secondCategory;


    @IsProperty(assignBeforeSave = true)
    @MapTo
    @Title(value = "User", desc = "Desc")
    @Required
    private User user;

    @IsProperty(assignBeforeSave = true)
    @MapTo
    @Title(value = "Explanation", desc = "Desc")
    private String explanation;

    @Observable
    public TgSubSystem setExplanation(final String explanation) {
        this.explanation = explanation;
        return this;
    }

    public String getExplanation() {
        return explanation;
    }




    @Observable
    public TgSubSystem setUser(final User user) {
        this.user = user;
        return this;
    }

    public User getUser() {
        return user;
    }

    @Observable
    public TgSubSystem setSecondCategory(final TgCategory secondCategory) {
        this.secondCategory = secondCategory;
        return this;
    }

    public TgCategory getSecondCategory() {
        return secondCategory;
    }

    @Observable
    public TgSubSystem setFirstCategory(final TgCategory category) {
        this.firstCategory = category;
        return this;
    }

    public TgCategory getFirstCategory() {
        return firstCategory;
    }

}