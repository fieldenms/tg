package ua.com.fielden.platform.eql.stage1.elements;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.eql.stage1.elements.operands.EntProp1.enhancePath;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.keyPaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntProp1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.OrderBy2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class OrderBy1 {
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand;
    public final String yieldName;
    public final boolean isDesc;

    public OrderBy1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean isDesc) {
        this.operand = operand;
        this.isDesc = isDesc;
        this.yieldName = null;
    }

    public OrderBy1(final String yieldName, final boolean isDesc) {
        this.operand = null;
        this.isDesc = isDesc;
        this.yieldName = yieldName;
    }

    public List<OrderBy2> transform(final PropsResolutionContext context, final Yields1 yields1, final IQrySource2<? extends IQrySource3> mainSource) {
        return operand != null ? transformForOperand(context, operand.transform(context), operand) : transformForYield(context, yields1, mainSource);
    }

    public List<OrderBy2> transformForYield(final PropsResolutionContext context, final Yields1 yields1, final IQrySource2<? extends IQrySource3> mainSource) {
        if (yields1.getYieldsMap().containsKey(yieldName)) {
            final Yield1 yield = yields1.getYieldsMap().get(yieldName);
            if (yield.operand instanceof EntProp1 /* && not calculated*/) {
                return (new OrderBy1(yield.operand, isDesc)).transformForOperand(context, yield.operand.transform(context), yield.operand);
            } else {
                return asList(new OrderBy2(yieldName, isDesc));
            }
        } else if (yields1.getYieldsMap().isEmpty()) {
            final PropResolution propResolution = EntProp1.resolvePropAgainstSource(mainSource, new EntProp1(yieldName, false));
            //final AbstractPropInfo<?> propInfo = mainSource.entityInfo().getProps().get(yieldName);
            if (propResolution != null) {
                final List<AbstractPropInfo<?>> path = enhancePath(propResolution.getPath());

                final int pathLength = path.size();
                final Class<?> containingType = pathLength == 1 ? mainSource.sourceType() : path.get(pathLength - 2).javaType();
                final String operandPropLastMember = path.get(pathLength - 1).name;
                if (!operandPropLastMember.equals(KEY) ||
                        operandPropLastMember.equals(KEY) && !isCompositeEntity((Class<? extends AbstractEntity<?>>) containingType)) {
                    return asList(new OrderBy2(new EntProp2(mainSource, path), isDesc));
                } else {
                    final Class<? extends AbstractEntity<?>> containingEntityType = (Class<? extends AbstractEntity<?>>) containingType;
                    final List<String> keyOrderProps = isCompositeEntity(containingEntityType) ? keyPaths(containingEntityType) : emptyList();
                    final String prefix = yieldName.equals(KEY) ? "" : yieldName.substring(0, yieldName.length() - 3);
                    final List<OrderBy2> result = new ArrayList<>();
                    for (final String keyMemberProp : keyOrderProps) {
                        final PropResolution keyMemberPropResolution = EntProp1.resolvePropAgainstSource(mainSource, new EntProp1(prefix + keyMemberProp, false));
                        final List<AbstractPropInfo<?>> keyMemberPropPath = enhancePath(keyMemberPropResolution.getPath());
                        result.add(new OrderBy2(new EntProp2(mainSource, keyMemberPropPath), isDesc));
                    }
                    return result;
                }
//                return transformForOperand(context, new EntProp2(mainSource, path), null);    
            }
        }
        throw new EqlException("Can't find yield [" + yieldName + "]!");
    }

    public List<OrderBy2> transformForOperand(final PropsResolutionContext context, final ISingleOperand2<?> operandTr, final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        final List<OrderBy2> result = new ArrayList<>();
        //final ISingleOperand2<?> operandTr = operand.transform(context);
        if (!(operandTr instanceof EntProp2)) {
            result.add(new OrderBy2(operandTr, isDesc));
        } else {
            final EntProp2 operandProp = (EntProp2) operandTr;
            final int pathLength = operandProp.getPath().size();
            final Class<?> containingType = pathLength == 1 ? operandProp.source.sourceType() : operandProp.getPath().get(pathLength - 2).javaType();
            final String operandPropLastMember = operandProp.getPath().get(pathLength - 1).name;
            if (!operandPropLastMember.equals(KEY) ||
                    operandPropLastMember.equals(KEY) && !isCompositeEntity((Class<? extends AbstractEntity<?>>) containingType)) {
                result.add(new OrderBy2(operandTr, isDesc));
            } else {
                final Class<? extends AbstractEntity<?>> containingEntityType = (Class<? extends AbstractEntity<?>>) containingType;
                final List<String> keyOrderProps = isCompositeEntity(containingEntityType) ? keyPaths(containingEntityType) : emptyList();
                final String prefix = ((EntProp1) operand).name.equals(KEY) ? "" : ((EntProp1) operand).name.substring(0, ((EntProp1) operand).name.length() - 3);

                for (final String keyMemberProp : keyOrderProps) {
                    result.add(new OrderBy2((new EntProp1(prefix + keyMemberProp, false)).transform(context), isDesc));
                }
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isDesc ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        result = prime * result + ((yieldName == null) ? 0 : yieldName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OrderBy1)) {
            return false;
        }

        final OrderBy1 other = (OrderBy1) obj;

        return Objects.equals(operand, other.operand) && Objects.equals(yieldName, other.yieldName) && (isDesc == other.isDesc);
    }
}