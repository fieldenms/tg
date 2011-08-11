package ua.com.fielden.platform.example.swing.components.autocompleter;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * This is a dummy implementation of interface Entity used in this demo.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Dynamic Entity", desc = "Dynamic entity used for demonstrations")
public class DemoEntity extends AbstractEntity<String>{
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Name", desc = "Property 'name'")
    private String name;

    @IsProperty
    @Title(value = "Another", desc = "Property 'anotherProperty'")
    private String anotherProperty;

    public DemoEntity(final String name, final String desc) {
        this.name = name;
        setDesc(desc);
    }

    public String getName() {
        return name;
    }

    @Observable
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "name: " + name;
    }

    public String getAnotherProperty() {
        return anotherProperty;
    }

    @Observable
    public void setAnotherProperty(final String anotherProperty) {
        this.anotherProperty = anotherProperty;
    }
}