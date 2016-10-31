package ua.com.fielden.platform.data.generator.skipe_demo;

import java.util.Map;

import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.error.Result;

public class DemoGenerator implements IGenerator<EntityToGen> {

    @Override
    public Result gen(Class<EntityToGen> type, Map<String, Object> params) {
        return Result.failure("no generation");
    }

}
