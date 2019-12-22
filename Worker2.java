// 2. Möglichkeit zum Auswerten
                
                InputStreamReader streamReader = new InputStreamReader(List.mcsocket.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader);

                try {
                    if( reader.ready() ){  // ready gibt Auskunft darüber, ob aktuell eine vollständige Zeile gelesen werden kann.
                      
                        String lines = new String( pkt.getData(), StandardCharsets.UTF_8 );
                        String[] line = lines.split("\\r?\\n");
                        
                        // Erste Zeile = Pakettyp ueberpruefen
                        if ( line[0].equalsIgnoreCase("HTTP/1.1 200 OK") ) {
                          System.out.println("Unicast packet identified.");
           
                          // Alle Zeilen des Pakets durchgehen, die nötigen Informationen speichern
                           
                          for( int i = 1; i< line.length; i++){
                            if( line[i].startsWith("ST: ") ){
                                // "example string".split(":", 2)
                            }
                            else if( line[i].startsWith("USN: ") ) {

                            }


                          }

                        }
                        else if( line[0].equalsIgnoreCase("NOTIFY * HTTP/1.1") ){
                          System.out.println("Multicast packet identified.");
                        }
                        else {
                          System.out.println( "Packet type unknown: " + line[0] );
                        }
                        
                        // String line = reader.readLine(); // Liest eine Zeile ohne Zeilenumbruch                                   
                        /* String lines = new String(dp.getData(), StandardCharsets.UTF_8);
                        if (l.startsWith("S: ")) {
                                u = l.split("S: ", 2)[1].split("uuid:", 2)[1];
                                 try {
                                     uuid = UUID.fromString(u);
                                 } catch (Exception var14) {
                                 System.out.println("UUID in invalid format!");
                                 return;
                                }
                                  } else if (l.startsWith("MAN: ")) {
                                man = l.split("MAN: ", 2)[1];
                                } else if (l.startsWith("ST: ")) {
                                 st = l.split("ST: ", 2)[1];
                                }
                            }
                         }
                         else if(){
                         }
                         else { System.out.println("Datagrammfortmat nicht erkannt"); }
                        */
                    } // 2. if end
                } catch (IOException e) {
                    e.printStackTrace();
                }