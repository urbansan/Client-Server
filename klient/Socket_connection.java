/** \file Socket_connection.java
	\brief Plik z mainem
*/

import java.io.IOException;

/** \brief Klasa obsługująca main'a... należy w niej umieścić port oraz adres serwera.
*/
public class Socket_connection {



	public static void main(String[] args){
		int port = 2000;
		String address = "192.168.1.12";
		

		TCPshareClient client = new TCPshareClient(port, address);
		
		try {
			while(client.startService() >=0)
			{}
		} catch (IOException e) {
			System.out.println("Connection timeout or failure");
			e.printStackTrace();
		}

		System.out.println("Communication closed");
	}


}
