/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestopt;

import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.ini4j.Ini;
/**
 *
 * @author wesse016
 */
public class Control {
  private int numberOfCores;
  private DataManager dataManager;
  private Optimize optimize;
  private String measuredDataFile;
  private String measuredOutflowFile;
  
  public Control(String[] aArgs){
    getAvailableProcessors();
    dataManager = new DataManager(aArgs[0]);
    initializeOptimization();
    optimize.dataManager = dataManager;
    optimize.numberOfSegments = dataManager.numberOfSegments;
    
    for (int i=0; i<5; i++){
      File fileOut = new File(optimize.outFile[i]);
      if (fileOut.exists()){
        fileOut.delete();
      }
    }
    
      optimize.setCore(aArgs[1], dataManager.iniFile.getAbsolutePath());
    
      if(aArgs.length > 2){
        if (aArgs[2].equals("continue")){
          optimize.continueOldRun = true;
        }
        else
        {
          optimize.continueOldRun = false;
        }
      }
    compute();
  }
  
  private void initializeOptimization(){
    String iniFileName = System.getProperty("user.dir");
    iniFileName = iniFileName.concat("/PestOpt.ini");           
    File iniFile = new File(iniFileName);
    if (!iniFile.exists()){
      System.out.println("??? Error: file " + iniFileName + " does not exist!");
    }
    else
    {
    Ini ini = new Ini();
    
    try {
      optimize = new Optimize();
      ini.load(new FileReader(iniFile));
      optimize.collectionSize = Integer.parseInt(ini.fetch("CRS", "Size"));
      optimize.numberOfIterations = Integer.parseInt(ini.fetch("CRS", "Iterations"));
      optimize.epsilon = Double.parseDouble(ini.fetch("CRS", "Epsilon"));
      int n = Integer.parseInt(ini.fetch("Params", "Number"));
      optimize.minValue = new double[n];
      optimize.maxValue = new double[n];
      optimize.range = new double[n];
      for (int i=0; i<n; i++){
        optimize.minValue[i] = Double.parseDouble(ini.fetch("Params", "minValue"+((Integer)(i+1)).toString()));
        optimize.maxValue[i] = Double.parseDouble(ini.fetch("Params", "maxValue"+((Integer)(i+1)).toString()));
      }
 
      n = Integer.parseInt(ini.fetch("Output", "Number"));
      optimize.outFile = new String[n];
      for (int i=0; i<n; i++){
        optimize.outFile[i] = ini.fetch("Output", "File"+((Integer)(i+1)).toString());
      }
      
      optimize.paramSetFileName = ini.fetch("Output", "ParamSet");
      optimize.optimumFileName = ini.fetch("Output", "OptPars");
       
      optimize.criterion = Integer.parseInt(ini.fetch("Criterion", "Method"));
      
      measuredDataFile = ini.fetch("Measurements", "File");
      measuredOutflowFile =  ini.fetch("Measurements", "Outflow");
      
      optimize.outflowFileName = dataManager.outflowFileName;
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
  
  private void getAvailableProcessors() {

   Runtime runtime = Runtime.getRuntime();

   numberOfCores = runtime.availableProcessors();

   System.out.println("Number of processors available to the Java Virtual Machine: " + numberOfCores);

  }

  
  private void compute(){  
    Calendar startTime = new GregorianCalendar();
    System.out.println(startTime.getTime().toString());
    optimize.measuredData = dataManager.readMeasuredData(measuredDataFile);
    optimize.measuredOutflow = dataManager.readOutflow(measuredOutflowFile);
    optimize.run();    
    Calendar endTime = new GregorianCalendar();
    System.out.println(endTime.getTime().toString());
    
  }
  
}
