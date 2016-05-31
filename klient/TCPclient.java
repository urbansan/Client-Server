/** \file TCPclient.java
    \brief Plik zawiera implementacje klasy abstrakcyjnej TCPclient oraz zaprojektowany wyjątek jabelko.
*/

import java.io.IOException;

/** \brief Klasa abstrakcyjna zawierająca wydmuszkę wymaganą do stworzenia klientaTCP.
*/
abstract class TCPclient {
	public int port; /// port
	public String server; ///adres kropkowy
	
	public TCPclient(int port, String server) { // Konstruktor
		this.port = port;
		this.server = server;
	}
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	
	abstract public int startService() throws IOException; /// Glowna metode klienta
}

/** \brief Klasa wyjątku wykorzystana przez metode sharedClientMethods.checkIfOverwritingFiles().
*/
class jabelko extends Throwable {
	private static final long serialVersionUID = 1L;
    
/** \brief Attribute which extends the Throwable class.
*/
	boolean isOverwrite;
	public jabelko(String mezg) {
		super(mezg);
	}
	public jabelko(boolean prawda) {
		isOverwrite = prawda;
	}
	
	public boolean getIsOverwrite(){
		return isOverwrite;
	}
}

