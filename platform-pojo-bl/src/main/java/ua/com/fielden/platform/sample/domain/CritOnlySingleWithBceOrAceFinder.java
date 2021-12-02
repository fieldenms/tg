package ua.com.fielden.platform.sample.domain;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isCritOnlySingle;
import static ua.com.fielden.platform.reflection.Finder.streamProperties;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

public class CritOnlySingleWithBceOrAceFinder {
    
    public static void main(final String[] args) {
        final Map<Class<? extends AbstractEntity<?>>, Set<String>> found = ApplicationDomain.domainTypes().stream()
            .filter(type -> critOnlySingleWithBceOrAce(type).size() > 0)
            .collect(toMap(type -> type, type -> critOnlySingleWithBceOrAce(type)));
        System.out.println("found = " + found);
    }
    
    private static Set<String> critOnlySingleWithBceOrAce(final Class<?> type) {
        final Set<String> props = new HashSet<>();
        props.addAll(streamProperties(type, CritOnly.class, BeforeChange.class).map(field -> field.getName()).filter(prop -> isCritOnlySingle(type, prop)).collect(toSet()));
        props.addAll(streamProperties(type, CritOnly.class, AfterChange.class).map(field -> field.getName()).filter(prop -> isCritOnlySingle(type, prop)).collect(toSet()));
        return props;
    }
    
}
