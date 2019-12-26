package edu.udo.cs.rvs.ssdp;

import java.net.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.net.DatagramPacket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

public class List implements Runnable {

    /**
     * Liste als LinkedList. Die empfangenen Pakete landen hier
     */
    public static LinkedList<DatagramPacket> dgramList = new LinkedList<>();
    public static LinkedList< Device > deviceList = new LinkedList<>();   // Liste um die Informationen von den Geraeten zu speichern/zeigen (Enthaelt eine UUID und ein oder mehr Dienst-Typen)
    public static MulticastSocket mcsocket;                              // public static -> socket ist sichtbar zu anderen Klassen
    InetAddress ip;

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

          // Lösung in Übung 6
          DatagramPacket buffer;
          try {
	      // Puffer erstellen, Paket empfangen und in die Liste einfügen.
              buffer = createBuffer();
	          this.mcsocket.receive(buffer);

	          // Alternative:
              // DatagramPacket dp = new DatagramPacket(new byte[this.mcsocket.getReceiveBufferSize()], this.mcsocket.getReceiveBufferSize());
              // this.ms.receive(dp);

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
          this.mcsocket.leaveGroup(ip);
          System.out.println("Socket leaving group 239.255.255.250.");
          this.mcsocket.close();
          System.out.println("Socket closed.");
        } catch (IOException e) { /* failed */ }

    }

}
