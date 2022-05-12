package ua.com.fielden.platform.processors.meta_model.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.Unique;

@KeyType(String.class)
public class Person extends ActivatableAbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "Name", desc = "The name of this person.")
    private String name;
    
    @IsProperty
    @MapTo
    @Title(value = "Age", desc = "The age of this person.")
    private Integer age;
    
    @IsProperty
    @MapTo
    @Title(value = "Vehicle", desc = "A vehicle belonging to this person.")
    private Vehicle vehicle;
    
    @IsProperty
    @MapTo
    @Title(value = "House", desc = "A house belonging to this person.")
    private House house;
    
    @IsProperty
    @MapTo
    @Title(value = "Active", desc = "Person active")
    @Unique
    private boolean active;

    @Observable
    public Person setActive(final boolean active) {
        this.active = active;
        return this;
    }

    public boolean getActive() {
        return active;
    }

    


    @Observable
    public Person setHouse(final House house) {
        this.house = house;
        return this;
    }

    public House getHouse() {
        return house;
    }

    @Observable
    public Person setVehicle(final Vehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    @Observable
    public Person setAge(final Integer age) {
        this.age = age;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    @Observable
    public Person setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }
}
