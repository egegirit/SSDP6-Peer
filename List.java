import java.net.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.net.DatagramPacket;

public class List implements Runnable {

    /**
     * Liste als Warteschlange. Die empfangenen Pakete landen hier
     */
    public static LinkedList<DatagramPacket> dgramList = new LinkedList<>();

    public static MulticastSocket mcsocket;  // public static -> sichtbar zu anderen Klassen
    
    // byte[] b = new byte[BUFFER_LENGTH];  // nicht noetig
    // DatagramPacket dgramPaket = new DatagramPacket(b, b.length);
    
    /**
     *  Konstruktor öffnet ein MulticastSocket auf Port 1900, und tritt Multicast-Gruppe „239.255.255.250“ bei
     */
    public List(){

        // Folgende Codes können auch am Anfang der run methode geschrieben werden

        /* ein MulticastSocket auf Port 1900 öffnen, .. */
        try {
            this.mcsocket = new MulticastSocket(1990);
            System.out.println("  Multicast Socked Opened at Port 1900.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Multicast-Gruppe „239.255.255.250“ beitreten
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("239.255.255.250");
            System.out.println("  InetAdress 239.255.255.250 initialized.");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            this.mcsocket.joinGroup(ip);
            System.out.println("  Joined Multicast group: 239.255.255.250.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Der Listen-Thread, um Datagramm zu empfangen
     */
    @Override
    public void run() {
        System.out.println("  List Thread running");
        /* bis zum Programmende endlos Datagramme empfangen und dem Worker-Thread zur Verfügung stellen */
        /* Dies soll solange passieren, wie das DatagramSocket nicht null, gebunden und nicht geschlossen ist. */
        while( (this.mcsocket != null) && (this.mcsocket.isBound()) && !(this.mcsocket.isClosed()) && !(User.exit) ) ){

          // Lösung in Übung
          try {
	      // Puffer erstellen, Paket empfangen und in die Liste einfügen.
              DatagramPacket buffer = createBuffer();
	          this.mcsocket.receive(buffer);
	      // Fehlerbehandlung
	      } catch (SocketException sexc) {
	          sexc.printStackTrace();
	      } catch (IOException ioexc) {
	         ioexc.printStackTrace();
	      }

          // Empfangene paket in die Liste einfügen, Threadsyncronisation
          synchronized( this.dgramList ) {
             this.dgramList.add( buffer );
          }                    
          System.out.println("  Datagram Packet added to list.");
          
          /*
           Der Thread soll sich zum Programmende beenden und dabei das MulticastSocket automatisch schließen.
           */

        }
        // Austritt von while schleife: Programm beenden
        try {
          socket.close();
          System.out.println("Socket closed.");
        } catch (IOException e) { /* failed */ }

    }

}
