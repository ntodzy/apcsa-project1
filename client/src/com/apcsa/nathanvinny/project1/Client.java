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

                // server returns 503 if two clients are connected.
                if (this.din.readInt() == 503) {
                    System.out.println("Server is busy right now. Please try again later.");
                    socket.close();
                    return; // just a way to force breaking. most likely redundant.

                } else {
                    System.out.println("Connected!");
                    id = this.din.readInt(); // should be the first thing the server sends!!
                    System.out.printf("Connected to server as user %d%n", id);

                    while (!socket.isClosed()) {
                        switch (din.readInt()) {
                            case 200: System.out.println("Server Accepted Request; waiting for opp."); break; // all good from the server. @todo set boolean varible to execute.
                            case 205: System.out.println("Opponent Disconnected, disconnecting."); break; // read.
                            case 400: System.out.println("Malformed Request. Most likely had wrong data"); break; // @todo add check in client as well.

                            default:;
                        }

                        // if you're here then congratulations. the server did not refuse the connections.
                        System.out.println("(R)ock (P)aper (S)cissors or (quit)");
                        String input = "";
                        if (cmdLn.hasNext()) {
                            switch (input = cmdLn.nextLine()) { // determine to continque the game loop, yes i know dumb switch.
                                case "quit": dout.writeInt(99); dout.flush(); socket.close();
                                default: dout.writeInt(100); dout.flush(); // server expects this to start its game loop.
                            }
                        } else {
                            System.out.println("NO INPUT"); // dotn know what else to put ehre.  i dont think the hasNext is very important.
                        }

                        // Start Game Loop
//                        System.out.println(input.replace(" ", "&nbsp"));
                        dout.writeUTF(input);
                        dout.flush();
                        // End Game Loop
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
