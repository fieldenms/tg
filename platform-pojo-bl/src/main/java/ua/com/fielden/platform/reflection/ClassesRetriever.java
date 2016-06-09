package ua.com.fielden.platform.reflection;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.Pair;

/**
 * This is a helper class to retrieve classes for packages/jars etc.
 *
 * @author TG Team
 *
 */
public class ClassesRetriever {
    /**
     * Let's hide default constructor, which is not needed for a static class.
     */
    private ClassesRetriever() {
    }

    /**
     * This interface provides method that can be used to filter out classes. It was implemented only for methods those returns classes from package and also must satisfies some
     * additional condition implemented in the isSatisfies method.
     *
     * @author oleh
     *
     */
    public interface IFilterClass {

        /**
         * Must return true if the given testClass satisfies implemented condition otherwise returns false
         *
         * @param testClass
         * @return
         */
        boolean isSatisfies(Class<?> testClass);
    }

    /**
     * Returns all classes in the specified package that is located on the path. The path might be a directory or *.jar archive according to condition specified by the filter
     * instance.
     *
     * @param path
     * @param packageName
     * @param filter
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws Exception
     */
    public static List<Class<?>> getClassesInPackage(final String path, final String packageName, final IFilterClass filter) throws Exception {
        final SortedSet<Class<?>> classes = new TreeSet<Class<?>>(new Comparator<Class>() {

            @Override
            public int compare(final Class o1, final Class o2) {
                return o1.getName().compareTo(o2.getName());
            }

        });
        final String packagePath = packageName.replace('.', '/');
        addPath(path.replace("%20", " "));
        if (path.indexOf(".jar") > 0) {
            classes.addAll(getFromJarFile(path.replace("%20", " "), packagePath, filter));
        } else {
            String filePath = path + "/" + packagePath;
            // WINDOWS HACK
            filePath = filePath.replace("%20", " ");
            classes.addAll(getFromDirectory(new File(filePath), packageName, filter));
        }
        return new ArrayList<Class<?>>(classes);
    }

    /**
     * Determines weather <code>derivedClass</code> is derived from <code>superClass</code>.
     *
     * @param derivedClass
     * @param superClass
     * @return
     */
    public static boolean isClassDerivedFrom(final Class<?> derivedClass, final Class<?> superClass) {
        return !derivedClass.equals(superClass) ? superClass.isAssignableFrom(derivedClass) : false;
    }

    /**
     * Searches for all classes defined in the provided package and located in the directory or archive specified with path.
     *
     * @param path
     * @param packageName
     * @return
     * @throws Exception
     */
    public static List<Class<?>> getAllClassesInPackage(final String path, final String packageName) throws Exception {
        return getClassesInPackage(path, packageName, null);
    }

    /**
     * returns all classes from the package that is located in the directory or archive specified with path and annotated with specified annotation
     *
     * @param path
     * @param packageName
     * @param annotation
     * @return
     * @throws Exception
     */
    public static List<Class<?>> getAllClassesInPackageAnnotatedWith(final String path, final String packageName, final Class<? extends Annotation> annotation) throws Exception {
        return getClassesInPackage(path, packageName, new IFilterClass() {

            @Override
            public boolean isSatisfies(final Class<?> testClass) {
                return AnnotationReflector.isAnnotationPresentForClass(annotation, testClass);
            }

        });
    }

    /**
     * Searches for all classes defined in the provided package from the directory or archive specified with path, which are derived from a specified superclass.
     *
     * @param path
     * @param packageName
     * @param superClass
     * @return
     * @throws Exception
     */
    public static List<Class<?>> getAllClassesInPackageDerivedFrom(final String path, final String packageName, final Class<?> superClass) {
        try {
            return getClassesInPackage(path, packageName, new IFilterClass() {

                @Override
                public boolean isSatisfies(final Class<?> testClass) {
                    return isClassDerivedFrom(testClass, superClass);
                }

            });
        } catch (final Exception ex) {
            throw new ReflectionException(format("Could not get classes on pathe [%s] in package [%s].", path, packageName), ex);
        }
    }

    /**
     * Searches for all non-abstract classes defined in the provided package from the directory or archive specified with path, which are derived from a specified superclass.
     *
     * @param path
     * @param packageName
     * @param superClass
     * @return
     * @throws Exception
     */
    public static List<Class<?>> getAllNonAbstractClassesInPackageDerivedFrom(final String path, final String packageName, final Class<?> superClass) throws Exception {
        return getClassesInPackage(path, packageName, new IFilterClass() {

            @Override
            public boolean isSatisfies(final Class<?> testClass) {
                final int modifiers = testClass.getModifiers();
                return !Modifier.isAbstract(modifiers) && isClassDerivedFrom(testClass, superClass);
            }

        });
    }

    /**
     * Returns classes in the package those are directly derived from the {@code superClass}
     *
     * @param path
     * @param packageName
     * @param superClass
     * @return
     * @throws Exception
     */
    public static List<Class<?>> getAllClassesInPackageDirectlyDerivedFrom(final String path, final String packageName, final Class<?> superClass) throws Exception {
        return getClassesInPackage(path, packageName, new IFilterClass() {

            @Override
            public boolean isSatisfies(final Class<?> testClass) {
                return !superClass.equals(testClass) ? superClass.equals(testClass.getSuperclass()) : false;
            }

        });
    }

    /**
     * Searches for all classes defined in the provided package, which have methods annotated with the specified annotation and located in the directory or archive specified with
     * path.
     *
     * @param path
     * @param packageName
     * @param annotation
     * @return
     * @throws Exception
     */
    public static List<Class<?>> getAllClassInPackageWithAnnotatedMethods(final String path, final String packageName, final Class<? extends Annotation> annotation)
            throws Exception {
        return getClassesInPackage(path, packageName, new IFilterClass() {

            @Override
            public boolean isSatisfies(final Class<?> testClass) {
                return AnnotationReflector.isClassHasMethodAnnotatedWith(testClass, annotation);
            }

        });
    }

    /**
     * Returns list of classes extended from the {@code superClass} and is not {@code abstract}
     *
     * @param path
     * @param packageName
     * @param superClass
     * @return
     * @throws Exception
     */
    public static List<Class<?>> getAllNonAbstractClassesDerivedFrom(final String path, final String packageName, final Class<?> superClass) throws Exception {
        return getClassesInPackage(path, packageName, new IFilterClass() {

            @Override
            public boolean isSatisfies(final Class<?> testClass) {
                return isClassDerivedFrom(testClass, superClass) && !Modifier.isAbstract(testClass.getModifiers()) && !testClass.getName().contains("$");
            }

        });
    }

    /**
     * Finds and loads class for the passed class name.
     *
     * @param className
     * @return
     */
    public static Class<?> findClass(final String className) {
        try {
            return ClassLoader.getSystemClassLoader().loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns all classes in the package and it's sub packages from the directory according to condition specified by the filter instance
     *
     * @param directory
     * @param packageName
     * @param filter
     * @return
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> getFromDirectory(final File directory, final String packageName, final IFilterClass filter) throws ClassNotFoundException {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        final List<Pair<File, String>> directories = new ArrayList<Pair<File, String>>();
        directories.add(new Pair<File, String>(directory, packageName));
        while (directories.size() > 0) {
            final Pair<File, String> nextDirectory = directories.get(0);
            if (nextDirectory.getKey().exists()) {
                for (final File file : nextDirectory.getKey().listFiles()) {
                    if (file.getName().endsWith(".class")) {
                        final String name = nextDirectory.getValue() + '.' + stripFilenameExtension(file.getName());
                        final Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(name);
                        if (filter != null && filter.isSatisfies(clazz)) {
                            classes.add(clazz);
                        } else if (filter == null) {
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

    /**
     * Utility method for checking string for existence of spaces. Value "%20" is needed to be checked for Windows paths.
     *
     * @param name
     * @return
     */
    private static boolean hasNoSpaces(final String name) {
        return name.indexOf(' ') < 0 && name.indexOf("%20") < 0;
    }

    /**
     * Returns all classes in the package and it's sub packages from the *.jar archive according to condition specified in the filter instance
     *
     * @param jar
     * @param packageName
     * @param filter
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static List<Class<?>> getFromJarFile(final String jar, final String packageName, final IFilterClass filter) throws ClassNotFoundException, IOException {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        try (final JarInputStream jarFile = new JarInputStream(new FileInputStream(jar))) {
            JarEntry jarEntry;
            do {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry != null) {
                    String className = jarEntry.getName();
                    if (className.endsWith(".class")) {
                        className = stripFilenameExtension(className);
                        if (className.startsWith(packageName + "/")) {
                            final Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className.replace('/', '.'));
                            if (filter != null && filter.isSatisfies(clazz)) {
                                classes.add(clazz);
                            } else if (filter == null) {
                                classes.add(clazz);
                            }

                        }
                    }
                }
            } while (jarEntry != null);
        }
        return classes;
    }

    /**
     * removes the extension of the file
     *
     * @param className
     * @return
     */
    private static String stripFilenameExtension(final String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }

    /**
     * Adds specified path to the class path. It works only if the system class loader is an instance of URLClassLoader.
     *
     * @param path
     * @throws Exception
     */
    private static void addPath(final String path) throws Exception {
        final File file = new File(path);
        final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class<URLClassLoader> urlClass = URLClassLoader.class;
        final Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[] { file.toURI().toURL() });
    }

}
