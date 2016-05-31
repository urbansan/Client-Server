/** \file sharedClientMethods.java
    \brief Zawiera tylko implementacje interface'u sharedClientMethods.
*/
import java.io.IOException;
import java.net.Socket;

/** \interface sharedClientMethods
    \brief Wymagana implementacja dla projektu TCP Client
*/
public interface sharedClientMethods {

/** \brief Odbiera plik z lista plików z serwera, interpretuje i wczytuje do pamięci.
    \param sock - gniazdo połączeniowe.
*/
	public void getRemoteFileList(Socket sock);
    
/** \brief Pobiera listę udostępnianych plików z katalogu \a "shared_java".
    
*/   
	public void getLocalFileList() ;
    
/** \brief Sprawdza, czy wybrana opcja \a download/upload może spowodować nadpisanie wybranego pliku po drugiej stronie połączenia.
    \param position - numeryczny identyfikator pliku z danej listy.
    \param option - literał określający, którą listę należy przejrzeć.
    \throw jabelko - wyjątek zawiera wartość \a boolean \a isOverwrite, która przyjmuje wartość \a TRUE, kiedy w wyniku wybranej operacji zostanie nadpisany wybrany plik. Przyjmuje wartość \a FALSE w przeciwnym wypadku.
*/  
	public void checkIfOverwritingFiles(String position, String option) throws jabelko;
    
/** \brief Pobiera plik \a filename korzystając z połączonego gniazda \a sock.
    \param filename - nazwa ściąganego pliku.
    \param sock - gniazdo połączeniowe.
*/  
	public void downloadFile(String filename, Socket sock) throws IOException;
    
/** \brief Wysyła plik \a filename przez gniazdo sock.
    \param filename - nazwa wysyłanego plik.
    \param sock - gniazdo połączeniowe.
*/    
	public void uploadFile(String filename, Socket sock) throws IOException;
	
}
