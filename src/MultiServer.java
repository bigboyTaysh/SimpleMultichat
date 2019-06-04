import java.io.*;
import java.net.*;
import java.util.*;

public class MultiServer{
	
	/** 
	 *Lista watkow obslugujacych klientow
	 */
	List<ClientThread> v = new ArrayList<>();
	
	/**
	 *Metoda uruchamiajaca serwer
	 */
	void runServer() throws IOException{
		//tworzenie gniazda serwera
		ServerSocket server = new ServerSocket(2000);
		
		System.out.println("Server run ... ");
		
		while(true){
			
			//Akceptacja polaczenia;
			Socket socket =  server.accept();
			System.out.println("New client");
			
			//Tworzenie watku obsugujacego klienta
			ClientThread thread = new ClientThread(socket);
			//dodawanie watku do listy
			v.add(thread);
		}
		
	}
	
	/**
	 *Metoda wysylajaca dane do wszystkich klientow w wektorze
	 *@param data - tablica bajt�w z danymi.
	 */
	void sendToAll(byte[] data){
		v.forEach(t -> t.send(data));
	}

	/*
	void sendTo(int id, int idTo, byte[] data){
		byte[] id2 = String.valueOf(id).getBytes();
		byte[] destination = new byte[id2.length + data.length];
		System.arraycopy(id2, 0, destination, 0, id2.length);
		System.arraycopy(data, 0, destination, id2.length, data.length);

		v.get(idTo).send( destination);
	}
	*/

	void sendTo(int id, int idTo, String data){
		if(idTo <= v.size()){
			v.get(idTo).send((String.valueOf(id) + ":" + data).getBytes());
			// v.get(idTo).send("\n".getBytes());
		}
	}

	/**
	 *Glowna metoda programu
	 */	
	public static void main(String[] args){
		try{
			new MultiServer().runServer();
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	/**
	 *Klasa dziedziczaca po Thread, obslugujaca klientow
	 */
	class ClientThread extends Thread{
		
		Socket socket;
		InputStream in;
		OutputStream out;
		
		
		/**
		 *@param socket - zaakceptowanie polaczenie z klientem
		 */
		ClientThread(Socket socket)throws IOException{
			this.socket = socket;
			
			// pobieranie strumieni
			this.in = socket.getInputStream();
			this.out = socket.getOutputStream();

			//uruchamianie watku
			start();
		}
		
		/**
		 *Nadpisana metoda w ciele kt�rej znajduja sie instrukcje
		 *wymagajace wykonania w osobnym w�tku
		 */
		public void run(){
		    try{
                System.out.println(v.size()-1);
                //wysłanie id klienta
                out.write(v.size()-1);
                out.write("\n".getBytes());

				while(isInterrupted() == false){
					int k = 0;
					StringBuffer sb = new StringBuffer();
					//czytanie ze strumienia
					while((k=in.read())!=-1 && k!='\n')
						sb.append((char)k);
						
					//wysylanie do wszystkich uczestnik�w czatu
					System.out.println(sb);

					String[] parts = sb.toString().split(":");
					int id = Integer.parseInt(parts[0]);
					int idTo = Integer.parseInt(parts[1]);
					String data = parts[2];

					sendTo(id, idTo, data);
				}


			}catch(IOException e){
				e.printStackTrace();
			}finally{
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
	 *Metoda wysylajaca odpowiedz do klienta
	 *
	 *@param data - tablica bajt�w z danymi
	 */	
	void send(byte[] data){
		try{
			//wysylanie danych
			out.write(data);
			out.write("\r\n".getBytes());

		}catch(IOException e){
			System.out.println(e);
		}
	}
	}
}