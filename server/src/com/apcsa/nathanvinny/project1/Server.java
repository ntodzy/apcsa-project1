package com.apcsa.nathanvinny.project1;

import com.sun.jdi.ThreadReference;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Vector;
import java.time.Instant;
// thanks stackoverflow i<3u


public class Server {
    final private int PORT = 57620;
    private ServerSocket ss;
    private Vector<ClientConnection> players = new Vector<ClientConnection>();
    private int playerCnt = 0;

    public Server() {
        try {
            ss = new ServerSocket(PORT);
            System.out.printf("Server starting on port %d%n", PORT);
        } catch (IOException e) {
            System.out.print("Server did not start.");
            e.printStackTrace();
        }
    }

    public void acceptConnections() {

        try {
            while(true) {
                Socket s = ss.accept(); // user

                if (players.size() <2) {
                    System.out.println("Player connected" + s);
                    if(players.size() == 1) {
                        System.out.println("Sending Message to Player 1.");
                        players.get(0).dout.writeUTF("Player 2 Connected");
                    }

                    ClientConnection conn = new ClientConnection(s);
                    players.addElement(conn);

                    Thread t = new Thread(players.get(players.indexOf(conn)));
                    t.start();
                } else {
                    System.out.println("Bouncing Connection from" + s);
                    DataOutputStream dout = new DataOutputStream(s.getOutputStream());

                    dout.writeInt( 503);
                    dout.flush();
                    s.close();
                }

            }

        } catch (IOException ex) {
            System.out.println("IOException in  acceptConnections()");
            ex.printStackTrace();
        }
    }

    public class ClientConnection implements Runnable {
        private Socket socket;
        private DataInputStream din;
        private DataOutputStream dout;
        private final int uuid;

        // Constructor
        public ClientConnection(Socket s) {
            this.socket = s;
            this.uuid = playerCnt++;

            try {
                din = new DataInputStream(socket.getInputStream());
                dout = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.out.print("IOEXCEPTION in ClientConnection");
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                dout.writeInt(200);
                dout.writeInt(this.uuid);
                dout.flush();

                while (!socket.isClosed()) {
//                    System.out.println("Socket is not closed.");
//                    System.out.println("Sending continue.");
                    dout.writeInt(100); dout.flush();

                    if (din.readInt() == 100) {
                        // Start Game Loop
                        String input = din.readUTF();
                        System.out.printf("Player %d: %s%n", this.uuid, input);
                        // End Game Loop
                    } else {
                        players.remove(this.uuid); playerCnt--;
                        if (players.size() == 1) {
                            ClientConnection opp = players.get(0);
                            System.out.println("Disconnecting " + opp);
                            opp.dout.writeInt(205);
                            opp.dout.flush();
                        }
                        System.out.println(players.toString());
                        this.socket.close();
                    }
                }


            } catch (SocketTimeoutException e) {
                System.out.println(this.uuid + "'s Socket Timed Out");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) throws IOException {
        Server lobby = new Server();
        lobby.acceptConnections();
    }

}
