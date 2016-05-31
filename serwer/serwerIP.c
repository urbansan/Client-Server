#include <stdio.h> /* printf(), fprintf() */
#include <sys/socket.h> /* socket(), bind(), connect() */
#include <arpa/inet.h> /* sockaddr_in, inet_ntoa() */
#include <stdlib.h> /* atoi() */
#include <string.h> /* memset() */
#include <unistd.h> /* read(), write(), close() */
#include <sys/un.h>
#include <fcntl.h>
#include <errno.h>
#include "serwerIP.h"


int main(int argc, char *argv[])
{
	if(argc != 2){
		puts("argv[1] = listnienig port. No other parameters required");
		exit(1);
	}
	
	int port = atoi(argv[1]);
	if(port > 65535 || port < 1024){
		puts("Invalid Port. Should be between 1024 and 65535"); exit(1);
	}

	int listen_fd;
	int comm_fd;
	struct sockaddr_in serwer_sockaddr;
	struct sockaddr_in klient_sockaddr;
	pthread_t thread;

	listen_fd = createListeningSocket((struct sockaddr_in *) &serwer_sockaddr, port);
	
	if (listen(listen_fd, MAXKOLEJKA) < 0)
	{ perror("listen() - nie udal sie nasluch"); exit(1); }
	puts("nasluchuje");
	
	
	socklen_t klientDl = sizeof(klient_sockaddr);
	for(;;){
		if ((comm_fd = accept(listen_fd, (struct sockaddr *) &klient_sockaddr, &klientDl)) < 0)
		{ perror("accept() - nie udal sie nawiazac comm_fd"); exit(1); }
		
		printf("Connected = %s\n", inet_ntoa(klient_sockaddr.sin_addr));
		
		if (pthread_create(&thread, NULL, threadFunction, (void *) comm_fd) != 0){
			perror("pthread_create()"); exit(1);
		}
	}

	close(listen_fd);

	puts("Communication closed");
	return 0;
}

void *threadFunction(void *arg){
	pthread_detach(pthread_self());
	int comm_fd = (int) arg;
	ReactToOption(comm_fd);
	shutdown(comm_fd, SHUT_RDWR);

	return (NULL);
} 

void sendList(int comm_fd){
	char pathToSpecificFile[1024];
	char pathToChosenFile[1024];
	char *lista[30];
	char user[22];
	memset(user,'\0',sizeof(user));
	
	struct sockaddr_in adres;
	
	socklen_t dl_adres=sizeof(adres);
	if (getpeername(comm_fd, (struct sockaddr *) &adres, &dl_adres) != 0){			
		perror("getpeername()"); exit(1);
	}
	sprintf(user,"%s.txt",inet_ntoa(adres.sin_addr));
	
	memset(lista,0,sizeof(lista));
	memset(&pathToSpecificFile,0,sizeof(pathToSpecificFile));
	memset(&pathToChosenFile,0,sizeof(pathToChosenFile));
	
	//			  2) stworzenie pliku z lista
	readSharedDir(lista);
	strcpy(pathToSpecificFile, "./userSpecific/");
	strcat(pathToSpecificFile, user); //stworzenie pelnej sciezki do pliku
	saveListaToFile(lista, (char *)&pathToSpecificFile);
	
	int i = 0;
	while(lista[i]){
		free(lista[i]);
		++i;
	}
	
	//			  3) wysylanie pliku z lista plikow do pobrania
	copy_file2fd((char *)&pathToSpecificFile, comm_fd);
}

int ReactToOption(int comm_fd)
{
	
	char optionBuffer[BUFWE];
	memset(optionBuffer,'\0',BUFWE);
	int otrzTekstDl;

	puts("Waiting for choosing an option");

	while((otrzTekstDl = read(comm_fd, optionBuffer, BUFWE)) <= 0){
		if (otrzTekstDl < 0){ perror("read() - nie udalo się"); exit(1); }
	}

	
	int option = (int) optionBuffer[0];
	printf("ilosc odczytanych danych %d\n", otrzTekstDl);
	printf("opcja: %d\n", option);
	if(option == 0){
		printf("performing option: \"delete file\"\n");

		while((otrzTekstDl = read(comm_fd, optionBuffer, BUFWE)) <= 0){
			if (otrzTekstDl < 0){ perror("read() - nie udalo się"); exit(1); }
		}
		printf("ilosc odczytanych danych %d\n", otrzTekstDl);
		printf("Odebrany string do usuniecia %s\n", optionBuffer);
		
		if(strcmp("wrongFile", optionBuffer)){

		char pathToFile[1024];
		
		strcpy(pathToFile, "./shared/");
		strcat(pathToFile, optionBuffer); //stworzenie pelnej sciezki do pliku
		unlink(pathToFile);
		printf("deleted file: %s\n", pathToFile);

		}else
		puts("wrongFile has occured");
	}
	else
	
if(option == 1){
		printf("performing option: \"send list\"\n");

		if(strcmp("wrongFile", optionBuffer)){
			sendList(comm_fd);	
			
		}else
		puts("wrongFile has occured");

	}
	else
	if(option == 2){
		printf("performing option: \"download\"\n");

		memset(optionBuffer,'\0',BUFWE);
		while((otrzTekstDl = read(comm_fd, optionBuffer, BUFWE)) <= 0){
			if (otrzTekstDl < 0){ perror("read() - nie udalo się"); exit(1); }
		}
		// optionBuffer[otrzTekstDl] = '\0';
		printf("ilosc odczytanych danych %d\n", otrzTekstDl);
		if(strcmp("wrongFile", optionBuffer)){

			// zlaczam plik ze sciezka
			char pathToChosenFile[1024];    
			strcpy(pathToChosenFile, "./shared/");
			strcat(pathToChosenFile, optionBuffer); //stworzenie pelnej sciezki do pliku
			copy_file2fd((char *)&pathToChosenFile, comm_fd);
			
		}else
		puts("wrongFile error");

	}else   
	
	if(option == 3){
		printf("performing option: \"upload\"\n");

		memset(optionBuffer,'\0',BUFWE);
		while((otrzTekstDl = read(comm_fd, optionBuffer, BUFWE)) <= 0){
			if (otrzTekstDl < 0){ perror("read() - nie udalo się"); exit(1); }
		}
		printf("ilosc odczytanych danych %d\n", otrzTekstDl);
		printf("Odebrany string do uploadu %s\n", optionBuffer);
		
		if(strcmp("wrongFile", optionBuffer)){
			
			char pathToChosenFile[1024];
			strcpy(pathToChosenFile, "./shared/");
			strcat(pathToChosenFile, optionBuffer); //stworzenie pelnej sciezki do pliku
			copy_fd2file(comm_fd, (char *)&pathToChosenFile);
			
		}else
		puts("wrongFile has occured");

	}
	else     {
		struct sockaddr_in adres;
		char pathToSpecificFile[1024];
		char user[22];
		memset(user,'\0',sizeof(user));	
		
		socklen_t dl_adres=sizeof(adres);
		if (getpeername(comm_fd, (struct sockaddr *) &adres, &dl_adres) != 0){
			perror("getpeername()");
			exit(1);
		}
		
		sprintf(user,"%s.txt",inet_ntoa(adres.sin_addr));
		strcpy(pathToSpecificFile, "./userSpecific/");
		strcat(pathToSpecificFile, user); //stworzenie pelnej sciezki do pliku
		unlink(pathToSpecificFile);
		printf("deleting specific file: %s\nDisconnecting", pathToSpecificFile);
		return -1;
	}
	return 0;
}


int saveListaToFile(char *lista[], char *filename){
	FILE *listaDoPliku = fopen(filename, "w");
	int i = 0;
	
	
	while(lista[i]){

		fprintf(listaDoPliku,"%d\t%s\n",i, lista[i]);
		++i;
	}

	fclose(listaDoPliku);
	return 0;
}

int readSharedDir(char *lista[]) {
	DIR * katalog;
	struct dirent * pozycja;
	int i = 0;
	if (!(katalog = opendir("./shared"))) {
		perror("opendir");
		return 1;
	}

	errno = 0;
	while ((pozycja = readdir(katalog))) {
		// puts(pozycja->d_name);
		if(strcmp(pozycja->d_name, ".") &&  strcmp(pozycja->d_name, "..")){
			lista[i] = (char *)malloc(sizeof(pozycja->d_name));
			memcpy(lista[i], pozycja->d_name, sizeof(pozycja->d_name));
			++i;
		}
		errno = 0;
	}
	if (errno) {
		perror("readdir");
		return 1;
	}
	closedir(katalog);
	return 0;
}

int createListeningSocket(struct sockaddr_in *serwer_sockaddr, int port){

	int skarpeta_serw;

	if ((skarpeta_serw = socket(PF_INET, SOCK_STREAM, 0)) < 0)
	{ perror("socket() - nie udalo się"); exit(1); }
	puts("stworzylem gniazdo");
	

	memset(serwer_sockaddr, 0, sizeof(struct sockaddr_in));
	serwer_sockaddr->sin_family = AF_INET;
	serwer_sockaddr->sin_addr.s_addr = htonl(INADDR_ANY);
	serwer_sockaddr->sin_port = htons(port);
	
	puts("utworzylem strukture adresowa");
	

	if (bind(skarpeta_serw, (const struct sockaddr *) serwer_sockaddr, sizeof(struct sockaddr_in)) < 0)
	{ perror("bind() - nie udalo się"); exit(1); }

	puts("spialem gniazdo z portem");
	return skarpeta_serw;
}



int copy_file2fd(char *path, int upload_fd)
{

	int fd_file;
	
	if ( (fd_file=open(path, O_RDONLY)) == -1 )
	{perror("cannot open file"); exit(1);}

	cp(fd_file, upload_fd);

	if ( close(fd_file) == -1 )
	{perror("Error closing files");exit(1);}
	
	if ( close(upload_fd) == -1 )
	{perror("Error closing files");exit(1);}
	printf("Uploaded file: %s\n", path);

	return 0;
}

int copy_fd2file(int download_fd, char *path)
{
	int fd_file;

	if ( (fd_file=creat( path, COPYMODE)) == -1 )
	{perror( "Cannot creat");exit(1);}
	cp(download_fd, fd_file);

	if ( close(fd_file) == -1 )
	{perror("Error closing files");exit(1);}
	
	if ( close(download_fd) == -1 )
	{perror("Error closing files");exit(1);}
	printf("Downloaded file: %s\n", path);
	return 0;
}


void cp(int fd1, int fd2){

	int n_chars;
	char buf[BUFFERSIZE];
	
	while((n_chars = read(fd1 , buf, BUFFERSIZE)) > 0 ){
		if ( write( fd2, buf, n_chars ) != n_chars )
		{perror("write() - nie udalo się"); exit(1);}
		// printf("Przeslano bajtow: %d, ", n_chars);
	}
	if ( n_chars == -1 )
	{perror("read() - nie udalo się"); exit(1);}
	puts("");

}
