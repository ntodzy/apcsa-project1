package com.apcsa.nathanvinny.project1;

import com.sun.jdi.ThreadReference;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Vector;
import java.time.Instant;
// thanks stackoverflow i<3u


public class Server {
    final private int PORT = 57620;
    private ServerSocket ss;
    private Vector<ClientConnection> players = new Vector<ClientConnection>();
    int[] inputs = {-1,-1}; // too be used later.

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

                // Some messy logic to send connection information between users.
                if (players.size() <2) {
                    System.out.println("Player connected" + s);
                    if(players.size() == 1) {
                        System.out.println("Sending Message to Player 1.");
                        players.get(0).dout.writeUTF("Player 2 Connected");
                    }

                    // Create a new collection and add it to the vector so we can cleanly access it later..
                    ClientConnection conn = new ClientConnection(s);
                    players.addElement(conn);

                    Thread t = new Thread(players.get(players.indexOf(conn)));
                    t.start();
                } else {
                    System.out.println("Bouncing Connection from" + s);
                    DataOutputStream dout = new DataOutputStream(s.getOutputStream());

                    dout.writeInt( 503); // 503 http code
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
            // on start of a new thread this is the function it runs.

            try {
                // Send 200 to client "handshake"
                dout.writeInt(200);
                dout.writeInt(this.uuid); // not really important at the moment.
                dout.flush();

                while (!socket.isClosed()) {
//                    System.out.println("Socket is not closed.");
//                    System.out.println("Sending continue.");
                    dout.writeInt(100); dout.flush(); // Server says hey, ill continue.

                    if (din.readInt() == 100) { // if the client accepts to continue the process run game
                        // Start Game Loop
                        String input = din.readUTF();

                        switch (input.toLowerCase()) {
                            case "rock", "r" -> inputs[this.uuid] = 0;
                            case "scissors", "s" -> inputs[this.uuid] = 1;
                            case "paper", "p" -> inputs[this.uuid] = 2;
                            default -> {
                                System.out.println("got bad response from client");
                                dout.writeInt(400);
                                dout.flush();
                                continue;
                            }
                        }

                        dout.writeInt(200); dout.flush();

                        System.out.println(Arrays.toString(inputs));
//                        System.out.printf("Player %d: value %d%n", this.uuid, inputs[this.uuid]);
                        // End Game Loop
                    } else { // otherwise start the disconnection process.
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
