/** \file serwerIP.h
    \brief Plik nagłówkowy
    
    Plik zawiera spis stworzonych funkcji, makr oraz pozastandarodwych bibliotek wykorzystanych przez projekt.
*/

#include <netdb.h>
#include <dirent.h>
#include <pthread.h>

/*! \def MAXKOLEJKA
    \brief Maksymalna kolejnka nasłuchiwanych przez gniazdo nasłuchujące.
*/
#define MAXKOLEJKA 5

/*! \def BUFWE
    \brief Rozmiar bufora dla przesyłanych Stringów - nazw plików.
*/
#define BUFWE 80

/*! \def BUFFERSIZE
    \brief Rozmiar bufora przesyłu danych (download, upload).
*/
#define BUFFERSIZE  4096

/*! \def COPYMODE
    \brief Prawa dostępu dla tworzonych plików.
*/
#define COPYMODE  0664

/** \fnv int main(int argc, char *argv[]);
\brief Główna funkcja serwera. 
\param argv[1] - Port nasłuchujący.

Tworzy gniazdo nasłuchujące i przyjmuje kolejnych klientów. Obsługa zapytań jest wykonywana w nastepujący sposób:
1. Tworzenie gniazda komunikacyjnego klienta i przekazania go do odrębnego wątku.
2. Uruchomienie funkcji wątku threadFunction().
3. Zamykanie gniazda.

Każde gniazdo żyje raz na daną opcja dla jednego klienta.
\returns 0 on success.
*/
int main(int argc, char *argv[]);

/** \fn createListeningSocket(struct sockaddr_in *serwer_sockaddr, int port);
\param serwer_sockaddr - wskażnik do struktury adresowej.
\param port - port przyjmowania zgłoszeń.
\returns Zwraca deskryptor gniazda nasłuchującego.
*/
int createListeningSocket(struct sockaddr_in *serwer_sockaddr, int port);

/** \fn void *threadFunction(void *arg);
\brief Funkcja wątku osbługująca klienta.
\param arg - deskryptor gniazda komunikacyjnego.

Przechodzi w stan odłączenia (detached) oraz wykonuje funkcję \a ReactToOption. Zamyka deskryptor gniazda komunikacyjnego za pomocą funkcji \a shutdown().
*/
void *threadFunction(void *arg);

/** \fn ReactToOption(int comm_fd);
\brief Funkcja interpretująca komunikaty od klienta.
\param comm_fd - gniazdo komunikacyjne

Funkcja komunikuje się z klientem w 4ch etapach:
1. Odbiór czynności, którą chce wykonać klient (send list, download, upload, delete file, close connection).
2. Dla opcji upload i download odbiera od klienta nazwę pliku i uwzględnia błędne przesłanie nazwy pliku.
3. Dla opcji \a upload, \a download oraz \a send \a list wybiera odpowiednią funkcję kopiującą do/ zapisującą z strumienia. 

Opcja \a close \a connection jest wybierana w przypadku nie wybrania żadnych z wyżej wymienionych.
\returns 0 on success.
*/
int ReactToOption(int comm_fd);

/** \fn void sendList(int comm_fd)
\brief Tworzy listę plików serwera i wysyła ją do klienta.
\param comm_fd deskryptor gniazda.

1. Tworzy plik specyficzny dla każdego klienta opartego na jego adresie IP i zapisuje go do katalogu \a "/userSpecific".
2. Do pliku zapisuje listę plików udostępnianych z katalogu ./shared/.
3. Wysyła plik do klienta.
*/
void sendList(int comm_fd); 

/** \fn void readSharedDir(char *lista[])
\brief Zapisuje do tablicy stringów \a lista listę plików z katalogu \a "/shared". Wykorzystuje ją sendList().
\param *lista[] - tablica stringów.
\returns 0 on success.
*/
int readSharedDir(char *lista[]);

/** \fn int saveListaToFile(char *lista[], char *filename);
\brief Zapisuje listę stringów do pliku \a "/shared/filename". wykorzystuje ją sendList().
\param *lista[] - tablica stringów z listą katalogów.
\param *filename[] - scieżka względna do pliku z listą.
*/
int saveListaToFile(char *lista[], char *filename);

/** \fn int copy_file2fd(char *path, int fd_target);
\brief Tworzy deskryptor scieżki \a path i kopiuje zawartość do deskryptora celu. Oba deskryptory są zamykane.
\param path - ścieżka do pliku.
\param fd_target - deskryptor celu, zazwyczaj deskryptor gniazda komunikacyjnego.
\returns 0 on success.
*/
int copy_file2fd(char *path, int fd_target);

/** \fn int copy_fd2file(int fd_source, char *path);
\brief Tworzy deskryptor scieżki \a path i kopiuje zawartość z deskryptora źródła. Oba deskryptory są zamykane.
\param fd_source - deskryptor źródła, zazwyczaj deskryptor gniazda komunikacyjnego.
\param path - ścieżka do pliku.
\returns 0 on success.
*/
int copy_fd2file(int fd_source, char *path);

/** \fn void cp(int fd1, int fd2);
\brief Kopiuje dane wykorzystując descryptory
\param fd1 - deskryptor strumienia kopiowanego.
\param fd2 - deskryptor strumienia zapisywanego.
*/
void cp(int fd1, int fd2);




