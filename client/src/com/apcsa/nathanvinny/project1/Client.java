package com.apcsa.nathanvinny.project1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

// thanks stackoverflow i<3u
public class Client {
    private int id;
    private int oppId;
    private ServerConnection csc;

    public void connectToServer() {
        csc = new ServerConnection();
    }

    private class ServerConnection {

        private Socket socket;
        private DataInputStream din;
        private DataOutputStream dout;
        // Constructor
        public ServerConnection() {
            try {
                System.out.println("Trying to connect to server.");
                socket = new Socket("localhost", 57620);
                din = new DataInputStream(socket.getInputStream());
                dout = new DataOutputStream(socket.getOutputStream());

                // Setup some shiet

                if (this.din.readInt() == 503) {
                    System.out.println("Server is busy right now. Please try again later.");
                    socket.close();
                    return;

                } else {
                    System.out.println("Connected!");
                    id = this.din.readInt(); // should be the first thing the server sends!!
                    System.out.printf("Connected to server as user %d%n", id);

                    while (true) {
                        // Start Game Loop


                        // End Game Loop
                    }
                }

            } catch (UnknownHostException e) {
                System.out.print("Unknown Host Exception");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.print("IOEXCEPTION: in ServerConnection.");
                e.printStackTrace();
            }
        }

    }

    // Constructor
    public Client() {/*do nothing now*/}

    public static void main(String[] args) {
        Client client = new com.apcsa.nathanvinny.project1.Client();
        client.connectToServer();

    }
}
