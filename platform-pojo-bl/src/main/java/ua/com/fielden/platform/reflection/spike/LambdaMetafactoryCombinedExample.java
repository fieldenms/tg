package ua.com.fielden.platform.reflection.spike;

import java.lang.invoke.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.lang.invoke.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

class Person {
    private String name;

    public void setName(String name) {
        System.out.println("setter is invoked");
        this.name = name;
    }

    public String getName() {
        System.out.println("getter is invoked");
        return name;
    }
}

public class LambdaMetafactoryCombinedExample {

    public static void main(String[] args) throws Throwable {
        // Step 1: Create MethodHandles.Lookup
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // Step 2: Find the setter method (setName)
        MethodHandle setter = lookup.findVirtual(Person.class, "setName",
                MethodType.methodType(void.class, String.class));

        // Step 3: Find the getter method (getName)
        MethodHandle getter = lookup.findVirtual(Person.class, "getName",
                MethodType.methodType(String.class));

        // Step 4: Create the setter lambda using LambdaMetafactory
        CallSite setterSite = LambdaMetafactory.metafactory(
                lookup,
                "accept",  // BiConsumer method (accept for setter)
                MethodType.methodType(BiConsumer.class),  // BiConsumer signature
                MethodType.methodType(void.class, Object.class, Object.class),  // Erased signature
                setter,  // The actual setter method handle
                MethodType.methodType(void.class, Person.class, String.class)  // Specific method signature
        );

        // Step 5: Create the getter lambda using LambdaMetafactory
        CallSite getterSite = LambdaMetafactory.metafactory(
                lookup,
                "apply",  // Function method (apply for getter)
                MethodType.methodType(Function.class),  // Function signature
                MethodType.methodType(Object.class, Object.class),  // Erased signature
                getter,  // The actual getter method handle
                MethodType.methodType(String.class, Person.class)  // Specific method signature
        );

        // Step 6: Get the target lambdas
        BiConsumer<Person, String> setNameLambda = (BiConsumer<Person, String>) setterSite.getTarget().invoke();
        Function<Person, String> getNameLambda = (Function<Person, String>) getterSite.getTarget().invoke();

        // Step 7: Test the combined lambdas
        Person person = new Person();

        // Use the setter lambda
        setNameLambda.accept(person, "John Doe");

        // Use the getter lambda
        String name = getNameLambda.apply(person);

        // Verify the result
        System.out.println("Person's name: " + name);  // Output: Person's name: John Doe
    }
}
