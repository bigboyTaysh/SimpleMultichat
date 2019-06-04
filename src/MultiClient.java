import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MultiClient extends Thread {

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    List<String> wiadomosci = new ArrayList<String>();
    List<String> wiadomosci2 = new ArrayList<String>();
    private int id;


    public static void main(String[] args) {
        MultiClient client = new MultiClient();

        client.run2();
    }

    public void add(String data) {
        wiadomosci.add(data);
    }

    public void run2(){
        try {
            socket = new Socket("localhost", 2000);
            System.out.println("Connected To Server ...");
            //Pobieranie strumieni do gniazda
            in = socket.getInputStream();
            out = socket.getOutputStream();
            //Pobieranie strumienia do standardowego urzadzenia wejscia(klawiatury)
            //i buforowanie danych z niego przychodzacych

            int k = 0;
            StringBuffer sb = new StringBuffer();
            //czytanie ze strumienia
            while((k=in.read())!=-1 && k!='\n')
                sb.append(k);

            id = Integer.parseInt(sb.toString().trim());
            System.out.println("Id klienta: " + id);

            start();

            BufferedReader fromKeyboard = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Format wiadomości -> odbiorca:treść wiadomości");

            while (true) {
               // sleep(1000);
                //Czytanie danych ze standardowego urzadzenia wejscia(klawiatury) po linii
                String data = fromKeyboard.readLine();

                //TimeUnit.SECONDS.sleep(1);
                //KONIEC FAZA 1.
                out.write((id + ":" + data).getBytes());
                out.write("\r\n".getBytes());// dokladanie znak�w konca wiercza



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
                while(in.available() != 0) {
                    k = 0;
                    sb = new StringBuffer();
                    //czytanie ze strumienia
                    while ((k = in.read()) != -1 && k != '\n')
                        sb.append((char) k);

                    String parts1 = sb.toString().trim();

                    wiadomosci2.add(parts1);
                }

                while(!wiadomosci2.isEmpty()){
                    String[] parts = wiadomosci2.get(0).split(":");
                    System.out.println("Od: " + parts[0]);
                    System.out.println("Wiadomość: " + parts[1]);
                    wiadomosci2.remove(0);
                }

                sleep(500);

            }
        } catch (UnknownHostException uhe) {
            System.err.println(uhe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}