package com.apcsa.nathanvinny.project1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

// thanks stackoverflow i<3u
public class Client {
    private int id;
    private int oppId;
    private ServerConnection csc;
    public Scanner cmdLn;

    public void connectToServer(String HOST, int PORT) {
        csc = new ServerConnection(HOST, PORT);
    }

    private class ServerConnection {

        private Socket socket;
        private DataInputStream din;
        private DataOutputStream dout;


        // Constructor
        public ServerConnection(String HOST, int PORT) {
            try {
                System.out.println("Trying to connect to server.");
                socket = new Socket(HOST, 57620);
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

                    while (!socket.isClosed()) {
                        // Start Game Loop
                        switch (din.readInt()) {
                            case 205: System.out.println("Opponent Disconnected, disconnecting."); break;
                            default:;
                        }

                        String input = "";
                        if (cmdLn.hasNext()) {
                            switch (input = cmdLn.nextLine()) {
                                case "quit": dout.writeInt(99); dout.flush(); socket.close();
                                default: dout.writeInt(100); dout.flush();
                            }
                        } else {
                            System.out.println("NO INPUT");
                        } // do it or else ill fuck you up code.
                        System.out.println(input.replace(" ", "&nbsp"));
                        dout.writeUTF(input);
                        dout.flush();
                    }
                }

            } catch (UnknownHostException e) {
                System.out.print("Unknown Host Exception");
                e.printStackTrace();
            } catch (SocketException e) {
                System.out.print("Left Game.");
            }
            catch (IOException e) {
                System.out.print("IOEXCEPTION: in ServerConnection.");
                e.printStackTrace();
            }
        }

    }

    // Constructor
    public Client() {
        /*do nothing now*/
        cmdLn = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Client client = new com.apcsa.nathanvinny.project1.Client();

        final String ADDRESS;
        final int PORT;

        System.out.print("Hostname: ");
        ADDRESS = client.cmdLn.next();
//
//        System.out.println("Hostname: ");
//        PORT = client.cmdLn.nextInt();

        client.connectToServer(ADDRESS, 57620);


    }
}
