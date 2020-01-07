package edu.udo.cs.rvs.ssdp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 *  dieser Thread soll die empfangenen Datagramme aus dem Listen-Thread verarbeiten und dem User-Thread mitteilen, welche Geräte gerade Dienste im Netzwerk anbieten.
 *  Hierfür muss der Thread sowohl das Objekt des User-Threads als auch das Objekt des Listen-Threads kennen.
 */

public class Worker implements Runnable {
    
    /** Der Worker-Thread, um die Empfangenen Datagramme zu verarbeiten (bis zum Programmende in Endlosschleife laufen)  */

    @Override
    public void run() {
      System.out.println("  Worker Thread running.");  // DEBUG
      while (!User.exit) { 
          
        /**
         *  Als erstes muss geprüft werden, ob überhaupt Datagramme zu abarbeiten vorliegen.
         *  Wenn keine Datagramme vorliegen, sollte der Thread einige Millisekunden schlafen
         */
        if( !(List.dgramList.isEmpty()) && !(List.dgramList == null) ){

            /**
             * Wenn nun Datagramme vorliegen, immer das älteste nehmen und
             * aus der Liste entfernen. Danach die Daten des Datagramms auswerten.
             */
            
            DatagramPacket pkt;
            synchronized(List.dgramList) {
                pkt = List.dgramList.poll();  // poll retrieves and removes the head (first element) of the list.
            }

            System.out.println("    Head of the Datagram list removed.");  // DEBUG

            System.out.println("    Head of the Datagram sent to handle."); // DEBUG
            /** die Daten des Datagramms auswerten */
            this.handlePacket( pkt );

        } // 1. If end

        /** Wenn Liste leer, 10 ms schlafen */
        else{
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // 1. If Else end
          
      } // while schleife end

      /** Austritt der while schleife nur wenn User EXIT Befehl eingibt */

    } // run end


    /** Methode, die die Daten des Datagramms auswertet:
     *  Es wird ein neues Geraet erstellt und je nach empfangenem Pakettyp (Unicast/Multicast) werden die Daten vom Geraet initialisiert
     */
    public static void handlePacket( DatagramPacket pkt ){
        System.out.println("      Handling packet."); // DEBUG
        // Neues Geraet initialisieren
        Device dvc = new Device();

        System.out.println("      Creating String for content."); // DEBUG

        /** Den Inhalt vom Paket in String konvertieren, danach die String zeile für zeile verarbeiten */
        byte[] buffer = new byte[1024];  // programm bleibt wenn getreveicesocketsize() benutzt wird
        buffer = pkt.getData();
        String empfangen = new String(buffer, StandardCharsets.UTF_8);

        System.out.println("      Content String created."); // DEBUG

        dvc.lines = empfangen;

        System.out.println("Content of the processing packet: "+ empfangen);  // DEBUG

        /** Empfangene string in Zeilen zerlegen */
        String[] line = empfangen.split("\\r?\\n");

        UUID uuid = null;
        String uuidString = null;
        String serviceType = null;
        String nts = null;

        boolean abmelden = false;

        /** sameDevice wird true, falls mehrere pakete dieselbe UUID haben. Damit werden neue Servicetypen zu ihren zugehörigen UUID hinzugefügt */
        boolean sameDevice = false;

        /**  die erste Zeile ist der Typ des Pakets */
        /**  Erstens überprüfen, ob wir einen Unicast Pakettyp haben  */
        if ( line[0].equalsIgnoreCase("HTTP/1.1 200 OK") ) {
            System.out.println("Unicast packet identified.");  // DEBUG
            dvc.PacketTyp = "Unicast";

            /**  Alle Zeilen des Pakets durchgehen, die nötigen Informationen speichern (ST und USN sind wichtig bei Unicast) */
            for( int i = 1; i < line.length; i++ ){

                System.out.println("Now processing: " + line[i] ); // DEBUG

                if( line[i].startsWith("USN: ") ) {  // UUID

                    /** Erst "USN:" trennen dann "uuid:" trennen, dann bleibt nur uuid Nummber übrig */
                    uuidString = line[i].split("USN: ", 2)[1].split("uuid:", 2)[1];
                    dvc.uuidString = uuidString;
                    try {
                        uuid = UUID.fromString(uuidString);
                        dvc.uuid = uuid;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(" USN added to device");  // DEBUG
                }
                else if( line[i].startsWith("ST: ") ){  // Service Typ

                    serviceType = line[i].split("ST: ", 2)[1];
                    dvc.serviceTypes.add(serviceType);
                    System.out.println(" ST added to device");  // DEBUG

                }

            }

            /** check if there is already a device with the same uuid , if there is, add the new unique service types to it */
            for (Device d : List.deviceList) {
                if( d.uuidString.equalsIgnoreCase(uuidString) ){
                    System.out.println("  SAME UUID IDENTIFIED"); // DEBUG
                    sameDevice = true;
                    if( d.serviceTypes.contains(serviceType) ){
                        System.out.println("  SAME SERVICE IDENTIFIED"); // DEBUG
                    }
                    else{
                        d.serviceTypes.add(serviceType);
                        System.out.println(" Service Type added to device"); // DEBUG
                    }
                }
            }
            if(!sameDevice){
                synchronized( List.deviceList ) {
                    List.deviceList.add(dvc);
                    System.out.println(" Unique Device added to list");  // DEBUG
                }
            }

        }
        /** Überprüfen ob es ein Multicastpaket ist */
        else if( line[0].equalsIgnoreCase("NOTIFY * HTTP/1.1") ){  // die erste Zeile ist der Typ des Paket
            System.out.println("Multicast packet identified.");  // DEBUG
            dvc.PacketTyp = "Multicast";

            // Alle Zeilen des Pakets durchgehen, die nötigen Informationen speichern (NT, USN und NTS sind wichtig bei Multicast)
            for( int i = 1; i < line.length; i++ ){
                System.out.println("Now processing: " + line[i] ); // DEBUG
                if( line[i].startsWith("NT: ") ){  // Service-Type

                    serviceType = line[i].split("NT: ", 2)[1];
                    dvc.serviceTypes.add(serviceType);
                    System.out.println(" Service Type added to device"); // DEBUG
                }
                else if( line[i].startsWith("USN: ") ) {  // UUID

                    // Erst "USN:" trennen dann "uuid:" trennen, dann bleibt nur uuid Nummber übrig
                    uuidString = line[i].split("USN: ", 2)[1].split("uuid:", 2)[1];
                    dvc.uuidString = uuidString;
                    try {
                        uuid = UUID.fromString(uuidString);
                        dvc.uuid = uuid;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(" USN added to device"); // DEBUG
                }
                else if( line[i].startsWith("NTS: ") ) {  // falls ssdp:byebye dann abmelden und aus der liste löschen

                    nts = line[i].split("NTS: ", 2)[1];
                    dvc.nts = nts;
                    if( nts.equalsIgnoreCase("ssdp:byebye") ){ abmelden = true; }
                    System.out.println(" NTS added to device"); // DEBUG

                }

            }

            if( abmelden ){  /** Geraet meldet sich ab, aus der liste löschen */
                System.out.println(" Bye Bye Detected");    // DEBUG
                System.out.println("  Device logged off");  // DEBUG
                for (Device d : List.deviceList) {
                    if( d.uuidString.equalsIgnoreCase(uuidString) ){
                        if( d.nts.contains("ssdp:byebye") ){
                            synchronized( List.deviceList ) {
                                List.deviceList.remove(d);
                            }
                            System.out.println("  Logged off device removed");  // DEBUG
                        }
                    }
                }

            }
            else {  /** kein ssdp:byebye, Geraet hinzufuegen/Servicetyp anpassen */

                // check if there is already a device with the same uuid and add the new unique service types
                for (Device d : List.deviceList) {
                    if( d.uuidString.equalsIgnoreCase(uuidString) ){
                        System.out.println("  SAME UUID IDENTIFIED");
                        sameDevice = true;
                        if( d.serviceTypes.contains(serviceType) ){
                            System.out.println("  SAME SERVICE IDENTIFIED");
                        }
                        else{
                            d.serviceTypes.add(serviceType);
                            System.out.println("  Service Type added to device");  // DEBUG
                        }
                    }
                }
                if(!sameDevice){

                    synchronized( List.deviceList ) {
                        List.deviceList.add(dvc);
                        System.out.println("  Device added to list");  // DEBUG
                    }

                }

            }

        }
        else {
            /** Kein bekannter Pakettyp erkannt, pakettyp anzeigen */
            System.out.println( "Packet type unknown: " + line[0] );  // DEBUG
        }

    }

} // Klasse end
