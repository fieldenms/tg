package ua.com.fielden.platform.example.swing.booking;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import ua.com.fielden.platform.utils.Pair;

public class VehicleDataStore {

    public static final List<Pair<VehicleEntity, List<BookingEntity>>> allData = new ArrayList<>();

    static {
	final VehicleEntity veh1 = new VehicleEntity().setKey("veh1").setDesc("First vehicle description.");
	final VehicleEntity veh2 = new VehicleEntity().setKey("veh2").setDesc("Second vehicle description.");
	final VehicleEntity veh3 = new VehicleEntity().setKey("veh3").setDesc("Third vehicle description.");

	final BookingEntity be1ForVeh1 = new BookingEntity().setVehicleEntity(veh1).//
		setBookingStart(new DateTime(2014, 4, 1, 0, 0).toDate()).//
		setBookingFinish(new DateTime(2014, 4, 20, 0, 0).toDate()).//
		setActStart(new DateTime(2014, 4, 1, 0, 0).toDate()).//
		setActFinish(new DateTime(2014, 4, 20, 0, 0).toDate());
	final BookingEntity be2ForVeh1 = new BookingEntity().setVehicleEntity(veh1).//
		setBookingStart(new DateTime(2014, 5, 30, 0, 0).toDate()).//
		setBookingFinish(new DateTime(2014, 6, 5, 0, 0).toDate());
	final BookingEntity be3ForVeh1 = new BookingEntity().setVehicleEntity(veh1).//
		setBookingStart(new DateTime(2014, 6, 10, 0, 0).toDate()).//
		setBookingFinish(new DateTime(2014, 6, 20, 0, 0).toDate());

	final BookingEntity be1ForVeh2 = new BookingEntity().setVehicleEntity(veh2).//
		setBookingStart(new DateTime(2014, 5 ,1, 0, 0).toDate()).//
		setBookingFinish(new DateTime(2014, 5, 19, 15, 0).toDate()).//
		setActStart(new DateTime(2014, 5, 2, 0, 0).toDate());
	final BookingEntity be2ForVeh2 = new BookingEntity().setVehicleEntity(veh2).//
		setBookingStart(new DateTime(2014, 5, 20, 0, 0).toDate()).//
		setBookingFinish(new DateTime(2014, 6, 9, 0, 0).toDate());

	final BookingEntity be1ForVeh3 = new BookingEntity().setVehicleEntity(veh3).//
		setBookingStart(new DateTime(2014, 5, 10, 0, 0).toDate()).//
		setBookingFinish(new DateTime(2014, 5, 31, 0, 0).toDate()).//
		setActStart(new DateTime(2014, 5, 11, 0, 0).toDate());

	final List<BookingEntity> be1 = new ArrayList<>();
	be1.add(be1ForVeh1);
	be1.add(be2ForVeh1);
	be1.add(be3ForVeh1);

	final List<BookingEntity> be2 = new ArrayList<>();
	be2.add(be1ForVeh2);
	be2.add(be2ForVeh2);

	final List<BookingEntity> be3 = new ArrayList<>();
	be3.add(be1ForVeh3);

	allData.add(new Pair<>(veh1, be1));
	allData.add(new Pair<>(veh2, be2));
	allData.add(new Pair<>(veh3, be3));
    }
}
