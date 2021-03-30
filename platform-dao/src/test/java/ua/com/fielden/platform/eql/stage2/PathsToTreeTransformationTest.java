package ua.com.fielden.platform.eql.stage2;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage2.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Child;
import ua.com.fielden.platform.eql.stage2.sources.ChildGroup;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeWorkOrder;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.types.tuples.T2;

public class PathsToTreeTransformationTest extends EqlStage2TestCase {
    
    @Test
    public void test_spike() {
        final T2<EntQueryGenerator, ResultQuery2> res =
                //qryCountAll2(select(TeVehicle.class).where().prop("replacedByTwice.replacedBy.replacedByTwice.modelMakeKey8").isNotNull());
                //qryCountAll2(select(TeVehicle.class).where().prop("replacedByTwice.replacedByTwice.modelMakeKey8").isNotNull());
                //qryCountAll2(select(TeVehicle.class).where().prop("replacedByTwice.modelMakeKey8").isNotNull());
                qryCountAll2(select(TeWorkOrder.class).where().anyOfProps("replacedByMake.key", "replacedByTwiceMake.key", "vehicle.replacedByTwice.key").isNotNull());
                //qryCountAll2(select(TeVehicleMake.class).where().prop("id").isNotNull().and().exists(select(TgBogie.class).model()));
                //qryCountAll2(select(TeVehicle.class).where().prop("mmake.key").isNotNull());
                //qryCountAll2(select(TeVehicle.class).where().prop("avgRepPrice").isNotNull());
                //qryCountAll2(select(TgBogie.class).where().prop("location.key").isNotNull());
                //qryCountAll2(select(TgBogie.class).where().prop("location.fuelType.key").isNotNull());
                //qryCountAll2(select(TgBogie.class).where().prop("location.fuelType").isNotNull());
                //qryCountAll2(select(TeVehicle.class).where().anyOfProps("mmake2").isNotNull());
                //qryCountAll2(select(TeVehicle.class).where().anyOfProps("modelMake2", "modelMakeKey8").isNotNull());
                //qryCountAll2(select(TeVehicle.class).where().prop("modelMakeKey7").isNotNull());
                //qryCountAll2(select(TeVehicle.class).where().prop("modelMakeKey8").isNotNull());
                //qryCountAll2(select(TeWorkOrder.class).where().prop("vehicleMake.key").isNotNull());
                //qryCountAll2(select(TeVehicleMake.class).where().anyOfProps("p7").isNotNull());
                //qryCountAll2(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey", "vehicle.modelMakeKeyDuplicate").isNotNull());
                //qryCountAll2(select(TeWorkOrder.class).where().anyOfProps("vehicle.station.parent.key", "vehicle.modelMakeKey", "vehicle.model.make.key", "vehicle.model.make.desc").isNotNull());
                //qryCountAll2(select(VEHICLE).where().anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull());
        final ResultQuery2 actQry = res._2;
        
        final PathsToTreeTransformator p2tt = new PathsToTreeTransformator(DOMAIN_METADATA.eqlDomainMetadata, res._1);
        Map<String, List<Child>> children = p2tt.transform(actQry.collectProps());
        for (Entry<String, List<Child>> el : children.entrySet()) {
            System.out.println("\n<<< QrySource: " + el.getKey());
            int i = 1;
            for (Child child : el.getValue()) {
                System.out.println(child);
                i  = i + 1;
                
            }
        }
        
        System.out.println("=====================================================================");
        System.out.println("=====================================================================");
        System.out.println("=====================================================================");
        System.out.println("=====================================================================");
        System.out.println("=====================================================================");
        
        Map<String, List<ChildGroup>> children2 = p2tt.groupChildren(actQry.collectProps());
        for (Entry<String, List<ChildGroup>> el : children2.entrySet()) {
            System.out.println("<<< QrySource: " + el.getKey());
            int i = 1;
            for (ChildGroup child : el.getValue()) {
                System.out.println(child);
                i  = i + 1;
                
            }
        }
    }
}