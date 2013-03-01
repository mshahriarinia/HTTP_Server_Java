import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * This class maintains the bank server. It supports addition to or reduction
 * from account balances based on account number, current balance, and zip code.
 * 
 * @author Morteza
 * 
 */
public class Bank {

	static final int COST_PER_DAY = 50;

	int port;

	List<Account> accountList;

	public Bank(int port) {
		this.port = port;
		initAccountList();
	}

	private void initAccountList() {
		accountList = new LinkedList<Account>();

		Account account;

		account = new Account(1, 100, 1);
		accountList.add(account);

		account = new Account(2, 100, 1);
		accountList.add(account);

		account = new Account(3, 100, 1);
		accountList.add(account);

		account = new Account(4, 100, 1);
		accountList.add(account);

		account = new Account(5, 100, 1);
		accountList.add(account);

		account = new Account(6, 100, 1);
		accountList.add(account);

		account = new Account(7, 100, 1);
		accountList.add(account);

		account = new Account(8, 100, 1);
		accountList.add(account);

		account = new Account(9, 100, 1);
		accountList.add(account);

		account = new Account(10, 100, 1);
		accountList.add(account);
	}

	/**
	 * interface with hotel
	 * 
	 * @throws IOException
	 */
	private void execute() throws IOException {

		String reqStr;
		String respStr = "";

		ServerSocket bankServer = new ServerSocket(port);

		while (true) {
			Socket connectionSocket = bankServer.accept();
			BufferedReader req = new BufferedReader(new InputStreamReader(
					connectionSocket.getInputStream()));

			PrintStream ps = new PrintStream(connectionSocket.getOutputStream());

			reqStr = req.readLine();

			respStr = handle(reqStr);
			ps.println(respStr);
			System.out.println("Response: " + respStr);
			System.out.println();
			ps.flush();

			req.close();
			ps.close();
			connectionSocket.close();
		}

	}

	/**
	 * and maintain balance modifications based on the requests received from
	 * hotel.
	 * 
	 * @param reqStr
	 * @return
	 */
	private String handle(String reqStr) {
		Account accTemp = new Account();

		boolean isReservation = reqStr.contains("Reservation");
		boolean isCancelation = reqStr.contains("Cancelation");
		if (isReservation) {
			accTemp.accountNumber = Integer.parseInt(reqStr.split("-")[1]);
			accTemp.zipCode = Integer.parseInt(reqStr.split("-")[2]);

			System.out.println("New request: Reservation - Account No: "
					+ accTemp.accountNumber + " Zip Code: " + accTemp.zipCode);

			Account accFound = getAccount(accTemp.accountNumber);
			if (accFound == null)
				return "BANK: IS NOT OK. Account not found.";
			else {

				System.out.println(accFound);
				if (accFound.balance >= COST_PER_DAY) {
					accFound.balance -= COST_PER_DAY;
					return "BANK: IS OK." + " " + accFound.toString();
				} else
					return "BANK: IS NOT OK. Not enough balance in bank account."
							+ " " + accFound.toString();

			}
		} else if (isCancelation) {
			int accountNo = Integer.parseInt(reqStr.split("-")[1]);
			Account accFound = getAccount(accountNo);
			System.out.println("New request: Cancelation - Account Number: "
					+ accountNo);
			if (accFound == null)
				return "BANK: IS NOT OK. Account not found.";
			else {
				accFound.balance += COST_PER_DAY;
				return "BANK: IS OK." + " " + accFound.toString();
			}
		}
		return "BANK: IS NOT OK. INVALID REQUEST.";
	}

	public static void main(String[] args) throws IOException {
		try{
		Bank bank = new Bank(Integer.parseInt(args[0]));
		bank.execute();
		}catch (Exception e) {
			System.out.println("Usage: Bank <Port_Number>");
		}
	}

	private Account getAccount(int accountNumber) {
		for (Account acc : accountList)
			if (accountNumber == acc.accountNumber) {
				return acc;
			}
		return null;
	}

}
