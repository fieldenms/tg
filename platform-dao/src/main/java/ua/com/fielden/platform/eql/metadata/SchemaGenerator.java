package ua.com.fielden.platform.eql.metadata;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.type.TypeResolver;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.reflection.Finder;

public class SchemaGenerator {
    private final List<Class<? extends AbstractEntity<?>>> entityTypes;
    private final TypeResolver typeResolver = new TypeResolver();

    public SchemaGenerator(List<Class<? extends AbstractEntity<?>>> entityTypes) {
        this.entityTypes = entityTypes;
    }

    private int resolveToSqlType(Class javaType) {
        // TODO take into account hibtype name from @PersistedType value. 
        if (isPersistedEntityType(javaType)) {
            return 4;
        }

        return typeResolver.heuristicType(javaType.getName()).sqlTypes(null)[0];
    }

    private MapTo columnFromProperty(Class<? extends AbstractEntity<?>> entityType, String propName) {
        return  getPropertyAnnotation(MapTo.class, entityType, propName);
    }

    public List<PersistedEntity> generate() {
        List<PersistedEntity> result = new ArrayList<>();

        for (Class<? extends AbstractEntity<?>> entityType : entityTypes) {
            if (isPersistedEntityType(entityType)) {
                PersistedEntity entity = new PersistedEntity(entityType.getName(), getAnnotation(entityType, MapEntityTo.class).value());
                result.add(entity);

                entity.getFinalProps().add(new FinalProperty(false, ID, "_ID", Long.class, resolveToSqlType(Long.class), 0, 0, 0, null));
                entity.getFinalProps().add(new FinalProperty(false, VERSION, "_VERSION", Long.class, resolveToSqlType(Long.class), 0, 0, 0, null));
                // TODO need to determine DESC property from annotations
                Class<? extends Comparable> keyType = getKeyType(entityType);
                if (!DynamicEntityKey.class.equals(keyType)) {
                    // TODO need to take into account the case of key column override
                    MapTo mt = columnFromProperty(entityType, KEY);
                    entity.getFinalProps().add(new FinalProperty(false, KEY, "KEY_", keyType, resolveToSqlType(keyType), mt.length(), mt.scale(), mt.precision(), mt.defaultValue()));
                }
                for (Field propField : Finder.findRealProperties(entityType, MapTo.class)) {
                    if (!propField.getName().equals("key")) {
                        boolean special = !(isPersistedEntityType(propField.getType()) ||
                                Long.class.equals(propField.getType()) ||
                                Date.class.equals(propField.getType()) ||
                                BigDecimal.class.equals(propField.getType()) ||
                                String.class.equals(propField.getType()) ||
                                Integer.class.equals(propField.getType()) ||
                                int.class.equals(propField.getType()) ||
                                boolean.class.equals(propField.getType())
                                );
                        if (!special) {
                            MapTo mt = columnFromProperty(entityType, propField.getName());
                            entity.getFinalProps().add(new FinalProperty(false, propField.getName(), mt.value(), propField.getType(), resolveToSqlType(propField.getType()), mt.length(), mt.scale(), mt.precision(), mt.defaultValue()));
                        }
                    }
                }
            }
        }
        return result;
    }
}
