/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestopt;

import java.io.File;
import java.io.FileWriter;

/**
 *
 * @author wesse016
 */
public class RunThread {
  private String lineSeparator;
  private String threadName;
//  private Thread myThread;
  private String runPestPostCore;
  private String pathToIni;
  
  public RunThread(String aThreadName, String aCore, String aPath){
    threadName = aThreadName;
    runPestPostCore = aCore;
    pathToIni = aPath;
    lineSeparator = System.getProperty("line.separator");
  }
  
  private void storeFile(String aFile, String aContent, Boolean aAppend){
    try{
      FileWriter fw  = new FileWriter(aFile, aAppend);
      fw.write(aContent);
      fw.close();        
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
  
  public void run(){
    try{
      String commandFile = System.getProperty("user.dir");
      commandFile = commandFile.concat("/runCore.sh");           
//      File myFile = new File(commandFile);
//      if (myFile.exists()){
//        myFile.delete();
//      }
//      String myCommand = "#!/bin/bash" + lineSeparator + "java -jar " + runPestPostCore + " " + pathToIni + lineSeparator;
 //     storeFile(commandFile, myCommand, false);
      
      Runner myRun = new Runner(commandFile, 3600); 
//      if (myRun.ok){
//        System.out.println("Run finished OK");
//      }
//      else
//      {
//        System.out.println("Run not finished OK");
//      }
    }
    catch (Exception e)
    {
      System.out.println("????ERROR: " + e.getMessage());
    }
  }
  
  public void start ()
   {
//      showMessage("Starting " +  threadName );
//      if (myThread == null)
//      {
//         myThread = new Thread (this, threadName);
//      }
//      myThread.start();
   }

}
