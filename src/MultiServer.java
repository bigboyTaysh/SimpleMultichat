import java.io.*;
import java.net.*;
import java.util.*;

public class MultiServer {

    /**
     * Lista watkow obslugujacych klientow
     */
    List<ClientThread> v = new ArrayList<>();
    private int id;


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
            id = v.size() - 1;
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
                int k = 0;
                StringBuffer sb = new StringBuffer();
                boolean userName;
                String login = null;


                do{
                    userName = true;
                    k = 0;
                    sb.delete(0, sb.length());
                    // login użytkownika i potwierdzenie loginu
                    //czytanie ze strumienia
                    while ((k = in.read()) != -1 && k != '\n')
                        sb.append((char) k);

                    //wysłanie loginu klienta
                    for (int i = 0; i < v.size(); i++) {
                        if (v.get(i).getName().equals(sb.toString().trim())) {
                            userName = false;
                        }
                    }

                    out.write((Boolean.toString(userName)).getBytes());
                    out.write("\n".getBytes());
                }while(!userName);


                login = sb.toString().trim();
                v.get(id).setName(login);


                // ######### lista użytkowników oprócz użytkowników bez nazwy (endList)
                for (int i = 0; i < v.size(); i++) {
                    if (!(v.get(i).getName().equals(login)) && !(v.get(i).getName().contains("Thread"))) {
                        out.write((Integer.toString(i + 1) + ". " + v.get(i).getName() + "\n").getBytes());
                    }
                }
                out.write("endList\n".getBytes());

                //wysłanie wiadomości o zalogowanym użytkowniku
                sendToAll(("/loggedIn").getBytes());
                sendToAll(login.getBytes());


                while (isInterrupted() == false) {
                    k = 0;
                    sb = new StringBuffer();
                    //czytanie ze strumienia
                    while ((k = in.read()) != -1 && k != '\n')
                        sb.append((char) k);

                    //wysylanie do użytkownika czatu
                    System.out.println(sb);

                    String[] parts = sb.toString().trim().split(":");
                    if(parts.length == 3 && !(parts[1].equals("/showFiles")) && !(parts[1].equals("/getFile"))){
                        String user = parts[0];
                        String userTo = parts[1];
                        String data = parts[2];

                        sendTo(user, userTo, data);
                    } else if (parts[1].equals("/showFiles") && parts.length == 2){
                        String user = parts[0];
                        showFiles(user);

                    } else if (parts[1].equals("/getFile")){
                        String user = parts[0];
                        String file = parts[2];
                        System.out.println(user);
                        System.out.println(file);

                        int idUser = 0;
                        for(int i = 0; i<v.size(); i++){
                            if(v.get(i).getName().equals(user)){
                                idUser = i;
                            }
                        }

                        getFile(user,file);

                    }
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

        void showFiles(String user) {
            File folder = new File("C:\\Users\\wolak\\OneDrive\\SEM 2\\TS\\SimpleMultichat\\ServerFiles");
            File[] listOfFiles = folder.listFiles();
            int idUser = 0;

            for(int i = 0; i<v.size(); i++){
                if(v.get(i).getName().equals(user)){
                    idUser = i;
                }
            }

            for (int i = listOfFiles.length-1; i >= 0 ; i--) {
                if (listOfFiles[i].isFile()) {
                    System.out.println("File "+ (i) +". " + listOfFiles[i].getName());
                    v.get(idUser).send((((i) +" " + listOfFiles[i].getName()).getBytes()));
                } else if (listOfFiles[i].isDirectory()) {
                    System.out.println("Directory " + listOfFiles[i].getName());
                }
            }
        }

        private void getFile(String user, String file) throws IOException {
            out.write("/getFile".getBytes());


            //Send file
            File myFile = new File("C:\\Users\\wolak\\OneDrive\\SEM 2\\TS\\SimpleMultichat\\ServerFiles\\" + file);
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
            os.close();
            dos.close();
        }
    }
}