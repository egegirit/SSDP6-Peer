package edu.udo.cs.rvs;


import java.util.Scanner;

public class User implements Runnable  {

    User userObject = new User();
    public Thread userThread = new Thread(userObject);  // sichtbar zu Worker Thread
    userThread.setName("User Thread");
    // Optional myWorkerThread.start();

    /**
     *  die Nutzereingaben lesen, verarbeiten und entsprechende Aktionen durchführen.
     *  Entsprechend verwaltet dieser Thread die anderen beiden Threads.
     */

    Scanner sc = new Scanner(System.in);  // Eingabe

    @Override
    public void run() {

        // bis zum Programmende in einer Endlosschleife laufen
        while( true ){

            if( sc.hasNextLine() ){
                //String input = sc.nextLine();

            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void handleInput(String befehl){

        switch (befehl) {
            case "EXIT": {
                System.out.println("Exiting...");
                break;
            }
            case "CLEAR": {
                System.out.println("List Cleared.");
                break;
            }
            case "LIST": {
                System.out.println("Listing Devices:");
                break;
            }
            case "SCAN":{
                System.out.println("Scanning for Devices...");
                break;
            }
            default: { System.out.println("Wrong Input: " + befehl); }

        }

    }

}
