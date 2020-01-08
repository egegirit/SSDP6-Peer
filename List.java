package edu.udo.cs.rvs.ssdp;

import java.net.*;
import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.net.DatagramPacket;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.io.OutputStream;
import java.io.DataOutputStream;

public class List implements Runnable {

    /** Die empfangenen Pakete werden hier gespeichert, dann wird immer das aelteste bearbeitet und gelöscht  */
    public static LinkedList<DatagramPacket> dgramList = new LinkedList<>();
    /** Liste von Geraeten um die Informationen von den Geraeten zu speichern/zeigen (Ein Geraet enthaelt eine UUID und ein oder mehrere Dienst-Typen)  */
    public static LinkedList< Device > deviceList = new LinkedList<>();
    public static MulticastSocket mcsocket;                              // public static -> socket ist sichtbar zu anderen Klassen
    InetAddress ip;

    public List(){

    }


    @Override
    public void run() {

        /** Der Listen-Thread, um  bis zum Programmende endlos Datagramme zu empfangen und dem Worker-Thread zur Verfügung stellen
         * Dies passiert solange, wie das DatagramSocket nicht null, gebunden , nicht geschlossen ist und der Benutzer nicht EXIT getippt hat */

        try{
            /** Der Thread öffnet ein MulticastSocket auf Port 1900, und tritt Multicast-Gruppe „239.255.255.250“ bei .*/
            this.mcsocket = new MulticastSocket(1900);
            ip = InetAddress.getByName("239.255.255.250");
            this.mcsocket.joinGroup(ip);

        } catch(Exception e){
            e.printStackTrace();
        }

        System.out.println("  Socket initialized.");  // DEBUG

        System.out.println("  List Thread running");  // DEBUG

        while( (this.mcsocket != null) && (this.mcsocket.isBound()) && !(this.mcsocket.isClosed()) && !(User.exit) ){

          /** Buffer für einen neu kommenden Paket erstellen, als Grösse die Buffergrösse vom Socket benutzen */
          DatagramPacket buffer = null;
            try {
                System.out.println( " Socket SIZE: "+this.mcsocket.getReceiveBufferSize() );  // DEBUG
                buffer = new DatagramPacket(new byte[this.mcsocket.getReceiveBufferSize()], this.mcsocket.getReceiveBufferSize());
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
              /** auf ein Paket warten und empfangen  */
              System.out.println("  Waiting for an incoming packet.");  // DEBUG
	          this.mcsocket.receive(buffer);
              System.out.println("  Datagram Packet received.");  // DEBUG

            } catch (SocketException sexc) {
	            sexc.printStackTrace();
	        } catch (IOException ioexc) {
	            ioexc.printStackTrace();
	        }

            /** Empfangene Paket in die Liste einfügen, Threadsyncronisation  */
            synchronized( this.dgramList ) {
               this.dgramList.add( buffer );
            }
            System.out.println("  Datagram Packet added to list.");  // DEBUG

            /** Socket wird in User Klasse automatisch geschlossen, wenn User "exit" eingibt */

        }

    }

}
