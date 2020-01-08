package edu.udo.cs.rvs.ssdp;//
// Decompiled by Procyon v0.5.36
// 

import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MulticastSocket;

public class SSDPTesterNew implements Runnable
{
    public MulticastSocket ms;
    
    public static void main(final String[] args) throws IOException, InterruptedException {
        SSDPTesterNew st = null;
        try {
            st = new SSDPTesterNew();
            final Thread t = new Thread(st);
            t.start();
        }
        catch (Exception exc) {
            System.out.println("Failed to start!");
            System.exit(1);
            return;
        }
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Started! Type \"exit\" to exit, or press CTRL+C");
        while (true) {
            if (br.ready() && br.readLine().equalsIgnoreCase("exit")) {
                System.exit(0);
            }
            if (Math.random() < 0.01) {
                st.notifyRandom();
            }
            Thread.sleep(100L);
        }
    }
    
    public SSDPTesterNew() throws Exception {
        (this.ms = new MulticastSocket(1900)).joinGroup(InetAddress.getByName("239.255.255.250"));
    }
    
    @Override
    public void run() {
        while (!this.ms.isClosed()) {
            try {
                final DatagramPacket dp = new DatagramPacket(new byte[this.ms.getReceiveBufferSize()], this.ms.getReceiveBufferSize());
                this.ms.receive(dp);
                this.handle(dp);
            }
            catch (Exception exc) {
                System.err.println("Couldn't receive DatagramPacket! " + exc.toString());
            }
        }
    }
    
    private void handle(final DatagramPacket dp) {
        final String lines = new String(dp.getData(), StandardCharsets.UTF_8);
        final String[] line = lines.split("\\r?\\n");
        System.out.println(" >> Received Packet: ");
        String[] array;
        for (int length = (array = line).length, j = 0; j < length; ++j) {
            final String l = array[j];
            if (!l.contains("\u0000")) {
                System.out.println("    >> " + l);
            }
        }
        if (line[0].equalsIgnoreCase("M-SEARCH * HTTP/1.1")) {
            System.out.println("Looks like M-SEARCH! Checking...");
            if (!line[0].equals("M-SEARCH * HTTP/1.1")) {
                System.out.println("Invalid format!");
                return;
            }
            String u = null;
            UUID uuid = null;
            String man = null;
            String st = null;
            try {
                String[] array2;
                for (int length2 = (array2 = line).length, k = 0; k < length2; ++k) {
                    final String i = array2[k];
                    if (i.startsWith("S: ")) {
                        u = i.split("S: ", 2)[1].split("uuid:", 2)[1];
                        try {
                            uuid = UUID.fromString(u);
                            continue;
                        }
                        catch (Exception e) {
                            System.out.println("UUID in invalid format!");
                            return;
                        }
                    }
                    if (i.startsWith("MAN: ")) {
                        man = i.split("MAN: ", 2)[1];
                    }
                    else if (i.startsWith("ST: ")) {
                        st = i.split("ST: ", 2)[1];
                    }
                }
            }
            catch (Exception e2) {
                System.out.println("Invalid format! Parse Error!");
                return;
            }
            if (man == null || uuid == null || st == null) {
                System.out.println("Missing information!");
            }
            else {
                final UUID u2 = UUID.randomUUID();
                final String data = "HTTP/1.1 200 OK\r\nS: uuid:" + u + "\r\n" + "ST: ge:fridge\r\n" + "USN: uuid:" + u2.toString() + "\r\n" + "\r\n";
                System.out.println("Announcing (reply): " + u2.toString());
                System.out.println(dp.getAddress().toString());
                System.out.println(dp.getPort());
                try {
                    final DatagramPacket dps = new DatagramPacket(data.getBytes(StandardCharsets.UTF_8), data.getBytes(StandardCharsets.UTF_8).length, dp.getAddress(), dp.getPort());
                    final DatagramPacket dpsm = new DatagramPacket(data.getBytes(StandardCharsets.UTF_8), data.getBytes(StandardCharsets.UTF_8).length, InetAddress.getByName("239.255.255.250"), dp.getPort());
                    this.ms.send(dps);
                    this.ms.send(dpsm);
                }
                catch (Exception exc) {
                    System.out.println("Failed to send!");
                }
            }
        }
    }
    
    private void notifyRandom() {
        final UUID u = UUID.randomUUID();
        final String data = "NOTIFY * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nSERVER: Debian/wheezy UPnP/1.1 MiniUPnPd/2.1\r\nNT: upnp:rootdevice\r\nUSN: uuid:" + u.toString() + ((Math.random() < 0.5) ? "" : ":") + ":upnp:rootdevice\r\n" + "NTS: " + ((Math.random() < 0.5) ? "ssdp:alive" : "ssdp:byebye") + "\r\n" + "\r\n";
        System.out.println("Announcing (notify): " + u.toString());
        try {
            final DatagramPacket dps = new DatagramPacket(data.getBytes(StandardCharsets.UTF_8), data.getBytes(StandardCharsets.UTF_8).length, InetAddress.getByName("239.255.255.250"), 1900);
            this.ms.send(dps);
        }
        catch (Exception exc) {
            System.out.println("Failed to send!");
        }
    }
}
