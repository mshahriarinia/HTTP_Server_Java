/**
 * This class holds the data of a valid room reservation, which is room number,
 * confirmation number and the data of the reservation event itself.
 * 
 * @author Morteza
 * 
 */
public class RoomReservation {

	public int roomNumber;

	public Reservation reservation;

	public int confirmationNumber;

	public boolean isSameDate(HotelCustomerEvent r) {
		return r.day == reservation.day && r.month == reservation.month
				&& r.year == reservation.year;
	}

}
