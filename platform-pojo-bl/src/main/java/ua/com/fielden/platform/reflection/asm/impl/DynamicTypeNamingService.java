package ua.com.fielden.platform.reflection.asm.impl;

import java.util.concurrent.atomic.AtomicInteger;

public class DynamicTypeNamingService {
    public static final String APPENDIX = "$$TgEntity";
    private AtomicInteger nextNo = new AtomicInteger(0);

    private String enhancedName(final String name) {
	return name + APPENDIX + nextNo.incrementAndGet();
    }

    public final String nextTypeName(final String name) {
	final String baseName = name.contains(APPENDIX) ? name.substring(0, name.indexOf(APPENDIX)) : name;
	return enhancedName(baseName);
    }

}
