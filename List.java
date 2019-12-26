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

    /** Die empfangenen Pakete werden hier gespeichert, dann wird immer das aelteste bearbeitet  */
    public static LinkedList<DatagramPacket> dgramList = new LinkedList<>();
    /** Liste um die Informationen von den Geraeten zu speichern/zeigen (Ein Geraet enthaelt eine UUID und ein oder mehr Dienst-Typen)  */
    public static LinkedList< Device > deviceList = new LinkedList<>();
    public static MulticastSocket mcsocket;                              // public static -> socket ist sichtbar zu anderen Klassen
    InetAddress ip;

    /** Konstruktor öffnet ein MulticastSocket auf Port 1900, und tritt Multicast-Gruppe „239.255.255.250“ bei  */
    public List(){

        // Folgende Codes können auch am Anfang der run methode geschrieben werden

        /* ein MulticastSocket auf Port 1900 öffnen, .. */
        try {
            this.mcsocket = new MulticastSocket(1990);
            // System.out.println("  Multicast Socked Opened at Port 1900.");  // DEBUG
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Multicast-Gruppe „239.255.255.250“ beitreten

        try {
            ip = InetAddress.getByName("239.255.255.250");
            // System.out.println("  InetAdress 239.255.255.250 initialized.");  // DEBUG
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            this.mcsocket.joinGroup(ip);
            //  System.out.println("  Joined Multicast group: 239.255.255.250.");  // DEBUG
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Der Listen-Thread, um Datagramm zu empfangen
     */
    @Override
    public void run() {
        // System.out.println("  List Thread running");  // DEBUG
        /* bis zum Programmende endlos Datagramme empfangen und dem Worker-Thread zur Verfügung stellen */
        /* Dies soll solange passieren, wie das DatagramSocket nicht null, gebunden und nicht geschlossen ist. */
        while( (this.mcsocket != null) && (this.mcsocket.isBound()) && !(this.mcsocket.isClosed()) && !(User.exit) ){

          // Lösung in Übung 6
          DatagramPacket buffer = null;
            try {
                buffer = new DatagramPacket(new byte[this.mcsocket.getReceiveBufferSize()], this.mcsocket.getReceiveBufferSize());
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
	      // Puffer erstellen, Paket empfangen und in die Liste einfügen.
              buffer = new DatagramPacket(new byte[this.mcsocket.getReceiveBufferSize()], this.mcsocket.getReceiveBufferSize());
              // System.out.println("  Waiting for an incoming packet.");  // DEBUG
	          this.mcsocket.receive(buffer);
              // System.out.println("  Datagram Packet received.");  // DEBUG

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
          // System.out.println("  Datagram Packet added to list.");  // DEBUG

            /** Socket wird in User Klasse automatisch geschlossen, wenn User "exit" eingibt */

        }

    }

}
