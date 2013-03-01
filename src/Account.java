/**
 * This class includes data related to an account which is accNumner, balance
 * and zipcode
 * 
 * @author Morteza
 * 
 */
public class Account {
	int accountNumber;
	int balance;
	int zipCode;

	public Account(int accountNumber, int balance, int zipCode) {
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.zipCode = zipCode;
	}

	public Account() {

	}

	@Override
	public String toString() {
		return "Account Number: " + accountNumber + " Zip Code: " + zipCode
				+ " Balance: " + balance;
	}
}
