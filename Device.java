package edu.udo.cs.rvs.ssdp;

import java.net.*;
import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.net.DatagramPacket;
import java.util.UUID;

/** Device Klasse Speichert die Informationen über ein Geraet */

public class Device {

    String DeviceTyp = null;  // Unicast or Multicast
    String lines = null;
    UUID uuid = null;
    String uuidString = null;
    LinkedList<String> serviceType = new LinkedList<String>();
    LinkedList<String> nt = new LinkedList<String>();
    String nts = null;

    /** Aufruf in List.java, diese Methode listet alle gespeicherten Informationen über ein Geraet. */
    public void showDevice( Device d ){
        // Beispielformat: abcdef01-7dec-11d0-a765-a0a0c91e6bf6 - ge:fridge, ge:ice-dispenser
        //                 17bb6bfd-3bde-4810-b89c-c39c2a1183ac - urn:service:WANPPPConnection

        if ( d.DeviceTyp.equals("Unicast") ) {  // ST und USN wichtig

            System.out.print( uuidString + " - " );
            System.out.print( serviceType );
            System.out.print('\n');
        }
        else if ( d.DeviceTyp.equals("Multicast") ) {  // NT, USN, NTS wichtig

            // Beim Abmelden (ssdp:byebye)  ist der Dienst-Typ irrelevant
            if( d.nts.equals("ssdp:alive") ){ System.out.print( nt ); }
            System.out.print('\n');

        }
        else {
            // Pakettyp nicht bekannt, ignorieren
        }

    }

}