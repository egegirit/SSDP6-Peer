package edu.udo.cs.rvs.ssdp;

import java.net.*;
import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.net.DatagramPacket;
import java.util.UUID;

public class Device {

    String DeviceTyp = null;  // Unicast or Multicast
    String lines = null;
    UUID uuid = null;
    String uuidString = null;
    String serviceType = null;  // als array? es kann mehrere serviceTypes geben
    String nt = null;
    String nts = null;

    // Für den List befehl in List.java
    public void showDevice( Device d ){
        // Beispielformat: abcdef01-7dec-11d0-a765-a0a0c91e6bf6 - ge:fridge, ge:ice-dispenser
        //                 17bb6bfd-3bde-4810-b89c-c39c2a1183ac - urn:service:WANPPPConnection

        if ( d.DeviceTyp.equals("Unicast") ) {  // ST und USN wichtig

            System.out.print( uuidString + " - " );
            System.out.print( serviceType + '\n');

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