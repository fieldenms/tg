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
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.conditions.NullPredicate2;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.operands.AbstractSingleOperand2;
import ua.com.fielden.platform.eql.stage2.queries.SourceQuery2;

public class YieldInfoNodesGenerator {
    public static Collection<YieldInfoNode> generate(final List<SourceQuery2> models) {
        final List<YieldInfoTail> yieldsInfo = new ArrayList<>();
        for (final YieldInfo yield : generateYieldInfos(models)) {
            yieldsInfo.add(new YieldInfoTail(asList(yield.name().split("\\.")), yield.propType(), yield.nonnullable()));
        }

        return group(yieldsInfo).values();
    }
    
    private static List<YieldInfo> generateYieldInfos(final List<SourceQuery2> models) {
        if (models.size() == 1) {
            return models.get(0).yields.getYields().stream().map(yield -> new YieldInfo(yield.alias, yield.operand.type(), determineNonnullability(new YieldAndConditions(yield, models.get(0).whereConditions)))).collect(toList());
        } else {
            final List<YieldInfo> result = new ArrayList<>(); 
            final Map<String, List<YieldAndConditions>> yieldMatrix = generateYieldMatrixFromQueryModels(models);
            validateYieldsMatrix(yieldMatrix, models.size());
            for (final Entry<String, List<YieldAndConditions>> yieldEntry : yieldMatrix.entrySet()) {
                result.add(new YieldInfo(yieldEntry.getKey(), determinePropType(yieldEntry.getValue()), determineNonnullability(yieldEntry.getValue())));
            }
            return result;
        }
    }
    
    private static boolean determineNonnullability(final YieldAndConditions yieldAndConditions) {
        return yieldAndConditions.yield().hasNonnullableHint || yieldAndConditions.yield().operand.isNonnullableEntity() || yieldAndConditions.conditions().conditionIsSatisfied(new NullPredicate2(yieldAndConditions.yield().operand, true));
    }
    
    private static boolean determineNonnullability(final List<YieldAndConditions> yieldVariants) {
        for (final YieldAndConditions yield : yieldVariants) {
            if (!determineNonnullability(yield)) {
                return false;
            }
        }
        return true;
    }

    private static PropType determinePropType(final List<YieldAndConditions> yieldVariants) {
        final Set<PropType> propTypes = new HashSet<>();
        for (final YieldAndConditions yieldVariant : yieldVariants) {
            if (yieldVariant.yield.operand.type() != null) {
                propTypes.add(yieldVariant.yield.operand.type());
            }
        }
        
        if (propTypes.isEmpty()) {
            return null;
        } else if (propTypes.size() == 1) {
            return propTypes.iterator().next();
        } else {
            return AbstractSingleOperand2.getTypeHighestPrecedence(propTypes);
        }
    }
    
    private static Map<String, List<YieldAndConditions>> generateYieldMatrixFromQueryModels(final List<SourceQuery2> models) {
        final Map<String, List<YieldAndConditions>> yieldsMatrix = new HashMap<>();        
        for (final SourceQuery2 entQuery : models) {
            for (final Yield2 yield : entQuery.yields.getYields()) {
                final List<YieldAndConditions> foundYields = yieldsMatrix.get(yield.alias);
                if (foundYields != null) {
                    foundYields.add(new YieldAndConditions(yield, entQuery.whereConditions));
                } else {
                    final List<YieldAndConditions> newList = new ArrayList<>();
                    newList.add(new YieldAndConditions(yield, entQuery.whereConditions));
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
                yieldsWithoutSubprops.put(first, new YieldInfo(first, yieldData.propType, yieldData.nonnullable));
            } else {
                existing.add(new YieldInfoTail(yieldData.name.subList(1, yieldData.name.size()), yieldData.propType, yieldData.nonnullable));
            }
        }

        final Map<String, YieldInfoNode> result = new HashMap<>();

        for (final Entry<String, List<YieldInfoTail>> yieldTree : yieldsTreeData.entrySet()) {
            final YieldInfo yieldWithoutSubprops = yieldsWithoutSubprops.get(yieldTree.getKey());
            result.put(yieldTree.getKey(), new YieldInfoNode(yieldTree.getKey(), yieldWithoutSubprops == null ? null : yieldWithoutSubprops.propType, yieldWithoutSubprops == null ? false : yieldWithoutSubprops.nonnullable, yieldTree.getValue().isEmpty() ? emptyMap() : group(yieldTree.getValue())));
        }

        return result;
    }
    
    private static record YieldAndConditions(Yield2 yield, Conditions2 conditions) {}
    
    private static record YieldInfo(String name, PropType propType, boolean nonnullable) {}
    
    private static record YieldInfoTail(List<String> name, PropType propType, boolean nonnullable) {}
}