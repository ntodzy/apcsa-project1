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
    public Vector<ClientConnection> players = new Vector<ClientConnection>();
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
                    System.out.println("[Server] Player connected" + s);

                    // Create a new collection and add it to the vector so we can cleanly access it later..
                    ClientConnection conn = new ClientConnection(s);
                    players.addElement(conn);

                    // Start threads
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
        private ClientConnection opp;

        // Constructor
        public ClientConnection(Socket s) {
            this.socket = s;
            this.uuid = playerCnt++;

            if (this.uuid == 1) {
                this.opp = players.get(0); // get opponent
            }

            try {
                this.din = new DataInputStream(socket.getInputStream());
                this.dout = new DataOutputStream(socket.getOutputStream());
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
                // Update player 0 opponent now that we have one and tell them the game is starting!

//                if(players.size() == 2) {
//                    System.out.println("[Server] Sending Message to Player 1.");
//                    players.get(0).opp = players.get(1);
//                    players.get(0).dout.writeUTF("Player 2 Connected");
//                }

                dout.flush();

                while (!socket.isClosed()) {
//                    System.out.println("Socket is not closed.");
//                    System.out.println("Sending continue.");
                    dout.writeInt(100); dout.flush(); // Server says hey, ill continue.

                    if (this.din.readInt() == 100) { // if the client accepts to continue the process run game
                        // Start Game Loop
                        String input = din.readUTF();

                        if ( inputs[this.uuid] == -1 && input.equals("")) {
                            continue;
                        }

                        inputs[this.uuid] = Integer.parseInt(input);

                        if ( inputs[0] == -1 || inputs[1] == -1 ) {
                            System.out.printf("[Client %d] Missing Information", this.uuid);
                            this.dout.writeInt(102); this.dout.flush();
                        } else {
                            System.out.printf("[Client %d] All Inputs Collected", this.uuid);
                            this.dout.writeInt(200); this.dout.flush();
                        }


                        while (inputs[0] == -1 || inputs[1] == -1) {System.out.println("yo im in a while loop");} // block loop
                        System.out.printf("[Client %d] Starting Logic", this.uuid);

                        if (inputs[0] < inputs[1]) { // player 0 wins
                            this.dout.writeInt(601); this.dout.flush();
                            disconnect(this);

                        } else if (inputs[0] > inputs[1]) { // player 1 wins
                            this.dout.writeInt(600); this.dout.flush();
                            disconnect(this);

                        } else {// Tied
                            System.out.println("Tie");
                            this.dout.writeInt(602); this.dout.flush();
                            disconnect(this);
                            System.out.println("Tie2");
                        }


                        System.out.println(Arrays.toString(inputs));
//                        System.out.printf("Player %d: value %d%n", this.uuid, inputs[this.uuid]);
                        // End Game Loop
                    } else { // otherwise start the disconnection process.
                        disconnect(this);
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

    public void disconnect(ClientConnection client) throws IOException {
        System.out.println("Disconnecting " + this);
        client.socket.close();

        players.remove(client); playerCnt--;
        System.out.println(players.toString());
    }

    public static void main(String[] args) throws IOException {
        Server lobby = new Server();
        lobby.acceptConnections();
    }

}
