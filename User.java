package edu.udo.cs.rvs;

import java.util.Scanner;

public class User implements Runnable  {

    public static boolean exit = false;  // Falls benutzereingabe EXIT, dann auf true setzen, damit while schleifen in threads beenden

    // User userObject = new User();
    // public Thread userThread = new Thread(userObject);  // sichtbar zu Worker Thread
    // userThread.setName("User Thread");
    // Optional myWorkerThread.start();

    /**
     *  die Nutzereingaben lesen, verarbeiten und entsprechende Aktionen durchführen.
     *  Entsprechend verwaltet dieser Thread die anderen beiden Threads.
     */

    Scanner sc = new Scanner(System.in);  // Eingabe
    String input;

    @Override
    public void run() {
        System.out.println("  User Thread running.");
        // bis zum Programmende in einer Endlosschleife laufen
        while( !exit ){

            if( sc.hasNextLine() ){
                input = sc.nextLine();
                this.handleInput( input );                
            } 
            
            // Kein Befehl, schlafen
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // while end

    }

    /** 
    Methode zum erkennen und bearbeiten der Benutzereingaben
    */
    public static void handleInput(String befehl){

        switch (befehl) {
            case "EXIT": {
                exit = true;
                System.out.println("Exiting...");
                break;
            }
            case "CLEAR": {
                // alle Geräte vergessen, threadsync
                System.out.println("List Cleared.");
                break;
            }
            case "LIST": {
                System.out.println("Listing Devices:");
                // ...
                break;
            }
            case "SCAN":{
                System.out.println("Scanning for Devices...");

                int buffersize = socket.getReceiveBufferSize();
                // byte[] dataToSend = String.valueOf(intToSend).getBytes();
                // DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, destAddr, port);
                // PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                // printWriter.write(StringToSend);
                // printWriter.flush();
                try {
                    List.mcsocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }                
                break;
            }
            default: { System.out.println("Wrong Input: " + befehl); }

        } // switch case end

    }

}
