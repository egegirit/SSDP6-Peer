package edu.udo.cs.rvs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 *  dieser Thread soll die empfangenen Datagramme aus dem Listen-Thread verarbeiten und dem User-Thread mitteilen, welche Geräte gerade Dienste im Netzwerk anbieten.
 *  Hierfür muss der Thread sowohl das Objekt des User-Threads als auch das Objekt des Listen-Threads kennen.
 */

public class Worker implements Runnable {
    
    /**
     *  Der Worker-Thread, um die Empfangenen Datagramme zu verarbeiten
     */
  
    /* bis zum Programmende in Endlosschleife laufen */
    @Override
    public void run() {
      while (!User.exit) { 
          
        /**
         *  Als erstes muss geprüft werden, ob überhaupt Datagramme zu abarbeiten vorliegen.
         *  Wenn keine Datagramme vorliegen sollte der Thread einige Millisekunden schlafen um den Prozessor nicht mit unnötig vielen Prüfungen zu überlasten
         */
        if( !(List.dgramList.isEmpty()) && !(List.dgramList == null) ){

            /**
             * Wenn nun Datagramme vorliegen sollte man sich immer das älteste nehmen und
             * aus der Liste entfernen (Threadsynchronierung!). Danach kann man die Daten des Datagramms auswerten.
             */
            
            DatagramPacket pkt;
            synchronized(List.dgramList) {
                pkt = List.dgramList.poll();  // poll retrieves and removes the head (first element) of this list.
            }

            /** die Daten des Datagramms auswerten */
            handlePacket( pkt );

        } // 1. If end
        // Datagram Liste ist leer, schlafen
        else{
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // 1. If Else end
          
      } // while schleife end
        
      // Austritt der while schleife nur wenn User EXIT Befehl eingibt
      reader.close(); // Schließt automatisch auch den streamReader
        
    } // run end

    /** die Daten des Datagramms auswerten */
    public static handlePacket( DatagramPacket pkt ){

        buffer = new byte[1024];
        buffer = pkt.getData();
        String empfangen = new String(buffer, StandardCharsets.UTF_8);
        System.out.println("Received (Test 1): "+ new String(buffer));  // new String(buffer) und empfangen sollen gleich sein,
        System.out.println("Received (Test 2): "+ empfangen);  // zum Testen, delete

        String[] line = empfangen.split("\\r?\\n");  // Empfangene string in Zeilen zerlegen

        if ( line[0].equalsIgnoreCase("HTTP/1.1 200 OK") ) {  // die erste Zeile ist der Typ des Pakets
            System.out.println("Unicast packet identified.");

            UUID uuid = null;
            String uuidString = null
            String st = null;

            // Alle Zeilen des Pakets durchgehen, die nötigen Informationen speichern (ST und USN sind wichtig bei Unicast)
            for( int i = 1; i< line.length; i++ ){

                if( line[i].startsWith("USN: ") ) {  // UUID
                    // Erst "USN:" trennen dann "uuid:" trennen, dann bleibt nur uuid Nummber übrig
                    uuidString = line[i].split("USN: ", 2)[1].split("uuid:", 2)[1];
                    try {
                        uuid = UUID.fromString(uuidString);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                else if( line[i].startsWith("ST: ") ){  // Service Typ

                    st = line[i].split("ST: ", 2)[1];

                }

            }

            // Gefundene uuid und service typ  in die speichern
            synchronized( List.deviceList ) {

            }

        }
        else if( line[0].equalsIgnoreCase("NOTIFY * HTTP/1.1") ){  // die erste Zeile ist der Typ des Paket
            System.out.println("Multicast packet identified.");

            // Alle Zeilen des Pakets durchgehen, die nötigen Informationen speichern (NT, USN und NTS sind wichtig bei Multicast)
            for( int i = 1; i< line.length; i++){
                if( line[i].startsWith("NT: ") ){
                    // "example string".split(":", 2)
                }
                else if( line[i].startsWith("USN: ") ) {  // UUID

                }
                else if( line[i].startsWith("NTS: ") ) {

                }

            }
        }
        else {  // Kein bekannter Pakettyp erkannt
            System.out.println( "Packet type unknown: " + line[0] );
        }

    }

} // Klasse end
