package edu.udo.cs.rvs.ssdp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.*;
import java.util.UUID;
import java.io.Reader;
import java.io.OutputStream;

public class User implements Runnable  {

    public static boolean exit = false;  // Falls benutzereingabe EXIT, dann auf true setzen, damit while schleifen in threads beenden

    /**
     *  die Nutzereingaben lesen, verarbeiten und entsprechende Aktionen durchführen.
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
     *  Eine Suchanfrage nach Geraeten senden.
     */
    public static void scanDevices(){

        UUID uuid = null;
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("239.255.255.250");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        /** Die UUID muss mit einer echten UUID ersetzt werden */
        UUID uuidRandom = uuid.randomUUID();
        String newUUID = uuidRandom.toString();

        // System.out.println("    Random generated UUID value: " + newUUID );  // DEBUG

        // In der Aufgabe vorgegeben
        String stringToSend =
                "M-SEARCH * HTTP/1.1\n" +                                      // Suche nach Geräten im Format HTTP/1.1
                        "S: uuid:" + newUUID + "\n" +                          // UUID des Anfragenden
                        "HOST: 239.255.255.250:1900\n" +                       // In der SSDP-Gruppe
                        "MAN: \"ssdp:discover\"\n" +                           // Anfrage-Typ: Geräte finden
                        "ST: ge:fridge\n" +                                    // Was für ein Geräte-Typ?
                        "\n";                                                  // Die leere Zeile schließt die Such-Anfrage ab

        byte[] dataToSend = String.valueOf(stringToSend).getBytes();
        DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, ip, 1900);

        try {
            // System.out.println("  Sending Scan command to the ServerSocket"); // DEBUG
            List.mcsocket.send( packet );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /** 
    Methode zum erkennen und bearbeiten der Benutzereingaben
    */
    public static void handleInput(String befehl){

        switch (befehl) {
            case "EXIT": {
                exit = true;
                /** Abmelden und Socket schliessen */
                try {
                    List.mcsocket.leaveGroup(InetAddress.getByName("239.255.255.250"));
                    System.out.println("Socket leaving group 239.255.255.250.");
                    List.mcsocket.close();
                    System.out.println("Socket closed.");
                } catch (IOException e) { /* failed */ }

                System.out.println("Exiting...");
                System.exit(0);
                break;
            }
            case "CLEAR": {
                /** alle Geräte vergessen, threadsync */
                synchronized( List.dgramList ) {
                    List.dgramList.clear();
                }
                synchronized( List.deviceList ) {
                    List.deviceList.clear();
                }

                System.out.println("List Cleared.");
                break;
            }
            case "LIST": {
                /** alle Geräte zeigen */
                System.out.println("Listing Devices:");

                synchronized( List.deviceList ) {
                    System.out.println( "Device count: " + List.deviceList.size() );
                    for (Device d : List.deviceList) {
                        d.showDevice(d);
                    }
                }

                break;
            }
            case "SCAN":{

                /** // über das MulticastSocket des Listen-Threads eine Suchanfrage senden */
                System.out.println("Scanning for Devices...");
                User.scanDevices();
                System.out.println("  Scan command sent!");  // DEBUG
                break;
            }
            default: { System.out.println("Wrong Input: " + befehl); }

        } // switch case end

    }

}
