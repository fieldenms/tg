package ua.com.fielden.platform.reflection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ua.com.fielden.platform.classloader.SecurityTokenClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static java.util.stream.Collectors.toList;

/// This is a helper class to retrieve classes for packages/jars etc.
///
public class ClassesRetriever {

    public static final String
            ERR_GETTING_CLASSES_ON_PATH = "Could not get classes on path [%s] in package [%s].",
            ERR_LOADING_CLASS = "Failed to load class [%s].";

    private static final SecurityTokenClassLoader URL_CLASS_LOADER = new SecurityTokenClassLoader(ClassLoader.getSystemClassLoader());
    private static final Cache<String, Class<?>> ADHOC_CLASSES = CacheBuilder.newBuilder().weakValues().build();

    /// Let's hide default constructor, which is not needed for a static class.
    ///
    private ClassesRetriever() {
    }

    /// Registers a class so that it can be found with [#findClass(String)].
    /// This enables dynamically generated classes to be located by name (such classes often have an anonymous class loader,
    /// which cannot be directly used to find them via [#loadClass(String)]).
    ///
    public static void registerClass(final Class<?> klass) {
        ADHOC_CLASSES.put(klass.getCanonicalName(), klass);
    }

    /// Returns all classes in the specified package that is located on the path.
    /// The path might be a directory or a `*.jar` archive according to conditions specified by `typePredicate`.
    ///
    /// @param typePredicate used to include only the types in the result, which satisfy the predicate.
    ///
    public static List<Class<?>> getClassesInPackage(final String path, final String packageName, final Predicate<Class<?>> typePredicate) throws Exception {
        final SortedSet<Class<?>> classes = new TreeSet<>((o1, o2) -> o1.getName().compareTo(o2.getName()));
        final String packagePath = packageName.replace('.', '/');
        addPath(path.replace("%20", " "));
        if (path.indexOf(".jar") > 0) {
            classes.addAll(getFromJarFile(path.replace("%20", " "), packagePath, typePredicate));
        } else {
            String filePath = path + "/" + packagePath;
            // WINDOWS HACK
            filePath = filePath.replace("%20", " ");
            classes.addAll(getFromDirectory(new File(filePath), packageName, typePredicate));
        }
        return new ArrayList<>(classes);
    }

    /// Determines weather `derivedClass` is derived from `superClass`.
    ///
    public static boolean isClassDerivedFrom(final Class<?> derivedClass, final Class<?> superClass) {
        return !derivedClass.equals(superClass) && superClass.isAssignableFrom(derivedClass);
    }

    /// Searches for all classes defined in the provided package and located in the directory or archive specified with path.
    ///
    public static List<Class<?>> getAllClassesInPackage(final String path, final String packageName) throws Exception {
        return getClassesInPackage(path, packageName, null);
    }

    /// Returns all classes from the package that is located in the directory or archive specified with path and annotated with specified annotation
    ///
    public static List<Class<?>> getAllClassesInPackageAnnotatedWith(final String path, final String packageName, final Class<? extends Annotation> annotation) throws Exception {
        return getClassesInPackage(path, packageName, (testClass) -> AnnotationReflector.isAnnotationPresentForClass(annotation, testClass));
    }

    /// Searches for all classes defined in the provided package from the directory or archive specified with path, which are derived from a specified superclass.
    ///
    public static <T> List<Class<? extends T>> getAllClassesInPackageDerivedFrom(final String path, final String packageName, final Class<T> superClass) {
        try {
            return getClassesInPackage(path, packageName, (testClass) -> isClassDerivedFrom(testClass, superClass)).stream()
                    .map(type -> (Class<? extends T>) type).collect(toList());
        } catch (final Exception ex) {
            throw new ReflectionException(ERR_GETTING_CLASSES_ON_PATH.formatted(path, packageName), ex);
        }
    }

    /// Searches for all non-abstract classes defined in the provided package from the directory or archive specified with path, which are derived from a specified superclass.
    ///
    public static <T> List<Class<? extends T>> getAllNonAbstractClassesInPackageDerivedFrom(final String path, final String packageName, final Class<T> superClass) throws Exception {
        return getClassesInPackage(path, packageName, testClass -> !Modifier.isAbstract(testClass.getModifiers()) && isClassDerivedFrom(testClass, superClass)).stream()
                .map(type -> (Class<? extends T>) type).collect(toList());
    }

    /// Returns classes in the package those are directly derived from the `superClass`
    ///
    public static List<Class<?>> getAllClassesInPackageDirectlyDerivedFrom(final String path, final String packageName, final Class<?> superClass) throws Exception {
        return getClassesInPackage(path, packageName, testClass -> !superClass.equals(testClass) ? superClass.equals(testClass.getSuperclass()) : false);
    }

    /// Searches for all classes defined in the provided package, which have methods annotated with the specified annotation and located in the directory or archive specified with
    /// path.
    ///
    public static List<Class<?>> getAllClassInPackageWithAnnotatedMethods(final String path, final String packageName, final Class<? extends Annotation> annotation) throws Exception {
        return getClassesInPackage(path, packageName, testClass -> AnnotationReflector.isClassHasMethodAnnotatedWith(testClass, annotation));
    }

    /// Returns list of classes extended from the `superClass` and is not `abstract`
    ///
    public static List<Class<?>> getAllNonAbstractClassesDerivedFrom(final String path, final String packageName, final Class<?> superClass) throws Exception {
        return getClassesInPackage(path, packageName, testClass -> isClassDerivedFrom(testClass, superClass) && !Modifier.isAbstract(testClass.getModifiers()) && !testClass.getName().contains("$"));
    }

    /// Finds and loads class for with `className`.
    ///
    public static Class<?> findClass(final String className) {
        try {
            return DynamicEntityClassLoader.loadType(className);
        } catch (final ClassNotFoundException ex) {
            try {
                return URL_CLASS_LOADER.loadClass(className);
            } catch (final ClassNotFoundException e) {
                final Class<?> klass = ADHOC_CLASSES.getIfPresent(className);
                if (klass != null) {
                    return klass;
                }
                throw new ReflectionException(ERR_LOADING_CLASS.formatted(className), e);
            }
        }
    }

    /// Returns all classes in the package and its sub-packages from the directory according to condition specified by `typePredicate`.
    ///
    private static List<Class<?>> getFromDirectory(final File directory, final String packageName, final Predicate<Class<?>> typePredicate) throws ClassNotFoundException {
        final List<Class<?>> classes = new ArrayList<>();
        final List<Pair<File, String>> directories = new ArrayList<>();
        directories.add(new Pair<File, String>(directory, packageName));
        while (!directories.isEmpty()) {
            final Pair<File, String> nextDirectory = directories.getFirst();
            if (nextDirectory.getKey().exists()) {
                for (final File file : nextDirectory.getKey().listFiles()) {
                    if (file.getName().endsWith(".class")) {
                        final String name = nextDirectory.getValue() + '.' + stripFilenameExtension(file.getName());
                        final Class<?> clazz = URL_CLASS_LOADER.loadClass(name);
                        if (typePredicate == null || typePredicate.test(clazz)) {
                            classes.add(clazz);
                        }
                    } else if (file.isDirectory() && hasNoSpaces(file.getName())) {
                        directories.add(new Pair<File, String>(file, nextDirectory.getValue() + '.' + file.getName()));
                    }
                }
            }
            directories.remove(nextDirectory);
        }
        return classes;
    }

    /// Utility method for checking string for existence of spaces. Value "%20" is needed to be checked for Windows paths.
    ///
    private static boolean hasNoSpaces(final String name) {
        return name.indexOf(' ') < 0 && name.indexOf("%20") < 0;
    }

    /// Returns all classes in the package and its sub-packages from the `*.jar` archive according to condition specified by `typePredicate`.
    ///
    private static List<Class<?>> getFromJarFile(final String jar, final String packageName, final Predicate<Class<?>> typePredicate) throws ClassNotFoundException, IOException {
        final List<Class<?>> classes = new ArrayList<>();
        try (final JarInputStream jarFile = new JarInputStream(new FileInputStream(jar))) {
            JarEntry jarEntry;
            do {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry != null) {
                    String className = jarEntry.getName();
                    if (className.endsWith(".class")) {
                        className = stripFilenameExtension(className);
                        if (className.startsWith(packageName + "/")) {
                            final Class<?> clazz = URL_CLASS_LOADER.loadClass(className.replace('/', '.'));
                            if (typePredicate != null && typePredicate.test(clazz)) {
                                classes.add(clazz);
                            } else if (typePredicate == null) {
                                classes.add(clazz);
                            }

                        }
                    }
                }
            } while (jarEntry != null);
        }
        return classes;
    }

    /// Removes the extension of the file.
    ///
    private static String stripFilenameExtension(final String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }
    
    /// Adds specified path to the class path.
    ///
    private static void addPath(final String path) throws Exception {
        final File file = new File(path);
        URL_CLASS_LOADER.addURL(file.toURI().toURL());
    }

}
