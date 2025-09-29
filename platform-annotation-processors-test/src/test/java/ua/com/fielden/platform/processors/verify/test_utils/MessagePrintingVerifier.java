package ua.com.fielden.platform.processors.verify.test_utils;

import ua.com.fielden.platform.processors.verify.ViolatingElement;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.ArrayList;
import java.util.List;

/**
 * A verifier that finds all elements annotated with {@link Message} and for each found element reports the specified message
 * of the specified kind. This verifier enables a convenient technique for making assertions about reported messages in tests.
 * To get started, simply annotate an input element with {@link Message} and then assert the existence of the reported message.
 *
 * @author TG Team
 */
public class MessagePrintingVerifier extends SimpleVerifier {

    public MessagePrintingVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected List<ViolatingElement> verify(final SimpleRoundEnvironment roundEnv) {
        final List<ViolatingElement> violators = new ArrayList<>();

        roundEnv.getElementsAnnotatedWith(Message.class).stream()
        .map(elt -> {
            final Message annot = elt.getAnnotation(Message.class);
            return new ViolatingElement(elt, annot.kind(), annot.value());
        })
        .forEach(ve -> {
            ve.printMessage(messager);
            violators.add(ve);
        });

        return violators;
    }

}
