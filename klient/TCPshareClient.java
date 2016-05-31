/** \file TCPshareClient.java
	\brief Plik zawiera końcową implementacje klienta.
	
*/

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

/** \brief Końcowa implementacja klienta.
*/
public class TCPshareClient extends TCPclient implements sharedClientMethods {

	int BUFF_SIZE;
	static Set<String> localFileList = Collections
			.synchronizedSet(new LinkedHashSet<>());
	static Set<String> remoteFileList = Collections
			.synchronizedSet(new LinkedHashSet<>());
	

	public TCPshareClient(int port, String server) {
		super(port, server);// "192.168.1.12"


		this.BUFF_SIZE = 8192;

		File path = new File("C:/shared_java/");
		if (!path.exists()) {
			path.mkdir();
		}

		path = new File("C:/shared_java/specific/");
		if (!path.exists()) {
			path.mkdir();
		}
	}
/** \brief Metoda nawiązujaca połączenie i przekazująca połączenie do poniższych metod.

	Tworzone gniazdo \a sock nie jest atrybutem obiektu dlatego, że byłaby możliwość zamknięcia połączenia w przypadku, 
	gdy na oddzielnym wątku jeszcze dokonuje się download/upload. Gniazdo jest przekazywane i zamykane przez każdą z opcji 
	wykonywanej przez ActAnOption().
	\returns -1 w przypadku błędu lub zakończenia aplikacji.
*/
	public int startService() throws IOException {
		int option = -1;

		try {
			Socket sock = new Socket(this.server, this.port); 
			System.out.println("Connecting...");
			System.out.println(sock.getInetAddress().getHostAddress());

			try {

				if ((option = ActAnOption(sock)) < 0)
					System.out.println("Nothing has been performed");

			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
		} catch (IOException e1) {
			System.out.println("Connection timeout or failure ");
		} finally {
			System.out.println("Round finished");

		}
		return option;
	}

/** \brief Główna metoda obsługująca połączenie z serwerem.
	\param sock - gniazdo połączeniowe.
	
	Opcje dostępne: download, upload, usuniecie pliku na serwerze, wyswietlenie listy plikow na serwerze, usuniecie danych specyficznych klienta.
	
	Opcja download:
	Klient: nawiązanie połączenia
	Serwer: akceptacja, nadanie gniazda
	klient: przesłanie nr opcje download (int)
	serwer: przyjmuje opcję i przechodzi do czesci zwiazanej z opcja download
	klient: przesyła nazwę pliku (string)
	serwer: odbiera nazwę pliku, rozpoczyna jego strumieniowanie i zamyka
	połączenie
	klient: odbiera strumien i zamyka polaczenie.

	Opcja upload jest analogiczna. Opcja Send List pomija etap weryfikacji
	nazwy pliku, ktora jest stała. Opcja usunięcia pliku weryfikuje nazwe
	pliku, ale pomija przesyłanie danych.
	\returns -1, gdy zostaną błędnie wybrane opcje lub użytkownik chce zamknąć aplikacje.
*/
	public int ActAnOption(Socket sock) throws IOException {
	

		BufferedReader klawa = new BufferedReader(new InputStreamReader(System.in));

		String nrPlikuDoPobrania;
		OutputStream out_s = sock.getOutputStream();

		System.out
				.println("(1) - Update remote files list\n(2) - Download\n(3) - upload\n(4) - close Client\n(0) - delete remote file");
		int option = 0;

		try {
			option = Integer.parseInt(klawa.readLine());

		} catch (NumberFormatException ebe) {
			System.out.println("Invalid option");

		}

		// 1) wyslanie opcji
		out_s.write(option);
		out_s.flush();

		if (option == 0) {
			System.out
					.println("Chose the number of the file you want to delete (pick from the \"Remote\" column)");
			nrPlikuDoPobrania = klawa.readLine();

			int readyFlag = 0;
			Iterator<String> itr = remoteFileList.iterator();

			String lineFromList[];

			while (itr.hasNext()) {
				lineFromList = ((String) itr.next()).split("\t");
				if (lineFromList[0].equals(nrPlikuDoPobrania)) {
					readyFlag = 1;
					out_s.write(lineFromList[1].getBytes(Charset
							.forName("UTF-8")));
					out_s.flush();
					break;
				}
			}

			if (readyFlag == 0) {
				out_s.write("wrongFile".getBytes(Charset.forName("UTF-8")));
				out_s.flush();
				System.out.println("Incorrect file, no deletion performed");
				if (sock != null) sock.close();
			}
			if (sock != null)
				sock.close();
		} else
		if (option == 1) {

			showAvailabeFiles(sock);
			// if (sock != null) sock.close();

		} else if (option == 2 || option == 3) {

			if (option == 2)
				System.out
						.println("Chose the number of the file you want (pick from the \"Remote\" column)");
			if (option == 3)
				System.out
						.println("Chose the number of the file you want (pick from the \"Local\" column)");

			nrPlikuDoPobrania = klawa.readLine();
			String yesno = "Y";
			int readyFlag = 0;

			try {
				if (option == 2)
					checkIfOverwritingFiles(nrPlikuDoPobrania, "RemoteNumber");
				if (option == 3)
					checkIfOverwritingFiles(nrPlikuDoPobrania, "LocalNumber");
			} catch (jabelko e1) {

				if (e1.getIsOverwrite()) {
					if (option == 2)
						System.out
								.println("Do you want to overwrite the local file? Y/N");
					if (option == 3)
						System.out
								.println("Do you want to overwrite the remote file? Y/N");
					yesno = klawa.readLine();
				}
			}

			if (yesno.equals("Y")) {
				Iterator<String> itr = null;
				if (option == 2)
					itr = remoteFileList.iterator();
				if (option == 3)
					itr = localFileList.iterator();

				String lineFromList[];

				while (itr.hasNext()) {
					lineFromList = ((String) itr.next()).split("\t");

					if (lineFromList[0].equals(nrPlikuDoPobrania)) {
						readyFlag = 1;
						
						out_s.write(lineFromList[1].getBytes(Charset
								.forName("UTF-8")));
						out_s.flush();
						final String filename = lineFromList[1];
						final int option2 = option;
						System.out.println("File Processed: " + filename);

						 new Thread() { public void run() {
							 
						try {
							if (option2 == 2)
								downloadFile("c:/shared_java/".concat(filename), sock);
							if (option2 == 3)
								uploadFile("c:/shared_java/".concat(filename), sock);
							 
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						 }}.start();

						break;
					}
				}
			}

			if (readyFlag == 0) {
				out_s.write("wrongFile".getBytes(Charset.forName("UTF-8")));
				out_s.flush();
				System.out
						.println("Incorrect file or no permission to overwrite");
			}

		} else {
			System.out.println("Finishing...");
			out_s.close();
			if (sock != null) sock.close();
			return -1;
		}
		
		return 0;
	}

	@Override
	public void getRemoteFileList(Socket sock) {
		remoteFileList.clear();
		try {

			downloadFile("c:/shared_java/specific/lista.txt", sock);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		FileReader fr;

		try {
			fr = new FileReader("c:/shared_java/specific/lista.txt");

			Scanner plik = new Scanner(fr);
			String line;

			while (plik.hasNextLine()) {
				line = plik.nextLine();
				remoteFileList.add(line);
				// System.out.println(line);
			}

			plik.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getLocalFileList() {
		localFileList.clear();
		File folder = new File("c:/shared_java");
		File[] listOfFiles = folder.listFiles();

		for (Integer i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				localFileList.add(i.toString() + "\t"
						+ listOfFiles[i].getName());
			}
		}

	}

	
/** \brief Metoda zestawia pliki dostępne na serwerze z lokalnymi plikami z katalogu c:\shared_java.
	\param sock - gniazdo połączeniowe, które jest przekazywane do metody getRemoteFileList().
	
	Uruchamiane są metody wczytania listy plików do statycznych kontenerów:
	1. getLocalFileList() - \a localFileList
	2. getRemoteFileList() - \a remoteFileList
	Następnie kolekcje są porównywane w ten sposób, że jedna linia odpowiada jednemu plikowi. 
	Dołożone są 2 kolumny wskazujące na identyfikator pliku na serwerze lub w lokalnym katalogu.
	Brak identyfikatora oznacza brak pliku w danym systemie plików.
*/
	public void showAvailabeFiles(Socket sock) {

		getLocalFileList();
		getRemoteFileList(sock);
		// System.out.println(localFileList.toString());
		// System.out.println(remoteFileList.toString());
		//

		System.out.println("Remote | Local | file");
		System.out.println("-----------------------------");

		String lineFromRemoteList[];
		String lineFromLocalList[];
		Iterator<String> remoteItr = remoteFileList.iterator();
		Iterator<String> localItr;
		String localPosition;

		while (remoteItr.hasNext()) {
			lineFromRemoteList = ((String) remoteItr.next()).split("\t");
			localPosition = " ";
			localItr = localFileList.iterator();
			while (localItr.hasNext()) {
				lineFromLocalList = ((String) localItr.next()).split("\t");
				if (lineFromRemoteList[1].equals(lineFromLocalList[1])) {
					localPosition = lineFromLocalList[0];
					break;
				}
			}
			// System.out.println(lineFromRemoteList[0] + " " + localPosition +
			// " " + lineFromRemoteList[1]);
			System.out.printf("%6s | %5s | %s\n", lineFromRemoteList[0],
					localPosition, lineFromRemoteList[1]);
		}
		localItr = localFileList.iterator();
		while (localItr.hasNext()) {
			lineFromLocalList = ((String) localItr.next()).split("\t");
			localPosition = " ";
			remoteItr = remoteFileList.iterator();
			while (remoteItr.hasNext()) {
				lineFromRemoteList = ((String) remoteItr.next()).split("\t");
				if (lineFromRemoteList[1].equals(lineFromLocalList[1])) {
					localPosition = lineFromLocalList[0];
					break;
				}
			}
			if (localPosition.equals(" ")) {
				// System.out.println(localPosition + " " + lineFromLocalList[0]
				// + " " + lineFromLocalList[1]);
				System.out.printf("%6s | %5s | %s\n", localPosition,
						lineFromLocalList[0], lineFromLocalList[1]);
			}
			// System.out.println(String.format("%-5s", "") + "w aliasach: " +
			// alias.toString());
		}
	}

	@Override
	public void checkIfOverwritingFiles(String position, String option)
			throws jabelko {

		if (option.equals("RemoteNumber")) {
			String lineFromRemoteList[];
			String lineFromLocalList[];
			Iterator<String> remoteItr = remoteFileList.iterator();
			Iterator<String> localItr;

			while (remoteItr.hasNext()) {
				lineFromRemoteList = ((String) remoteItr.next()).split("\t");
				if (lineFromRemoteList[0].equals(position)) {
					localItr = localFileList.iterator();
					while (localItr.hasNext()) {
						lineFromLocalList = ((String) localItr.next())
								.split("\t");
						if (lineFromRemoteList[1].equals(lineFromLocalList[1])) {
							throw new jabelko(true);
						}
					}
				}
			}
			throw new jabelko(false);
		}
		if (option.equals("LocalNumber")) {
			String lineFromRemoteList[];
			String lineFromLocalList[];
			Iterator<String> localItr = localFileList.iterator();
			Iterator<String> remoteItr;

			while (localItr.hasNext()) {
				lineFromLocalList = ((String) localItr.next()).split("\t");
				if (lineFromLocalList[0].equals(position)) {
					remoteItr = remoteFileList.iterator();
					while (remoteItr.hasNext()) {
						lineFromRemoteList = ((String) remoteItr.next())
								.split("\t");
						if (lineFromLocalList[1].equals(lineFromRemoteList[1])) {
							throw new jabelko(true);
						}
					}
				}
			}
			throw new jabelko(false);
		}

	}

	@Override
	public void downloadFile(String filename, Socket sock) throws IOException {

		int bytesRead;
		int allBytes = 0;

		byte[] byteBuffer = new byte[BUFF_SIZE];
		FileOutputStream fos = new FileOutputStream(filename);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		DataInputStream fromRemote = new DataInputStream(sock.getInputStream());

		while ((bytesRead = fromRemote.read(byteBuffer, 0, BUFF_SIZE)) > -1) {
			bos.write(byteBuffer, 0, bytesRead);
			allBytes += bytesRead;
		}
		bos.flush();

		System.out.println("File " + filename + " downloaded (" + allBytes
				+ " bytes recieved)");

		fos.close();
		bos.close();
		sock.close();
	}

	@Override
	public void uploadFile(String filename, Socket sock) throws IOException {

		int bytesRead = 0;
		int allBytes = 0;
		FileInputStream fis = null;
		BufferedInputStream bis = null;

		byte[] byteBuffer = new byte[BUFF_SIZE];
		fis = new FileInputStream(filename);
		bis = new BufferedInputStream(fis);
		DataOutputStream toRemote = new DataOutputStream(sock.getOutputStream());

		while ((bytesRead = bis.read(byteBuffer, 0, BUFF_SIZE)) > -1) {
			toRemote.write(byteBuffer, 0, bytesRead);
			allBytes += bytesRead;
		}
		toRemote.flush();

		System.out.println("File " + filename + " uploaded (" + allBytes
				+ " bytes sent)");

		fis.close();
		bis.close();
		sock.close();

	}
}
