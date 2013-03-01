/**
 * This class holds data required for a cancelation event.: Room number,
 * confirmation number and account number.
 * 
 * @author Morteza
 * 
 */
public class Cancelation extends HotelCustomerEvent {

	public int roomNumber;

	public int confirmationNumber;

	public int accountNumber;

	@Override
	public String toString() {
		String superStr = super.toString();
		String res = "Cancelation " + superStr + " Room Number: " + roomNumber
				+ " Confirmation Number: " + confirmationNumber;
		return res;
	}
}
