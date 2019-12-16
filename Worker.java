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
      // while (!User.exit) {  }
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
            synchronized(List.dgramList) {  // fix: sadece pop senkronize olmalı

                pkt = List.dgramList.pop();  

                /** die Daten des Datagramms auswerten */

                InputStreamReader streamReader = new InputStreamReader(„inputStream“, StandardCharsets.UTF_8);  // Inputstream ??
                BufferedReader reader = new BufferedReader(streamReader);

                try {
                    if( reader.ready() ){  // ready gibt Auskunft darüber, ob aktuell eine vollständige Zeile gelesen werden kann.
                      
                        String lines = new String( pkt.getData(), StandardCharsets.UTF_8 );
                        String[] line = lines.split("\\r?\\n");
                        
                        // Erste Zeile = Pakettyp ueberpruefen
                        if ( line[0].equalsIgnoreCase("HTTP/1.1 200 OK") ) {
                          System.out.println("Unicast packet identified.");
                        }
                        else if( line[0].equalsIgnoreCase("NOTIFY * HTTP/1.1") ){
                          System.out.println("Multicast packet identified.");
                        }
                        else {
                          System.out.println("Packet type unknown.");
                        }
                        
                        // String line = reader.readLine(); // Liest eine Zeile ohne Zeilenumbruch
                        //  [...]
                        
                        
                        reader.close(); // Schließt automatisch auch den streamReader

                        /* String lines = new String(dp.getData(), StandardCharsets.UTF_8);
                         String[] line = lines.split("\\r?\\n");
                         if (line[0].equalsIgnoreCase("M-SEARCH * HTTP/1.1")) {

                            for(int var9 = 0; var9 < var10; ++var9) {
                                String l = var11[var9];
                                if (l.startsWith("S: ")) {
                                u = l.split("S: ", 2)[1].split("uuid:", 2)[1];

                                 try {
                                     uuid = UUID.fromString(u);
                                 } catch (Exception var14) {
                                 System.out.println("UUID in invalid format!");
                                 return;
                                }
                                  } else if (l.startsWith("MAN: ")) {
                                man = l.split("MAN: ", 2)[1];
                                } else if (l.startsWith("ST: ")) {
                                 st = l.split("ST: ", 2)[1];
                                }
                            }

                         }
                         else if(){
                         }
                         else { System.out.println("Datagrammfortmat nicht erkannt"); }

                        */

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
        else{
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
