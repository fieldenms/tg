package ua.com.fielden.platform.processors.test_utils;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/// A processor that generates an arbitrary source file in the 1st round.
/// The effect of such generation is the forcing of the 3rd round (i.e., the 2nd round will not be the last one.)
/// This is useful in cases where a processor under test regenerates some source in the 2nd round.
/// If the 2nd round were to be the last, and the previous version of the regenerated source contained errors
/// (e.g., unresolved references due to deletion of a referenced class), the compilation would fail, disregarding
/// the new, regenerated version.
///
public final class GeneratorProcessor extends AbstractPlatformAnnotationProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of("*");
    }

    @Override
    protected boolean processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (getRoundNumber() == 1) {
            generateRandomSource(filer);
        }
        return false;
    }

    private static void generateRandomSource(final Filer filer) {
        try {
            final var random = new Random();
            final var randomStr = Stream.generate(() -> random.nextInt(10)).limit(10).map(Object::toString).collect(joining());
            JavaFile.builder("test", TypeSpec.classBuilder("Generated%s".formatted(randomStr)).build())
                    .build()
                    .writeTo(filer);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
