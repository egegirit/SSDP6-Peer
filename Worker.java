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
    
    /**
     *  Der Worker-Thread, um die Empfangenen Datagramme zu verarbeiten
     */
  
    /* bis zum Programmende in Endlosschleife laufen */
    @Override
    public void run() {
      System.out.println("  Worker Thread running.");
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
      // reader.close(); // Schließt automatisch auch den streamReader
        
    } // run end


    /** die Daten des Datagramms auswerten:
     *  Es wird ein neues Geraet erstellt und je nach empfangenem Pakettyp (Unicast/Multicast) werden die Daten vom Geraet initialisiert
     */
    public static void handlePacket( DatagramPacket pkt ){
        // Neues Geraet initialisieren
        Device dvc = new Device();

        byte[] buffer = new byte[1024];
        buffer = pkt.getData();
        String empfangen = new String(buffer, StandardCharsets.UTF_8);

        dvc.lines = empfangen;

        System.out.println("Received (Test 1): "+ new String(buffer));  // new String(buffer) und empfangen sollen gleich sein,
        System.out.println("Received (Test 2): "+ empfangen);  // zum Testen, delete

        String[] line = empfangen.split("\\r?\\n");  // Empfangene string in Zeilen zerlegen

        if ( line[0].equalsIgnoreCase("HTTP/1.1 200 OK") ) {  // die erste Zeile ist der Typ des Pakets
            System.out.println("Unicast packet identified.");
            dvc.DeviceTyp = "Unicast";
            UUID uuid = null;
            String uuidString = null;
            String st = null;

            // Alle Zeilen des Pakets durchgehen, die nötigen Informationen speichern (ST und USN sind wichtig bei Unicast)
            for( int i = 1; i < line.length; i++ ){

                if( line[i].startsWith("USN: ") ) {  // UUID
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

                }
                else if( line[i].startsWith("ST: ") ){  // Service Typ

                    st = line[i].split("ST: ", 2)[1];
                    dvc.serviceType = st;

                }

            }
            List.deviceList.add(dvc);

        }
        else if( line[0].equalsIgnoreCase("NOTIFY * HTTP/1.1") ){  // die erste Zeile ist der Typ des Paket
            System.out.println("Multicast packet identified.");
            dvc.DeviceTyp = "Multicast";
            UUID uuid = null;
            String uuidString = null;
            String nt = null;
            String nts = null;

            // Alle Zeilen des Pakets durchgehen, die nötigen Informationen speichern (NT, USN und NTS sind wichtig bei Multicast)
            for( int i = 1; i < line.length; i++ ){
                if( line[i].startsWith("NT: ") ){  // Service-Type

                    nt = line[i].split("NT: ", 2)[1];
                    dvc.nt = nt;

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

                }
                else if( line[i].startsWith("NTS: ") ) {

                    nts = line[i].split("NTS: ", 2)[1];
                    dvc.nts = nts;
                }

            }
            List.deviceList.add(dvc);
        }
        else {  // Kein bekannter Pakettyp erkannt, pakettyp anzeigen
            System.out.println( "Packet type unknown: " + line[0] );
            dvc.DeviceTyp = "Unknown: " + line[0];
        }

    }

} // Klasse end
