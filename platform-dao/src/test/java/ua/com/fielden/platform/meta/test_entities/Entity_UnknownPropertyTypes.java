package ua.com.fielden.platform.meta.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;

import java.util.List;
import java.util.Map;

@MapEntityTo
@KeyType(String.class)
public class Entity_UnknownPropertyTypes<T> extends AbstractEntity<String> {

    @IsProperty
    private Map<String, String> map;

    @IsProperty
    private List<T> listWithTypeVar;

    @IsProperty
    private List rawList;

    public List getRawList() {
        return rawList;
    }

    @Observable
    public Entity_UnknownPropertyTypes setRawList(final List rawList) {
        this.rawList = rawList;
        return this;
    }

    public List<T> getListWithTypeVar() {
        return listWithTypeVar;
    }

    @Observable
    public Entity_UnknownPropertyTypes setListWithTypeVar(final List<T> listWithTypeVar) {
        this.listWithTypeVar = listWithTypeVar;
        return this;
    }

    public Map<String, String> getMap() {
        return map;
    }

    @Observable
    public Entity_UnknownPropertyTypes setMap(final Map<String, String> map) {
        this.map = map;
        return this;
    }

}
