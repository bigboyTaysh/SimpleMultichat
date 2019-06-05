import java.io.*;
import java.net.*;
import java.util.*;

public class MultiServer {

    /**
     * Lista watkow obslugujacych klientow
     */
    List<ClientThread> v = new ArrayList<>();


    /**
     * Glowna metoda programu
     */
    public static void main(String[] args) {
        try {
            new MultiServer().runServer();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Metoda uruchamiajaca serwer
     */
    void runServer() throws IOException {
        //tworzenie gniazda serwera
        ServerSocket server = new ServerSocket(2000);

        System.out.println("Server run ... ");

        while (true) {

            //Akceptacja polaczenia;
            Socket socket = server.accept();
            System.out.println("New client");

            //Tworzenie watku obsugujacego klienta
            ClientThread thread = new ClientThread(socket);
            //dodawanie watku do listy
            v.add(thread);
        }
    }

    /**
     * Klasa dziedziczaca po Thread, obslugujaca klientow
     */
    class ClientThread extends Thread {

        Socket socket;
        InputStream in;
        OutputStream out;


        /**
         * @param socket - zaakceptowanie polaczenie z klientem
         */
        ClientThread(Socket socket) throws IOException {
            this.socket = socket;

            // pobieranie strumieni
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();

            //uruchamianie watku
            start();
        }

        /**
         * Nadpisana metoda w ciele kt�rej znajduja sie instrukcje
         * wymagajace wykonania w osobnym w�tku
         */
        public void run() {
            try {

                // login użytkownika i potwierdzenie loginu
                String login = null;
                int k = 0;
                StringBuffer sb = new StringBuffer();
                //czytanie ze strumienia
                while ((k = in.read()) != -1 && k != '\n')
                    sb.append((char) k);


                System.out.println(v.size());
                v.get(v.size() - 1).setName(sb.toString().trim());
                //wysłanie id klienta
                for (int i = 0; i < v.size(); i++) {
                    System.out.println(v.get(i).getName());
                    if (v.get(i).getName().equals(sb.toString().trim())) {
                        login = v.get(i).getName();
                    }
                }

                out.write(login.getBytes());
                out.write("\n".getBytes());

                // ######### lista użytkowników (endList)

                for (int i = 0; i < v.size(); i++) {
                    if (!(v.get(i).getName().equals(sb.toString().trim())) && !(v.get(i).getName().contains("Thread"))) {
                        out.write((Integer.toString(i + 1) + ". " + v.get(i).getName() + "\n").getBytes());
                    }
                }
                out.write("endList\n".getBytes());

                //wysłanie wiadomości o zalogowanym użytkowniku
                sendToAll(("loggedIn").getBytes());
                sendToAll(login.getBytes());


                while (isInterrupted() == false) {
                    k = 0;
                    sb = new StringBuffer();
                    //czytanie ze strumienia
                    while ((k = in.read()) != -1 && k != '\n')
                        sb.append((char) k);

                    //wysylanie do użytkownika czatu
                    System.out.println(sb);

                    String[] parts = sb.toString().split(":");
                    String user = parts[0];
                    String userTo = parts[1];
                    String data = parts[2];

                    sendTo(user, userTo, data);
                }


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Metoda wysylajaca odpowiedz do klienta
         *
         * @param data - tablica bajt�w z danymi
         */
        void send(byte[] data) {
            try {
                //wysylanie danych
                out.write(data);
                out.write("\r\n".getBytes());

            } catch (IOException e) {
                System.out.println(e);
            }
        }

        /**
         * Metoda wysylajaca dane do wszystkich klientow w wektorze
         *
         * @param data - tablica bajt�w z danymi.
         */
        void sendToAll(byte[] data) {
            //v.forEach(t -> t.send(data));
            for(int i=0; i<v.size(); i++){
                if(!(v.get(i).getName().equals(this.getName()))){
                    v.get(i).send(data);
                }
            }
        }

        void sendTo(String user, String userTo, String data) {
            for(int i = 0; i<v.size(); i++){
                if(v.get(i).getName().equals(userTo)){
                    v.get(i).send((user + ":" + data).getBytes());
                }
            }
        }
    }
}