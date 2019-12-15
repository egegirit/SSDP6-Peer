import java.net.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.net.DatagramPacket;

public class List implements Runnable {

    /**
     * Liste als Warteschlange. Die empfangenen Pakete landen hier
     */
    public static LinkedList<DatagramPacket> queue = new LinkedList<>();  // DatagramPacket Typ koennte falsch sein

    public static MulticastSocket mcsocket;  // static? Workerda erişim lazım
    List listObject = new List();
    public Thread listThread = new Thread(listObject);  // sichtbar zu Worker Thread
    listThread.setName("List Thread");
    // Optional myWorkerThread.start();

    /**
     *  Konstruktor öffnet ein MulticastSocket auf Port 1900, und tritt Multicast-Gruppe „239.255.255.250“ bei
     */
    public List(){

        /* dieser Thread soll ein MulticastSocket auf Port 1900 öffnen, .. */
        try {
            this.mcsocket = new MulticastSocket(1990);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Multicast-Gruppe „239.255.255.250“ beitreten
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("239.255.255.250");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            this.mcsocket.joinGroup(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Der Listen-Thread, um Datagramm zu empfangen
     */
    @Override
    public void run() {

        /* bis zum Programmende endlos Datagramme empfangen und dem Worker-Thread zur Verfügung stellen */
        /* Dies soll solange passieren, wie das DatagramSocket nicht null, gebunden und nicht geschlossen ist. */
        while( this.mcsocket != null && this.mcsocket.isBound() && !this.mcsocket.isClosed() ){



        }

    }

}
