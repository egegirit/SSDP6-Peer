package edu.udo.cs.rvs.ssdp;

/**
 * This class is first instantiated on program launch and IF (and only if) it
 * implements Runnable, a {@link Thread} is created and started.
 *
 */
public class SSDPPeer implements Runnable
{  
  // Damit die Klassenobjekte zu Worker Thread sichtbar sind
  public static List listObject;  // static?
  public static User userObject;
  public static Worker workerObject;
  
  public SSDPPeer()
  {
    // List Object erstellen, Thread inizialisieren
    listObject = new List();
    System.out.println("List Object created.");
    public Thread listThread = new Thread(listObject);  // sichtbar zu Worker Thread
    listThread.setName("List Thread");
    System.out.println("List Thread initialized.");
    
    // User Object erstellen, Thread inizialisieren
    userObject = new User();
    System.out.println("User Object created.");
    public Thread userThread = new Thread(userObject);  // sichtbar zu Worker Thread
    userThread.setName("User Thread");
    System.out.println("User Object initialized.");
    
    // Worker Object erstellen, Thread inizialisieren
    workerObject = new Worker();
    System.out.println("Worker Object created.");
    Thread workerThread = new Thread(workerObject);
    workerThread.setName("Worker Thread");
    System.out.println("Worker Thread initialized.");    
    
  }

  @Override
  public void run() {

    // Threads starten
    listThread.start();
    userThread.start();
    workerThread.start();

  }
}
