/**
 * This class encapsulates the data required for a reservation event.: Account
 * number and zipcode.
 * 
 * @author Morteza
 * 
 */
public class Reservation extends HotelCustomerEvent {

	public int accountNumber;

	public int zipCode;

	@Override
	public String toString() {
		String superStr = super.toString();
		String res = "Reservation " + superStr + " Account Number: "
				+ accountNumber + " Zip Code: " + zipCode;
		return res;
	}
}
