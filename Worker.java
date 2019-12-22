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

                buffer = new byte[1024];
                buffer = pkt.getData();
                String empfangen = new String(buffer, StandardCharsets.UTF_8);
                System.out.println("Received (Test 1): "+ new String(buffer));  // new String(buffer) und empfangen sollen gleich sein,
                System.out.println("Received (Test 2): "+ empfangen);

                String[] line = empfangen.split("\\r?\\n");  // Empfangene string in Zeilen zerlegen
                if ( line[0].equalsIgnoreCase("HTTP/1.1 200 OK") ) {
                    System.out.println("Unicast packet identified.");

                    // Alle Zeilen des Pakets durchgehen, die nötigen Informationen speichern

                    for( int i = 1; i< line.length; i++){
                        if( line[i].startsWith("ST: ") ){
                            // "example string".split(":", 2)
                        }
                        else if( line[i].startsWith("USN: ") ) {

                        }

                    }

                }
                else if( line[0].equalsIgnoreCase("NOTIFY * HTTP/1.1") ){
                    System.out.println("Multicast packet identified.");
                }
                else {
                    System.out.println( "Packet type unknown: " + line[0] );
                }

        } // 1. If end
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

} // Klasse end
