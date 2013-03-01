/**
 * This is the base class of all events (reservation and cancellation), the
 * common data content among events is date.
 * 
 * @author Morteza
 * 
 */
public abstract class HotelCustomerEvent {

	public int day, month, year;

	@Override
	public String toString() {
		return month + "/" + day + "/" + year;
	}
}
