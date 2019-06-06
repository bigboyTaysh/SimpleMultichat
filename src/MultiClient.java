import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MultiClient extends Thread {

    List<String> wiadomosci = new ArrayList<String>();
    List<String> wiadomosci2 = new ArrayList<String>();
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private int id;
    private String login;


    public static void main(String[] args) {
        MultiClient client = new MultiClient();

        client.run2();
    }


    public void run2() {
        try {
            // połączenie z serwerem
            socket = new Socket("localhost", 2000);
            System.out.println("Connected To Server ...");

            //Pobieranie strumieni do gniazda
            in = socket.getInputStream();
            out = socket.getOutputStream();
            //Pobieranie strumienia do standardowego urzadzenia wejscia(klawiatury)
            //i buforowanie danych z niego przychodzacych


            // login użytkownika
            BufferedReader fromKeyboard = new BufferedReader(new InputStreamReader(System.in));
            int k = 0;
            StringBuffer sb = new StringBuffer();
            String data = null, newLogin = null;

            //sprawdzenie czy istnieje uzytkownik o podanej nazwie
            boolean userName = true;

            do {
                System.out.print("Podaj login: ");
                data = fromKeyboard.readLine();

                out.write((data).getBytes()); // wysłanie loginu do serwera
                out.write("\n".getBytes());// dokladanie znak�w konca wiercza

                k = 0;
                sb.delete(0, sb.length());
                // potwierdzenie loginu
                //czytanie ze strumienia
                while ((k = in.read()) != -1 && k != '\n')
                    sb.append((char) k);

                // sprawdzenie komunikatu od serwera
                newLogin = sb.toString().trim();
                if (newLogin.toLowerCase().equals("true")) {
                    System.out.println("Pomyślnie zalogowano!");
                    userName = false;
                } else {
                    System.out.println("Podany login istnieje!");
                    userName = true;
                }
            } while (userName);

            login = data;

            System.out.println("Przydatne komendy:");
            System.out.println("/showFiles - pokazuje dostępne pliki na serwerze");
            System.out.println("/getFile:(nazwa) - przesyła wybrany plik do użytkownika");
            System.out.println("/sendFile - otwiera możliwość wyboru pliku do przesłania na serwer");
            System.out.println("Format wiadomości -> odbiorca:treść wiadomości");

            // lista użytkowników
            System.out.println("Lista zalogowanych użytkowników:");
            k = 0;
            sb.delete(0, sb.length());

            // wczytywanie linii z uzytkownikami do momentu otrzymania komendy /endList
            do {
                sb.delete(0, sb.length());
                while ((k = in.read()) != -1 && k != '\n')
                    sb.append((char) k);

                if (!sb.toString().equals("endList")) {
                    System.out.println(sb.toString().trim());
                }
            } while (!sb.toString().equals("endList"));

            start();

            fromKeyboard = new BufferedReader(new InputStreamReader(System.in));

            // wysyłanie tekstu, komunikatów oraz plików do serwera
            while (true) {
                //Czytanie danych ze standardowego urzadzenia wejscia(klawiatury) po linii
                data = fromKeyboard.readLine();

                //wysłanie do serwera komunikat o treści (login aktualnego użytkownika):(treść)
                out.write((login + ":" + data).getBytes());
                out.write("\r\n".getBytes());// dokladanie znak�w konca wiercza

                // jeśli została wpisana komenda /sendFile otwira się możliwość wybrania pliku do przesyłu na serwer
                if (data.equals("/sendFile")) {
                    System.out.println("Lista plików:");

                    //pobranie z folderu klienta nazwy plików i wyśiwtlenie na konsolę
                    File folder = new File("C:\\Users\\wolak\\OneDrive\\SEM 2\\TS\\SimpleMultichat\\ClientFiles");
                    File[] listOfFiles = folder.listFiles();

                    for (int i = listOfFiles.length - 1; i >= 0; i--) {
                        if (listOfFiles[i].isFile()) {
                            System.out.println(listOfFiles[i].getName());
                        } else if (listOfFiles[i].isDirectory()) {
                            System.out.println("Directory " + listOfFiles[i].getName());
                        }
                    }

                    System.out.println("Podaj nazwę pliku do przesłania:");
                    data = fromKeyboard.readLine();

                    //Send file - otworzenie nowych strumieniu do przesyły plików
                    File myFile = new File("C:\\Users\\wolak\\OneDrive\\SEM 2\\TS\\SimpleMultichat\\ClientFiles\\" + data);
                    byte[] mybytearray = new byte[(int) myFile.length()];

                    FileInputStream fis = new FileInputStream(myFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    //bis.read(mybytearray, 0, mybytearray.length);

                    DataInputStream dis = new DataInputStream(bis);
                    dis.readFully(mybytearray, 0, mybytearray.length);

                    OutputStream os = socket.getOutputStream();

                    //Sending file name and file size to the server
                    DataOutputStream dos = new DataOutputStream(os);
                    dos.writeUTF(myFile.getName());
                    dos.writeLong(mybytearray.length);
                    dos.write(mybytearray, 0, mybytearray.length);
                    dos.flush();

                    //Sending file data to the server
                    os.write(mybytearray, 0, mybytearray.length);
                    os.flush();

                    //Closing socket
                }
            }
        } catch (UnknownHostException uhe) {
            System.err.println(uhe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    public void run() {
        //odbieranie danych strumieniem wejściowym od serwera
        try {
            while (true) {
                int k = 0;
                StringBuffer sb = new StringBuffer();
                // pobranie powiadomienia od serwera

                while (in.available() != 0) {
                    k = 0;
                    sb = new StringBuffer();

                    //czytanie ze strumienia
                    while ((k = in.read()) != -1 && k != '\n')
                        sb.append((char) k);

                    //dzielenie treści wiadomości od serwera na części
                    String message = sb.toString().trim();
                    String[] parts = message.split(":");

                    //sprawdzenie wiadomości od serwera
                    //jeśli /loggedIn dodanie do listy wiadomosci, informacja o zalogowanym uzytkowniku
                    if (message.equals("/loggedIn")) {
                        k = 0;
                        sb.delete(0, sb.length());

                        while ((k = in.read()) != -1 && k != '\n')
                            sb.append((char) k);
                        wiadomosci.add(sb.toString().trim() + " zalogował się!");

                        //jeśli /getFile odebranie przesłanego pliku
                    } else if (message.equals("/getFile")) {
                        int bytesRead;
                        int current = 0;
                        DataInputStream clientData = new DataInputStream(in);

                        //nadanie nazwy plikowi, dołączenie nazwy użytkownika do nazwy pliku odebranego ze strumienia clientData
                        String fileName = clientData.readUTF();
                        fileName = "Plik " + login + " " + fileName;
                        OutputStream output = new FileOutputStream("C:\\Users\\wolak\\OneDrive\\SEM 2\\TS\\SimpleMultichat\\ClientFiles\\" + fileName);

                        // pobranie długości pliku
                        long size = clientData.readLong();
                        byte[] buffer = new byte[1024];
                        // odbieranie bajtów pliku
                        while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                            output.write(buffer, 0, bytesRead);
                            size -= bytesRead;
                        }

                        // Closing the FileOutputStream handle
                        // jeśli ilość częsci podzielonego komunikatu od serwera == 3 i druga część to login użytkownika to znaczy że jest to wiadomość od innego użytkownika
                    } else if (parts.length == 3) {
                        if (parts[1].equals(login)) {
                            //dodanie do listy wiadomości do wyświetlenia
                            wiadomosci.add(message);
                        }
                        // jeśli ilość częsci podzielonego komunikatu od serwera == 1 i ta część jest równa /showFiles to należy pobierać linie tekstu z serwera
                    } else if (parts.length == 1) {
                        if (parts[0].equals("/showFiles")) {
                            k = 0;
                            sb = new StringBuffer();
                            do {
                                sb.delete(0, sb.length());
                                while ((k = in.read()) != -1 && k != '\n')
                                    sb.append((char) k);
                                message = sb.toString().trim();

                                // dodawanie do listy wiadomości dopóki nie przyjdzie komunikat /endShowFiles
                                if (!sb.toString().equals("/endShowFiles")) {
                                    wiadomosci.add(message);
                                }
                            } while (!sb.toString().equals("/endShowFiles"));
                        }
                    }
                }

                //wyśiwtlenie wiadomości z serwera, po wyświetleniu usunięcie wiadomości z listy
                while (!wiadomosci.isEmpty()) {
                    String[] parts = wiadomosci.get(0).split(":");
                    if (parts.length == 3) {
                        System.out.println("Wiadomość od " + parts[0] + ": " + parts[2]);
                        wiadomosci.remove(0);
                    } else {
                        System.out.println(parts[0]);
                        wiadomosci.remove(0);
                    }
                }
            }
        } catch (UnknownHostException uhe) {
            System.err.println(uhe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
}