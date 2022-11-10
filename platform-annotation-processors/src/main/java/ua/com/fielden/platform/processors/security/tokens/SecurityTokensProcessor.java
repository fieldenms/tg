package ua.com.fielden.platform.processors.security.tokens;

import static java.util.stream.Collectors.joining;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

import com.google.common.base.Stopwatch;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.attachment.AttachmentDownload_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanRead_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanSave_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.AttachmentMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.DashboardRefreshFrequencyMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserRoleMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequencyUnit_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequencyUnit_CanRead_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanRead_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanSave_Token;
import ua.com.fielden.platform.security.tokens.persistent.KeyNumber_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.KeyNumber_CanRead_Token;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserRoleTokensUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRolesUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanSave_Token;
import ua.com.fielden.platform.security.tokens.web_api.GraphiQL_CanExecute_Token;
import ua.com.fielden.platform.utils.StreamUtils;

/**
 * Annotation processor that builds and generates a hierarchy of security token nodes.
 * 
 * @author TG Team
 */
@SupportedAnnotationTypes("*")
public class SecurityTokensProcessor extends AbstractProcessor {
    public static final Set<Class<? extends ISecurityToken>> PLATFORM_TOKENS;
    private static final String GENERATED_PKG_NAME = "tokens";
    private static final String GENERATED_CLASS_SIMPLE_NAME = "TokensHierarchy";
    private static final String GENERATED_CLASS_NAME = "%s.%s".formatted(GENERATED_PKG_NAME, GENERATED_CLASS_SIMPLE_NAME);

    static {
        PLATFORM_TOKENS = Set.of(
                User_CanSave_Token.class,
                User_CanRead_Token.class,
                User_CanReadModel_Token.class,
                User_CanDelete_Token.class,
                UserMaster_CanOpen_Token.class,
                UserRole_CanSave_Token.class,
                UserRole_CanRead_Token.class,
                UserRole_CanReadModel_Token.class,
                UserRole_CanDelete_Token.class,
                UserRoleMaster_CanOpen_Token.class,
                UserAndRoleAssociation_CanRead_Token.class,
                UserAndRoleAssociation_CanReadModel_Token.class,
                UserRolesUpdater_CanExecute_Token.class,
                UserRoleTokensUpdater_CanExecute_Token.class,
                Attachment_CanSave_Token.class,
                Attachment_CanRead_Token.class,
                Attachment_CanReadModel_Token.class,
                Attachment_CanDelete_Token.class,
                AttachmentMaster_CanOpen_Token.class,
                AttachmentDownload_CanExecute_Token.class,
                DashboardRefreshFrequencyUnit_CanRead_Token.class,
                DashboardRefreshFrequencyUnit_CanReadModel_Token.class,
                DashboardRefreshFrequency_CanSave_Token.class,
                DashboardRefreshFrequency_CanRead_Token.class,
                DashboardRefreshFrequency_CanReadModel_Token.class,
                DashboardRefreshFrequency_CanDelete_Token.class,
                DashboardRefreshFrequencyMaster_CanOpen_Token.class,
                DomainExplorer_CanRead_Token.class,
                DomainExplorer_CanReadModel_Token.class,
                KeyNumber_CanRead_Token.class,
                KeyNumber_CanReadModel_Token.class,
                GraphiQL_CanExecute_Token.class);
    }

    private final String classSimpleName = this.getClass().getSimpleName();

    private Filer filer;
    private Elements elementUtils;
    private Messager messager;
    private Map<String, String> options;

    private SecurityTokenFinder tokenFinder;
    private int roundNumber;
    private final Set<TypeElement> collectedTokens = new HashSet<>();
    private boolean aborted = false;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.options = processingEnv.getOptions();

        this.roundNumber = 0;
        this.tokenFinder = new SecurityTokenFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());

        messager.printMessage(Kind.NOTE, "%s initialized.".formatted(classSimpleName));
        if (!this.options.isEmpty()) {
            messager.printMessage(Kind.NOTE, "Options: [%s]".formatted(
                    options.keySet().stream().map(k -> "%s=%s".formatted(k, options.get(k))).sorted().collect(joining(", "))));
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        roundNumber = roundNumber + 1;
        final Stopwatch stopwatchProcess = Stopwatch.createStarted();

        messager.printMessage(Kind.NOTE, ">>> %s: PROCESSING ROUND %d START >>>".formatted(classSimpleName, roundNumber));
        if (aborted) {
            messager.printMessage(Kind.NOTE, "Aborted in a prior round, skipping.");
        }
        else {
            messager.printMessage(Kind.NOTE, "annotations: [%s]".formatted(
                    annotations.stream().map(el -> el.getSimpleName().toString()).sorted().collect(joining(", "))));
            final Set<? extends Element> rootElements = roundEnv.getRootElements();
            messager.printMessage(Kind.NOTE, "rootElements: [%s]".formatted(
                    rootElements.stream().map(el -> el.getSimpleName().toString()).sorted().collect(joining(", "))));

            try {
                doProcess(annotations, roundEnv);
            }
            // we want to catch both checked and unchecked exceptions to record the fact that it was thrown 
            // and be able to skip subsequent rounds
            catch (final Exception e) {
                aborted = true;
                messager.printMessage(Kind.ERROR, "Exception was thrown, aborting.");
                messager.printMessage(Kind.ERROR, "%s\n%s".formatted(e.toString(),
                        Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"))));
            }
        }

        stopwatchProcess.stop();
        messager.printMessage(Kind.NOTE, "<<< %s: PROCESSING ROUND %d END [%s millis] <<<".formatted(classSimpleName, roundNumber,
                stopwatchProcess.elapsed(TimeUnit.MILLISECONDS)));

        return false;
    }

    private void doProcess(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) throws Exception {
        collectedTokens.addAll(collectTokens(roundEnv.getRootElements()));
        if (roundEnv.processingOver()) {
            if (shouldGenerate()) {
                generate();
            }
            else {
                messager.printMessage(Kind.NOTE, "No generation is required.");
            }
        }
    }

    private boolean shouldGenerate() {
        // don't generate if no tokens were collected and generated class already exists
        return !(collectedTokens.isEmpty() && elementUtils.getTypeElement(GENERATED_CLASS_NAME) != null);
    }

    private Set<TypeElement> collectTokens(final Set<? extends Element> rootElements) throws Exception {
        return rootElements.stream()
                .filter(el -> el.asType().getKind().equals(TypeKind.DECLARED))
                .map(el -> (TypeElement) el)
                .filter(el -> tokenFinder.isSecurityToken(el.asType()))
                .collect(Collectors.toSet());
    }
    }

    private void generate() throws Exception {
    }

}