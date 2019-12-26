package edu.udo.cs.rvs.ssdp;

import java.util.Scanner;
import java.util.*;

public class User implements Runnable  {

    public static boolean exit = false;  // Falls benutzereingabe EXIT, dann auf true setzen, damit while schleifen in threads beenden

    /**
     *  die Nutzereingaben lesen, verarbeiten und entsprechende Aktionen durchführen.
     *  Entsprechend verwaltet dieser Thread die anderen beiden Threads.
     */

    Scanner sc = new Scanner(System.in);  // Eingabe
    String input;

    @Override
    public void run() {
        System.out.println("  User Thread running.");
        // bis zum Programmende in einer Endlosschleife laufen
        while( !exit ){

            if( sc.hasNextLine() ){
                input = sc.nextLine();
                this.handleInput( input );                
            }
            // Kein Befehl, schlafen
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } // while end

    }

    /** 
    Methode zum erkennen und bearbeiten der Benutzereingaben
    */
    public static void handleInput(String befehl){

        switch (befehl) {
            case "EXIT": {
                exit = true;
                System.out.println("Exiting...");
                break;
            }
            case "CLEAR": {
                // alle Geräte vergessen, threadsync
                synchronized( this.dgramList ) {
                    List.dgramList.clear();
                }
                synchronized( List.deviceList ) {
                    List.deviceList.clear();
                }

                System.out.println("List Cleared.");
                break;
            }
            case "LIST": {
                System.out.println("Listing Devices:");

                synchronized( List.deviceList ) {
                    for (Device d : List.deviceList) {
                        d.showDevice(d);
                    }
                }

                break;
            }
            case "SCAN":{
                // über das MulticastSocket des Listen-Threads eine Suchanfrage senden
                System.out.println("Scanning for Devices...");

                // int buffersize = List.mcsocket.getReceiveBufferSize();
                // byte[] dataToSend = String.valueOf(intToSend).getBytes();
                // DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, destAddr, port);
                // PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                // printWriter.write(StringToSend);
                // printWriter.flush();

                // get the output stream from the socket.
                OutputStream outputStream = List.mcsocket.getOutputStream();
                // create a data output stream from the output stream so we can send data through it
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                System.out.println("  Sending Scan command to the ServerSocket");

                // Die UUID muss mit einer echten UUID ersetzt werden
                // creating random UUID & checking the value of random UUID
                String newUUID = uid.randomUUID();
                System.out.println("    Random UUID value: " + newUUID);

                // In der Aufgabe vorgegeben
                String stringToSend =
                        "M-SEARCH * HTTP/1.1\n" +                              // Suche nach Geräten im Format HTTP/1.1
                        "S: uuid:" + newUUID + "\n"    // UUID des Anfragenden
                        "HOST: 239.255.255.250:1900\n" +                       // In der SSDP-Gruppe
                        "MAN: \"ssdp:discover\"\n" +                           // Anfrage-Typ: Geräte finden
                        "ST: ge:fridge\n" +                                    // Was für ein Geräte-Typ?
                        "\n";                                                  // Die leere Zeile schließt die Such-Anfrage ab

                // write the message to send
                dataOutputStream.writeUTF(stringToSend);
                dataOutputStream.flush(); // send the message
                dataOutputStream.close(); // close the output stream

                // 2. Möglichkeit zu senden ist: List.mcsocket.send( paket );
                // das wurde durch dataOutputStream.writeUTF() ersetzt

                break;
            }
            default: { System.out.println("Wrong Input: " + befehl); }

        } // switch case end

    }

}
