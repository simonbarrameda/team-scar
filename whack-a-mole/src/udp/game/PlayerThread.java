package udp.game;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import config.GameConfig;
import main.GameState;
import main.Player;
import state.Play;

public class PlayerThread implements Runnable {

	private GameState game;
	private String address;
	private int port;
	int i = 20;
	
	public PlayerThread(GameState game, String address, int port) {
		this.game = game;
		this.address = address;
		this.port = port;
	}
	
	public void update(String data) {
		data = data.trim();
		
		String[] type = data.split("_");
		
		// Update Player
		for(String temp: type[0].split(";")) {
			String[] player = temp.split(",");

			int id    = Integer.parseInt(player[1]);
			int score = Integer.parseInt(player[2]);
			int x     = Integer.parseInt(player[3]);
			int y     = Integer.parseInt(player[4]);

			Player p = game.getPlayers().get(id);
			p.setScore(score);
			p.setCoords(x, y);
		}
		
		// Update Board
		for(String temp: type[1].split(";")) {
			String[] box = temp.split(",");
			
			int index = Integer.parseInt(box[0]) % 21;
			int state = Integer.parseInt(box[1]);
			
			game.getMoles().get(index).setUp(state);
			
		}
	}
	
	public synchronized void send(String message) throws IOException {
        byte[] buffer  = new byte[256];
		InetAddress addr = InetAddress.getByName(GameConfig.SERVER_NAME);
        DatagramSocket socket = new DatagramSocket();
        
        buffer = message.getBytes();
		DatagramPacket packet = new DatagramPacket(
        		buffer,
        		buffer.length,
        		addr,
        		GameConfig.PORT
		);

        socket.send(packet);
	}
	
	@Override
	public void run() {
		System.out.println("Client running");
		try {
			InetAddress addr = InetAddress.getByName(address);
	        byte[] buffer  = new byte[256];
	       
	        MulticastSocket socket = new MulticastSocket(port);
	        socket.joinGroup(addr);
	        
	        while(true) {
	        	DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		    	socket.receive(packet);
		       
		    	String data = new String(buffer, 0, buffer.length);
		        update(data);
	       }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}
}