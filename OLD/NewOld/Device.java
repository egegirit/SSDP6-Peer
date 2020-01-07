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

    String PacketTyp = null;  // Unicast or Multicast
    String lines = null;      // Der Inhalt des gelesenen Pakets, die alle informationen über das Geraet enthaelt
    UUID uuid = null;
    String uuidString = null;
    LinkedList<String> serviceTypes = new LinkedList<String>();  // Alle Servicetypen eines Geraets (Unicast)
    // LinkedList<String> nt = new LinkedList<String>();           // Alle Servicetypen eines Geraets (Multicast)
    String nts = null;  // Beim Anmelden: "ssdp:byebye" , Beim Abmelden: "ssdp:byebye"

    /** Aufruf in List.java, diese Methode listet alle gespeicherten Informationen über ein Geraet. (UUID und Servicetypen) */
    public void showDevice( Device d ){
        // Beispielformat: abcdef01-7dec-11d0-a765-a0a0c91e6bf6 - ge:fridge, ge:ice-dispenser
        //                 17bb6bfd-3bde-4810-b89c-c39c2a1183ac - urn:service:WANPPPConnection

        System.out.print( uuidString + " - " );
        // if( d.nts.equals("ssdp:alive") ){ System.out.print( serviceTypes ); }  // Beim Abmelden (ssdp:byebye)  ist der Dienst-Typ irrelevant
        System.out.print( serviceTypes );
        System.out.print('\n');

    }

}