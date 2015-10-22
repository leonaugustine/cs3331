import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class cdht {

	public static class PingUDP implements Runnable {
		private Thread t;
		private DatagramSocket pingSocketUDP;
		private int sucPeer1PORT;
		private int sucPeer2PORT;
		
		public PingUDP(DatagramSocket pingSocketUDP, int sucPeer1PORT, int sucPeer2PORT) {
			this.pingSocketUDP = pingSocketUDP;
			this.sucPeer1PORT = sucPeer1PORT;
			this.sucPeer2PORT = sucPeer2PORT;
		}


		@Override
		public void run() {
			try{
			while(true){

				Thread.sleep(200000);

				String message = "REQUESTPING";
				byte[] buffer = new byte[1024];
				buffer = message.getBytes();

				// Create a datagram packet to send as an UDP packet.
				DatagramPacket ping1 = new DatagramPacket(buffer, buffer.length, pingSocketUDP.getLocalAddress(), sucPeer1PORT);
				try {
					pingSocketUDP.send(ping1);
				} catch (IOException e) { e.printStackTrace();}
				
				// Create a datagram packet to send as an UDP packet.
				DatagramPacket ping2 = new DatagramPacket(buffer, buffer.length, pingSocketUDP.getLocalAddress(), sucPeer2PORT);
				try {
					pingSocketUDP.send(ping2);
				} catch (IOException e) { e.printStackTrace();}
			}
		}catch (Exception e) {

		}

		}

		public void start () {
            if (t == null) {
                t = new Thread (this); //tutorials point , works, REMOVE
                t.start ();
            }
       }

	}

	


    public static class PingPongUDPServer implements Runnable {
    	private Thread t;
    	private DatagramSocket pingSocketUDP;
		private int sucPeer1PORT;
		private int sucPeer2PORT;
		
		public PingPongUDPServer(DatagramSocket pingSocketUDP, int sucPeer1PORT, int sucPeer2PORT) {
			this.pingSocketUDP = pingSocketUDP;
			this.sucPeer1PORT = sucPeer1PORT;
			this.sucPeer2PORT = sucPeer2PORT;	
		}


		@Override
		public void run() {


			while(true){
				// TODO Auto-generated method stub
				//datagram to hold recideved udp packt
				DatagramPacket requestPacket =  new DatagramPacket(new byte[1024], 1024);
				try {
					//blocks until a udp packet is attained
					pingSocketUDP.receive(requestPacket);
				} catch (IOException e) { e.printStackTrace();}
				//rparsing the data recieved
				// String packetData = new String(requestPacket.getData());

				byte[] buffer = requestPacket.getData();
			    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(buffer);
			    InputStreamReader inputStream = new InputStreamReader(byteInputStream);
			    BufferedReader br = new BufferedReader(inputStream);
			    String receivedMsg = null;
			         try {
					  receivedMsg = br.readLine();
					 	} catch (IOException e) {e.printStackTrace(); System.out.printf("lol"); }
			 //    				// 	recievedMsg = br.readLine();
				// } catch (IOException e) {e.printStackTrace();}
				receivedMsg = receivedMsg.trim();

			    if(receivedMsg.equals("REQUESTPING")){
                    System.out.printf("A ping request message was received from Peer %d.\n", requestPacket.getPort() - 50000); 
                    byte[] buf = new byte[1024];
                    String msg = "RESPONSEPONG";

                    InetAddress peerHost = pingSocketUDP.getLocalAddress();
                    buf = msg.getBytes();
                    DatagramPacket reply = new DatagramPacket(buf, buf.length, peerHost, requestPacket.getPort());
                    try {
						pingSocketUDP.send(reply);
					} catch (IOException e) {	e.printStackTrace();}
                    
			    }else if(receivedMsg.equals("RESPONSEPONG")){
                    System.out.printf("A ping response message was received from Peer %d.\n", requestPacket.getPort() - 50000); 	
			    }
			}
			

		}


		

		public void start () {
            if (t == null) {
              	t = new Thread (this);
                t.start ();
            }
       }


    }


    public static class TCPFileServer implements Runnable {
    	private Thread t;
    	private ServerSocket serverSocketTCP;
    	private int sucPeer1PORT;
		private int currPeerPort;
		
		public TCPFileServer(ServerSocket serverSocketTCP, int sucPeer1PORT, int currPeerPort) {
			// TODO Auto-generated constructor stub
			this.serverSocketTCP = serverSocketTCP;
			this.sucPeer1PORT = sucPeer1PORT;
			this.currPeerPort = currPeerPort;	
			
		}

		@Override
		public void run() {

			Socket connectionSocket = null;
			try {
				connectionSocket = serverSocketTCP.accept();
			} catch (IOException e) {}
			BufferedReader inFromClient = null;
			try {
				inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			} catch (IOException e) {}
			

		    String clientSentence = null;
		    try {
			    clientSentence = inFromClient.readLine();

			} catch (IOException e) {}

			if(clientSentence.contains("MESSAGE")){
				System.out.println("File"+ Integer.parseInt(clientSentence.split(" ")[1]) + "is not stored here");
				System.out.println("A request message has been forwaded to my succesor" + sucPeer1PORT);
				TCPFileClientHandler tcpH = new TCPFileClientHandler(sucPeer1PORT,currPeerPort ,Integer.parseInt(clientSentence.split(" ")[1]));
				tcpH.start();
			}
			
		}

		public void start () {
            if (t == null) {
              	t = new Thread (this);
                t.start ();
            }
       }
    }

    public static class TCPFileClientHandler implements Runnable {
    	private Thread t;
    	private int fileH;
    	private int sucPeer1PORT;
		private int currPeerPort;

		public TCPFileClientHandler(int sucPeer1PORT, int currPeerPort, int fileH) {
			// TODO Auto-generated constructor stub
			this.sucPeer1PORT = sucPeer1PORT;
			this.currPeerPort = currPeerPort;	
			this.fileH = fileH;
		}

		@Override
		public void run() {
			
			// get server address
			String serverName = "localhost";

			InetAddress serverIPAddress = null;
			try {
			serverIPAddress = InetAddress.getLocalHost();
			} catch (IOException e) {}
			// get server port

			// create socket which connects to server
			Socket clientSocket =null;
			try {
				clientSocket = new Socket(serverIPAddress, sucPeer1PORT);

			// get input from keyboard
			String sentence = "MESSAGE" + " " + fileH + " " + currPeerPort + " " + sucPeer1PORT ;

			// write to server
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			outToServer.writeBytes(sentence);

			// close client socket
			clientSocket.close();
			} catch (IOException e) {}

			
		}
		
		public void start () {
            if (t == null) {
              	t = new Thread (this);
                t.start ();
            }
       }
    }




	public static void main(String[] args) throws Exception {

		int currentPeer = Integer.parseInt(args[0]);
        int sucPeer1 = Integer.parseInt(args[1]);
        int sucPeer2 = Integer.parseInt(args[2]);

        int currentPeerPORT = currentPeer + 50000;
        int sucPeer1PORT = sucPeer1 + 50000;
        int sucPeer2PORT = sucPeer2 + 50000;

        DatagramSocket pingSocketUDP = new DatagramSocket(currentPeerPORT);
        ServerSocket serverSocketTCP = new ServerSocket(currentPeerPORT);

        PingUDP pingT = new PingUDP(pingSocketUDP, sucPeer1PORT,sucPeer2PORT );
        pingT.start();

        PingPongUDPServer udpServer = new PingPongUDPServer(pingSocketUDP, sucPeer1PORT,sucPeer2PORT);
        udpServer.start();

        TCPFileServer tcpServer = new TCPFileServer(serverSocketTCP,sucPeer1PORT,currentPeerPORT);
        tcpServer.start();

 		while(true){
 			Scanner scanner = new Scanner(System.in);
 			String username = scanner.nextLine();
			if(username.contains("request")){
				int fileNameH = (Integer.parseInt(username.split(" ")[1]) + 1) % 256;
				System.out.println("request for " + fileNameH + " has beem sent to my successor");
				TCPFileClientHandler tcpH = new TCPFileClientHandler(sucPeer1PORT,currentPeerPORT,fileNameH);
				tcpH.start();
			}
		}	


	}
}



//input handler -- read input if req instantiate client handler
	//only first one to get input
//client handler -- specific tcp and sends request to servver
//tcp server -- recive request, make handler (pass stuff)