/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.ini4j.Ini;
import ucar.ma2.ArrayDouble;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author wesse016
 */
public class DataManager {
  
  public File iniFile;
  private String fileSurfaceContent;
  private NetcdfFile fileSurfaceContentRead;
  private int timeSteps;
  public int stepSize;
  public int numberOfSegments;
  private DecimalFormat sixDigits;
  public String outflowFileName;
  private String segmentFileName;
  
  public DataManager(String aIniFile){
    sixDigits = new DecimalFormat("0.000000");

    iniFile = new File(aIniFile);
    readIniFile();
  }
  
  public ArrayList<SegmentData> readSegmentContents(){
    ArrayList<SegmentData> myData = new ArrayList<SegmentData>();
    BufferedReader input = null;
    int n = 0;

    try{ 
      try {
        FileReader fr = new FileReader(segmentFileName);
        input = new BufferedReader(fr);
        String line = input.readLine();
        
        while (line != null) {
          n++;
          if (n > 1) {
            String[] parts = line.split(",");
            try{
              SegmentData d = new SegmentData();
              d.seconds = Double.parseDouble(parts[1].trim());
              for (int i=2; i<parts.length; i++){
                d.content.add(Double.parseDouble(parts[i].trim()));
              }
              myData.add(d);
            }
            catch(Exception e)
            {
              System.out.println(e.getMessage());
            }
          }
          line = input.readLine();
        }
      }
      catch(Exception e) {
        System.out.println(e.getMessage());
      }
      }
      finally
      {
      try {
        input.close();
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
    return myData;
  }   
  
  
  public ArrayList<MeasuredData> readMeasuredData(String aFile){
    ArrayList<MeasuredData> myData = new ArrayList<MeasuredData>();
    BufferedReader input = null;
    int n = 0;

    try{ 
      try {
        FileReader fr = new FileReader(aFile);
        input = new BufferedReader(fr);
        String line = input.readLine();
        
        while (line != null) {
          n++;
          if (n > 1) {
            String[] parts = line.split(",");
            try{
              MeasuredData d = new MeasuredData();
              d.seconds = Integer.parseInt(parts[0].trim());
              for (int i=0; i<parts.length-1; i++){
                d.content[i] = Double.parseDouble(parts[i+1].trim());
              }
              myData.add(d);
            }
            catch(Exception e)
            {
              System.out.println(e.getMessage());
            }
          }
          line = input.readLine();
        }
      }
      catch(Exception e) {
        System.out.println(e.getMessage());
      }
      }
      finally
      {
      try {
        input.close();
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
    return myData;
  }   

  public ArrayList<Outflow> readOutflow(String aFile){
    ArrayList<Outflow> myData = new ArrayList<Outflow>();
    BufferedReader input = null;
    int n = 0;

    try{ 
      try {
        FileReader fr = new FileReader(aFile);
        input = new BufferedReader(fr);
        String line = input.readLine();
        
        while (line != null) {
          n++;
          if (n > 1) {
            String[] parts = line.split(",");
            try{
              Outflow o = new Outflow();
              o.t = Double.parseDouble(parts[0].trim());
              o.water = Double.parseDouble(parts[3].trim());
              o.solid = Double.parseDouble(parts[4].trim());
              myData.add(o);
            }
            catch(Exception e)
            {
              System.out.println(e.getMessage());
            }
          }
          line = input.readLine();
        }
      }
      catch(Exception e) {
        System.out.println(e.getMessage());
      }
      }
      finally
      {
      try {
        input.close();
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
    return myData;
  }   

  public void readParamSet(ArrayList<ParamSet> aSet, String aFileName){
    BufferedReader input = null;
    aSet.clear();
    try{ 
      try {
        FileReader fr = new FileReader(aFileName);
        input = new BufferedReader(fr);
        String line = input.readLine();
  
        while (line != null) {
          String[] parts = line.split(",");
          try{
            ParamSet mySet = new ParamSet(parts.length-1);
            for (int i=0; i<parts.length-1; i++){
              mySet.param[i] = Double.parseDouble(parts[i]);
            }
            mySet.functionValue = Double.parseDouble(parts[parts.length-1]);
            aSet.add(mySet);
          }
          catch(Exception e)
          {
            System.out.println(e.getMessage());
          }
          line = input.readLine();
        }
      }
      catch(Exception e) {
        System.out.println(e.getMessage());
      }
      }
      finally
      {
      try {
        input.close();
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }   

  private void readIniFile(){
    Ini ini = new Ini();
    try {
      ini.load(new FileReader(iniFile));
      fileSurfaceContent = ini.fetch("Output", "Dir").concat("\\surfaceContent.nc");
      timeSteps = Integer.parseInt(ini.fetch("Pesticide", "TimeSteps"));
      stepSize = Integer.parseInt(ini.fetch("Lisem", "Timestep"));
      outflowFileName = ini.fetch("Output", "Losses");
      segmentFileName = ini.fetch("Output", "Segments");
      numberOfSegments = Integer.parseInt(ini.fetch("Segments", "Number"));
  } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
  
   public void openSurfaceContentFileForReading(){
    try{
      fileSurfaceContentRead = NetcdfFile.open(fileSurfaceContent);
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
    }
  }
   
  public double[] getTotals(int aLowY, int aHighY){
    double[] total = new double[timeSteps];
    try{
      try {
        Variable dataVar = fileSurfaceContentRead.findVariable("Content");

        if (dataVar == null) {
          System.out.println("Cant find Variable data");
        }
        else
        {
          int[] dataDims = dataVar.getShape();
          
          int[] dims = new int[3];
          dims[0] = aHighY - aLowY + 1;
          dims[1] = dataDims[1];
          dims[2] = timeSteps;
          int[] origin = new int[3];
          origin[0] = aLowY;
          origin[1] = 0;
          origin[2] = 0;
          
          ArrayDouble.D3 dataArray = (ArrayDouble.D3)dataVar.read(origin, dims);
          for (int k=0; k<timeSteps; k++){
            total[k] = 0.0;
            for (int j=0; j < dims[1]; j++){
              for (int i=0; i < dims[0]; i++){
                total[k] = total[k] + dataArray.get(i,j,k);
              }
            }
          }
        }
      }
      catch (Exception e) {
           System.out.println(e.getMessage());
      }
    }
    finally{
      return total;
    }
  }
  
  public void closeSurfaceContentFileAfterReading(){
    if (fileSurfaceContentRead != null){
      try {
        fileSurfaceContentRead.close();
        fileSurfaceContentRead = null;
      } 
      catch (Exception e) {
         System.out.println(e.getMessage());
       }
    }
  }
  
  public void storeValuesToIni(double[] aValue){
    Ini ini = new Ini();
    
    try {
      ini.load(new FileReader(iniFile));
      ini.put("Pesticide", "MaxCw", sixDigits.format(aValue[0]));
      ini.put("Pesticide", "MaxCs", sixDigits.format(aValue[1]));
      ini.put("Pesticide", "fInfiltration", sixDigits.format(aValue[2]));
      ini.store(iniFile);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

  }
  
  public void storeFile(String aFile, String aContent, Boolean aAppend){
    try{
      FileWriter fw  = new FileWriter(aFile, aAppend);
      fw.write(aContent);
      fw.close();        
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
    


  

  
}
