package ua.com.fielden.platform.eql.stage1.sources;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.conditions.NullTest2;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.operands.AbstractSingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;

public class YieldInfoNodesGenerator {
    public static Collection<YieldInfoNode> generate(final List<SourceQuery2> models) {
        final List<YieldInfoTail> yieldsInfo = new ArrayList<>();
        for (final YieldInfo yield : generateYieldInfos(models)) {
            yieldsInfo.add(new YieldInfoTail(asList(yield.name().split("\\.")), yield.javaType(), yield.required()));
        }

        return group(yieldsInfo).values();
    }
    
    private static List<YieldInfo> generateYieldInfos(final List<SourceQuery2> models) {
        if (models.size() == 1) {
            return models.get(0).yields.getYields().stream().map(yield -> new YieldInfo(yield.alias, yield.javaType(), determineRequiredness(new YieldAndConditions(yield, models.get(0).conditions)))).collect(toList());
        } else {
            final List<YieldInfo> result = new ArrayList<>(); 
            final Map<String, List<YieldAndConditions>> yieldMatrix = generateYieldMatrixFromQueryModels(models);
            validateYieldsMatrix(yieldMatrix, models.size());
            for (final Entry<String, List<YieldAndConditions>> yieldEntry : yieldMatrix.entrySet()) {
                result.add(new YieldInfo(yieldEntry.getKey(), determineJavaType(yieldEntry), determineRequiredness(yieldEntry.getValue())));
            }
            return result;
        }
    }
    
    private static boolean determineRequiredness(final YieldAndConditions yieldAndConditions) {
        return yieldAndConditions.yield().hasRequiredHint || yieldAndConditions.yield().operand.isNonnullableEntity() || yieldAndConditions.conditions().conditionIsSatisfied(new NullTest2(yieldAndConditions.yield().operand, true));
    }
    
    private static boolean determineRequiredness(final List<YieldAndConditions> yieldVariants) {
        for (final YieldAndConditions yield : yieldVariants) {
            if (!determineRequiredness(yield)) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> determineJavaType(final Entry<String, List<YieldAndConditions>> yieldVariantsEntry) {
        final Set<Class<?>> javaTypes = new HashSet<>();
        for (final YieldAndConditions yield : yieldVariantsEntry.getValue()) {
            if (yield.yield.javaType() != null) {
                javaTypes.add(yield.yield.javaType());
            }
        }
        
        if (javaTypes.isEmpty()) {
            return null;
        } else if (javaTypes.size() == 1) {
            return javaTypes.iterator().next();
        } else {
            return AbstractSingleOperand2.getTypeHighestPrecedence(javaTypes);
        }
    }
    
    private static Map<String, List<YieldAndConditions>> generateYieldMatrixFromQueryModels(final List<SourceQuery2> models) {
        final Map<String, List<YieldAndConditions>> yieldsMatrix = new HashMap<>();        
        for (final SourceQuery2 entQuery : models) {
            for (final Yield2 yield : entQuery.yields.getYields()) {
                final List<YieldAndConditions> foundYields = yieldsMatrix.get(yield.alias);
                if (foundYields != null) {
                    foundYields.add(new YieldAndConditions(yield, entQuery.conditions));
                } else {
                    final List<YieldAndConditions> newList = new ArrayList<>();
                    newList.add(new YieldAndConditions(yield, entQuery.conditions));
                    yieldsMatrix.put(yield.alias, newList);
                }
            }
        }
        return yieldsMatrix;
    }

    private static void validateYieldsMatrix(final Map<String, List<YieldAndConditions>> yieldMatrix, final int modelsCount) {
        for (final Entry<String, List<YieldAndConditions>> entry : yieldMatrix.entrySet()) {
            if (entry.getValue().size() != modelsCount) {
                throw new EqlStage1ProcessingException("Incorrect models used as query source - their result types are different! Alias [" + entry.getKey() + "] has been yielded only " + entry.getValue().size() + " but the models count is " + modelsCount);
            }
        }
    }
    
    private static Map<String, YieldInfoNode> group(final List<YieldInfoTail> yieldsData) {
        final Map<String, List<YieldInfoTail>> yieldsTreeData = new HashMap<>(); // tails starting from the same common part but already without it
        final Map<String, YieldInfo> yieldsWithoutSubprops = new HashMap<>();

        for (final YieldInfoTail yieldData : yieldsData) {
            final String first = yieldData.name.get(0);

            List<YieldInfoTail> existing = yieldsTreeData.get(first);

            if (existing == null) {
                existing = new ArrayList<>();
                yieldsTreeData.put(first, existing);
            }

            if (yieldData.name.size() == 1) {
                yieldsWithoutSubprops.put(first, new YieldInfo(first, yieldData.javaType, yieldData.required));
            } else {
                existing.add(new YieldInfoTail(yieldData.name.subList(1, yieldData.name.size()), yieldData.javaType, yieldData.required));
            }
        }

        final Map<String, YieldInfoNode> result = new HashMap<>();

        for (final Entry<String, List<YieldInfoTail>> yieldTree : yieldsTreeData.entrySet()) {
            final YieldInfo yieldWithoutSubprops = yieldsWithoutSubprops.get(yieldTree.getKey());
            result.put(yieldTree.getKey(), new YieldInfoNode(yieldTree.getKey(), yieldWithoutSubprops == null ? null : yieldWithoutSubprops.javaType, yieldWithoutSubprops == null ? false : yieldWithoutSubprops.required, yieldTree.getValue().isEmpty() ? emptyMap() : group(yieldTree.getValue())));
        }

        return result;
    }
    
    private static record YieldAndConditions(Yield2 yield, Conditions2 conditions) {}
    
    private static record YieldInfo(String name, Class<?> javaType, boolean required) {}
    
    private static record YieldInfoTail(List<String> name, Class<?> javaType, boolean required) {}
}