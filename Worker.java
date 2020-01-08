package edu.udo.cs.rvs.ssdp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

/**
 *  der Thread von Worker Klasse verarbeitet die empfangenen Datagramme aus dem Listen-Thread, und verwaltet(hinzufügen/löschen) die erkannten Geräte in einer Liste.
 *  Mit der Gerätliste kann man sehen, welche Geräte gerade Dienste im Netzwerk anbieten.
 *
 */

public class Worker implements Runnable {
    
    /** Der Worker-Thread, um die Empfangenen Datagramme zu verarbeiten (bis zum Programmende in Endlosschleife laufen)  */

    @Override
    public void run() {
      System.out.println("  Worker Thread running.");  // DEBUG
      while (!User.exit) { 
          
        /**
         *  Als erstes prüfen, ob überhaupt Datagramme zu abarbeiten vorliegen.
         *  Wenn keine Datagramme vorliegen, schlaeft der Thread 10 Millisekunden
         */
        if( !(List.dgramList.isEmpty()) && !(List.dgramList == null) ){

            /**
             * Wenn mindestens 1 Datagramm vorliegt, immer das älteste nehmen und
             * aus der Liste entfernen. Danach die Daten des Datagramms auswerten.
             */
            
            DatagramPacket pkt;
            synchronized(List.dgramList) {
                pkt = List.dgramList.poll();  // poll retrieves and removes the head (first element) of the list.
            }

            System.out.println("    Head of the Datagram list removed.");  // DEBUG

            System.out.println("    Removed datagram sent to handle."); // DEBUG
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


    /** Methode, die die Daten eines Datagramms auswertet:
     *  Es wird ein neues Geraet erstellt und je nach empfangenem Pakettyp (Unicast/Multicast) werden die Daten vom Geraet initialisiert.
     *  Von einem Paket werden diese Informationen über ein Geraet geholt: UUID von Geraet, Servicetyp von Geraet, An/Abmeldenachricht (optional)
     * @param DatagramPacket pkt: Das aelteste Paket aus der Listenthread, das über das Multicastsocket empfangen wurde.
     */
    public static void handlePacket( DatagramPacket pkt ){
        System.out.println("      Handling packet."); // DEBUG
        // Neues Geraet initialisieren
        Device dvc = new Device();

        System.out.println("      Creating String for content."); // DEBUG

        /** Den Inhalt vom Paket in String konvertieren, danach die String zeile für zeile verarbeiten */
        byte[] buffer = new byte[65536];  // programm bleibt stehen wenn getreveicesocketsize() benutzt wird, deshalb die Grösse 65536 manual schreiben
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

            /*
            *  Man sollte vermeiden, innerhalb eines foreach-Blocks(arbeiten intern mit Iteratoren, falls iterator struktur geaendert dann error)
            *  die Liste zu modifizieren,
            *  da die Datenstruktur der Liste während der Iteration für Veränderungen gesperrt ist.
            *  Ein Hinzufügen oder Löschen von Elemente führt somit zur java.util.ConcurrentModificationException.
            *  Ein Workaround besteht darin, sich während der Iteration die IDs (die man löschen möchte) in einer
            *  weiteren Liste zwischenzuspeichern und dann über diese Liste zum Löschen zu iterieren.
            *  Oder einfach normale for schleifen verwenden.
            * */

            /** check if there is already a device with the same uuid , if there is, add the new unique service types to it */
            synchronized( List.deviceList ){
                if( !(List.deviceList.isEmpty()) && !(List.deviceList == null) ){
                        for( int i = 0; i < List.deviceList.size(); i++ ){

                            if( List.deviceList.get(i).uuidString.equalsIgnoreCase(uuidString) ){

                                System.out.println("  Same Device UUID identified!"); // DEBUG  System.out.println("  Same Service in the Device identified!"); // DEBUG System.out.println(" New Service Type added to the device"); // DEBUG
                                sameDevice = true;
                                if( (!(List.deviceList.get(i).serviceTypes == null) && !(List.deviceList.get(i).serviceTypes.isEmpty()))  && List.deviceList.get(i).serviceTypes.contains(serviceType) ){
                                    System.out.println("  Same Service in the Device identified!"); // DEBUG
                                }
                                else{
                                    List.deviceList.get(i).serviceTypes.add(serviceType);
                                    System.out.println(" New Service Type added to the device"); // DEBUG
                                }

                            }

                        }

                }
            }
            /** Neues Geraet erkannt, in die Liste speichern */
            if(!sameDevice){
                synchronized( List.deviceList ) {
                    List.deviceList.add(dvc);
                    System.out.println(" New Device added to list");  // DEBUG
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
                    // b3c152d8-blaa-5736-a8c3f32db7a42abdfe:upnp:rootdevice erste Teil trennen

                    uuidString = uuidString.split(":", 2)[0];
                    System.out.println(" UUID of the Device in the Datagramm: " + uuidString ); // DEBUG
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
                synchronized( List.deviceList ) {

                    if( !(List.deviceList.isEmpty()) && !(List.deviceList == null) ){

                        for( int i = 0; i < List.deviceList.size(); i++ ){

                            if( List.deviceList.get(i).uuidString.equalsIgnoreCase(uuidString) ){

                                List.deviceList.remove(i);
                                System.out.println("  Logged off device removed");  // DEBUG

                            }

                        }

                    }


                }

            }
            else {  /** kein ssdp:byebye, Geraet hinzufuegen/Servicetyp anpassen */

                // check if there is already a device with the same uuid and add the new unique service types
              synchronized( List.deviceList ){
                if( !(List.deviceList.isEmpty()) && !(List.deviceList == null) ){

                    for( int i = 0; i < List.deviceList.size(); i++ ){
                            if( List.deviceList.get(i).uuidString.equalsIgnoreCase(uuidString) ){

                                System.out.println("  Same Device UUID identified!"); // DEBUG
                                sameDevice = true;
                                if( (List.deviceList.get(i).serviceTypes != null) && !(List.deviceList.get(i).serviceTypes.isEmpty()) && List.deviceList.get(i).serviceTypes.contains(serviceType) ){
                                    System.out.println("  Same Service in the Device identified!"); // DEBUG
                                }
                                else{
                                    List.deviceList.get(i).serviceTypes.add(serviceType);
                                    System.out.println(" New Service Type added to the device"); // DEBUG
                                }
                            }
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
