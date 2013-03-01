import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * This class maintains the hotel server. It is a web server, submitting the
 * index.html and subsequent responses based on the requests made by the user.
 * This class generates UI and includes business logic for hotel actions.
 * 
 * @author Morteza
 * 
 */
public class Hotel {

	final static int MAX_ROOM_COUNT = 30;

	private int confirmationNumber = 0;

	static final byte[] EOL = { (byte) '\r', (byte) '\n' };

	List<RoomReservation> reservedRoomList;

	int hotelPort;
	String bankHostName;
	int bankPort;

	public Hotel(int hotelPort, String bankHostName, int bankPort) {
		this.hotelPort = hotelPort;
		this.bankHostName = bankHostName;
		this.bankPort = bankPort;
	}

	private void initializeRoomList() {
		reservedRoomList = new LinkedList<RoomReservation>();
	}

	/**
	 * Responds with the actual index.html content initially, in latter cases it
	 * will respond with the response of the bank server and any details
	 * regarding hotel itself such as room availability, etc.
	 * 
	 * It is rather low level dealing with sockets to the browser.
	 * 
	 * @throws IOException
	 */
	public void execute() throws IOException {

		initializeRoomList();

		String reqStr;
		String respStr = "";

		String indexHtml = getIndexHtml();

		ServerSocket hotelServer = new ServerSocket(hotelPort);

		while (true) {
			int contentLength = -1;
			Socket connectionSocket = hotelServer.accept();
			BufferedReader req = new BufferedReader(new InputStreamReader(
					connectionSocket.getInputStream()));

			PrintStream ps = new PrintStream(connectionSocket.getOutputStream());

			respStr = "HTTP/1.1 200 OK\r\n";
			ps.print(respStr);

			// respStr = "Content-Length: 3326\r\n";
			// ps.print(respStr);

			String pageContent = "";
			String temp = "1";
			while (!temp.isEmpty()) {

				temp = req.readLine();
				reqStr = temp;

				if (reqStr.toLowerCase().contains("content-length:")) {
					contentLength = Integer.parseInt(reqStr.split(":")[1]
							.trim());
				}

				// System.out.println("REQ: " + reqStr);

				// System.out.println("responsed");

			}

			char c;
			String parameters = "";
			for (int i = 0; i < contentLength; i++) {
				c = (char) req.read();
				parameters = parameters + c;
			}

			HotelCustomerEvent hcEvent;
			try {
				hcEvent = handleParameters(parameters);

				if (hcEvent != null) {
					System.out.println("New Request: " + hcEvent);
					pageContent += handleEvent(hcEvent);

				} else {
					pageContent += indexHtml;
				}

			} catch (Exception e) {
				pageContent += "<h1>Bad Parameter Please Retry</h1>";
				pageContent += indexHtml;

			}

			ps.print("Content-Length: " + pageContent.length() + "\r\n");

			ps.println();

			ps.print(pageContent);

			connectionSocket.close();
			ps.println();

			ps.print(respStr);
			// ps.print(indexHtml);
		}

	}

	/**
	 * The high level handling of events and checking logics of the hotel server
	 * and creating response messages.
	 * 
	 * @param hcEvent
	 * @return
	 */
	private String handleEvent(HotelCustomerEvent hcEvent) {
		String message = "<h1>Unknown Request.</h1>";
		if (hcEvent instanceof Reservation) {
			Reservation r = (Reservation) hcEvent;
			boolean hasAlreadyReservedThatDate = hasAlreadyReservedThatDate(
					r, reservedRoomList);
			if (! hasAlreadyReservedThatDate) {
				boolean roomAvailability = checkRoomAvailability(r,
						reservedRoomList);
				if (roomAvailability == true) {
					message = checkBank(hcEvent);
					if (!message.contains("IS NOT OK")) {
						RoomReservation rr = new RoomReservation();
						rr.reservation = r;
						rr.confirmationNumber = getConfirmationNumber();
						rr.roomNumber = getNewRoomNumber(r.day, r.month,
								r.year, reservedRoomList);
						reservedRoomList.add(rr);
						message = message + "<br>Room Number: " + rr.roomNumber
								+ "<br>Confirmation Number: "
								+ rr.confirmationNumber;
					}
				} else
					message = "No room available.";
			} else
				message = "User has already reseved a room for that date.";
		} else if (hcEvent instanceof Cancelation) {
			Cancelation c = (Cancelation) hcEvent;

			RoomReservation toDeleteRoomReservation = null;
			for (RoomReservation rr : reservedRoomList) {
				if (rr.isSameDate(c)
						&& rr.confirmationNumber == c.confirmationNumber
						&& rr.roomNumber == c.roomNumber) {
					toDeleteRoomReservation = rr;
					c.accountNumber = rr.reservation.accountNumber;
				}

			}

			if (toDeleteRoomReservation != null) {
				message = checkBank(hcEvent);
				if (!message.contains("IS NOT OK")) {
					reservedRoomList.remove(toDeleteRoomReservation);
					message = message + " Money RETURNED.";
				}
			} else {
				message = "Reserved room with that data not found. " + c;
			}
		}
		System.out.println("Response: " + message);
		System.out.println();
		return message;
	}

	private boolean hasAlreadyReservedThatDate(Reservation r,
			List<RoomReservation> rrList) {
		for (RoomReservation rr : rrList) {
			if (rr.reservation.day == r.day && rr.reservation.month == r.month
					&& rr.reservation.year == r.year
					&& rr.reservation.accountNumber == r.accountNumber) {
				return true;
			}
		}

		return false;
	}

	/**
	 * interfacing with bank server.
	 * 
	 * @param hcEvent
	 * @return
	 */
	private String checkBank(HotelCustomerEvent hcEvent) {
		String message = "NOT INITIALIZED - RESPONSE.";
		try {
			Socket socket = new Socket(bankHostName, bankPort);
			PrintStream ps = new PrintStream(socket.getOutputStream());
			BufferedReader bankResp = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			if (hcEvent instanceof Reservation) {
				ps.print("Reservation-");
				Reservation r = (Reservation) hcEvent;
				ps.println(r.accountNumber + "-" + r.zipCode);

				ps.flush();
				message = bankResp.readLine();

			} else if (hcEvent instanceof Cancelation) {
				ps.print("Cancelation-");
				Cancelation c = (Cancelation) hcEvent;
				ps.println(c.accountNumber);

				ps.flush();
				message = bankResp.readLine();
			}

			ps.close();
			bankResp.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return message;
	}

	private int getNewRoomNumber(int day, int month, int year,
			List<RoomReservation> rrList) {

		for (int i = 0; i < MAX_ROOM_COUNT; i++) {
			boolean busy = false;
			for (RoomReservation rr : rrList) {
				if (rr.reservation.day == day && rr.reservation.month == month
						&& rr.reservation.year == year) {
					if (rr.roomNumber == i + 1) {
						busy = true;
					}
				}
			}
			if (busy == false)
				return i + 1;
		}
		return -1;
	}

	private boolean checkRoomAvailability(Reservation reservation,
			List<RoomReservation> reservedRoomList_) {
		int dayRoomCount = 0;
		for (RoomReservation rr : reservedRoomList_) {
			if (rr.isSameDate(reservation))
				dayRoomCount++;
		}
		return dayRoomCount < MAX_ROOM_COUNT;
	}

	/**
	 * extract content of data from request from the browser and assign them to
	 * an event.
	 * 
	 * @param parameterStr
	 * @return
	 * @throws Exception
	 */
	private HotelCustomerEvent handleParameters(String parameterStr)
			throws Exception {
		HotelCustomerEvent hcEvent = null;
		if (parameterStr.contains("whattodo=reservation")) {
			Reservation reservation = new Reservation();
			String[] parametersArr = parameterStr.split("&");
			for (String s : parametersArr) {
				if (s.contains("Rday")) {
					reservation.day = Integer.parseInt(s.split("=")[1]);
				} else if (s.contains("Rmonth")) {
					reservation.month = Integer.parseInt(s.split("=")[1]);
				} else if (s.contains("Ryear")) {
					reservation.year = Integer.parseInt(s.split("=")[1]);
				} else if (s.contains("Raccountnumber")) {
					reservation.accountNumber = Integer
							.parseInt(s.split("=")[1]);
				} else if (s.contains("Rzipcode")) {
					reservation.zipCode = Integer.parseInt(s.split("=")[1]);
				}
			}
			hcEvent = reservation;
		} else if (parameterStr.contains("whattodo=cancelation")) {
			Cancelation cancelation = new Cancelation();
			String[] parametersArr = parameterStr.split("&");
			for (String s : parametersArr) {
				if (s.contains("Cday")) {
					cancelation.day = Integer.parseInt(s.split("=")[1]);
				} else if (s.contains("Cmonth")) {
					cancelation.month = Integer.parseInt(s.split("=")[1]);
				} else if (s.contains("Cyear")) {
					cancelation.year = Integer.parseInt(s.split("=")[1]);
				} else if (s.contains("Croomnumber")) {
					cancelation.roomNumber = Integer.parseInt(s.split("=")[1]);
				} else if (s.contains("Cconfirmationnumber")) {
					cancelation.confirmationNumber = Integer.parseInt(s
							.split("=")[1]);
				}
			}
			hcEvent = cancelation;
		}
		return hcEvent;
	}

	/**
	 * load the content of the index in the beginning of the program running.
	 * 
	 * @return
	 */
	private static String getIndexHtml() {

		String s = "";
		Scanner sc = null;
		System.out.println();
		try {
			sc = new Scanner(new File(System.getProperty("user.dir")
					+ "/src/index.html"));

		} catch (Exception e) {
			try {
				sc = new Scanner(new File("index.html"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

		}
		while (sc.hasNextLine()) {
			s += sc.nextLine();
		}
		System.out.println("index.html loaded.");
		return s;
	}

	public static void main(String[] args) throws IOException {
		System.out
				.println("Note: Please make sure the hotel host name and port number are properly set in the index.html file.");
		System.out.println();
		try {
			Hotel hotel = new Hotel(Integer.parseInt(args[0]), args[1],
					Integer.parseInt(args[2]));
			hotel.execute();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println();
			System.out
					.println("Usage: Hotel <Hotel_Port_Number> <Bank_Host_Name> <Bank Port_Number>");

		}
	}

	private int getConfirmationNumber() {
		return ++confirmationNumber;
	}

}
