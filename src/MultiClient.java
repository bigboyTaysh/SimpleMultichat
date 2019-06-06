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

                out.write((data).getBytes());
                out.write("\n".getBytes());// dokladanie znak�w konca wiercza

                k = 0;
                sb.delete(0, sb.length());
                // potwierdzenie loginu
                //czytanie ze strumienia
                while ((k = in.read()) != -1 && k != '\n')
                    sb.append((char) k);

                // sprawdzenie komunikatu od serwera
                newLogin = sb.toString().trim();
                if(newLogin.toLowerCase().equals("true")){
                    System.out.println("Pomyślnie zalogowano!");
                    userName = false;
                } else {
                    System.out.println("Podany login istnieje!");
                    userName = true;
                }
            }while(userName);

            login = data;

            // lista użytkowników
            System.out.println("Lista zalogowanych użytkowników:");
            k = 0;
            sb.delete(0, sb.length());

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
            System.out.println("Format wiadomości -> odbiorca:treść wiadomości");

            while (true) {
                //Czytanie danych ze standardowego urzadzenia wejscia(klawiatury) po linii
                data = fromKeyboard.readLine();

                //TimeUnit.SECONDS.sleep(1);
                //KONIEC FAZA 1.
                out.write((login + ":" + data).getBytes());
                out.write("\r\n".getBytes());// dokladanie znak�w konca wiercza
                if(data.contains("/getFile")){
                    int bytesRead;
                    int current = 0;

                    while(true) {

                        DataInputStream clientData = new DataInputStream(in);

                        String fileName = clientData.readUTF();
                        fileName = login + fileName;
                        OutputStream output = new FileOutputStream(fileName);
                        long size = clientData.readLong();
                        byte[] buffer = new byte[1024];
                        while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)
                        {
                            output.write(buffer, 0, bytesRead);
                            size -= bytesRead;
                        }

                        // Closing the FileOutputStream handle
                        clientData.close();
                        output.close();
                    }
                }
            }
        } catch (UnknownHostException uhe) {
            System.err.println(uhe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    public void run() {
        //wysylanie danych strumieniem wyjsciowym do serwera
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

                    String message = sb.toString().trim();
                    //sprawdzenie wiadomości od serwera
                    //jeśli /loggedIn dodanie do listy wiadomosci, informacja o zalogowanym uzytkowniku
                    if (message.equals("/loggedIn")) {
                        k = 0;
                        sb.delete(0, sb.length());

                        while ((k = in.read()) != -1 && k != '\n')
                            sb.append((char) k);
                        wiadomosci.add(sb.toString().trim() + " zalogował się!");
                    } else {
                        wiadomosci.add(message);
                    }
                }

                while (!wiadomosci.isEmpty()) {
                    String[] parts = wiadomosci.get(0).split(":");
                    if(parts.length == 2){
                        System.out.println("Wiadomość od " + parts[0] + ": " + parts[1]);
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