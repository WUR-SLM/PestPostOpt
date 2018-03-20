/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestopt;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author wesse016
 */
public class Optimize {
  public DataManager dataManager;
  public Boolean continueOldRun;
  private ArrayList<ParamSet> paramSet;
  public int collectionSize;
  public int numberOfIterations;
  public int criterion;
  public double epsilon;
  
  private String lineSeparator;
  
  private DecimalFormat fiveDigits;
  private DecimalFormat sixDigits;
  private Random generator;
  private RunThread myThread;
  public ArrayList<MeasuredData> measuredData;
  public ArrayList<Outflow> measuredOutflow;
  public ArrayList<Outflow> computedOutflow;
  public ArrayList<SegmentData> segmentData;
  public String[] outFile;
  public double[] minValue;
  public double[] maxValue;
  public double[] range;
  public String paramSetFileName;
  public String optimumFileName;
  public String outflowFileName;
  public String segmentFileName;
  public int numberOfSegments;
  // param[0] = Cw;
  // param[1] = Cs;
  
  public Optimize(){
    measuredOutflow = new ArrayList<Outflow>();
    computedOutflow = new ArrayList<Outflow>();
    paramSet = new ArrayList<ParamSet>();
 
    fiveDigits = new DecimalFormat("0.00000");
    sixDigits = new DecimalFormat("0.000000");
    lineSeparator = System.getProperty("line.separator");
  
    long seed = GregorianCalendar.getInstance().getTimeInMillis();
    generator = new Random(seed);
    
    continueOldRun = false;
  }
  
  public void setCore(String aCoreName, String aPath){
    myThread = new RunThread("Thread1", aCoreName, aPath);
  }

  private double computeRatio(double aValue1, double aValue2){
     double ratio = 0.0;
     if (Math.abs(aValue2) < 1.0e-4){
       ratio = aValue1;
     }
     else
     {
       ratio = (Math.max(Math.abs(aValue1), Math.abs(aValue2)) / Math.min(Math.abs(aValue1), Math.abs(aValue2))) - 1.0;
     }
     return ratio;
  }
  
  private double deviation(double aMeasured, double aCalculated){
    double f = 0.0;
    if (criterion == 0){
      f = (aMeasured - aCalculated) * (aMeasured - aCalculated);
    }
    if (criterion == 1){
      f = computeRatio(aMeasured, aCalculated);
    }
    if (criterion == 2){
      aCalculated = aCalculated / aMeasured;
      aMeasured = 1.0;
      f = (aMeasured - aCalculated) * (aMeasured - aCalculated);
    }
    return f;
  }  
  
  private double computeFunctionValue ( int aSegment){
    double functionValue = 0.0;
    Iterator myIterator = measuredData.iterator();
    while (myIterator.hasNext()){
      MeasuredData myData = (MeasuredData)myIterator.next();
      int pos = (int)(myData.seconds / dataManager.stepSize) - 1;
      if (pos < segmentData.size()){
        double value = segmentData.get(pos).content.get(aSegment);
        double measured = myData.content[aSegment];
        functionValue = functionValue + deviation(measured, value);
      }
    }
    return functionValue;
  }
  
  private double functionForOutflow(){
    computedOutflow = dataManager.readOutflow(outflowFileName);
    double f = 0.0;
    Iterator myIterator = measuredOutflow.iterator();
    while (myIterator.hasNext()){
      Outflow myData = (Outflow)myIterator.next();
      int pos = (int)(myData.t / dataManager.stepSize) - 1;
      if (pos < computedOutflow.size()){
        double value = computedOutflow.get(pos).water;
        double measured = myData.water;
        f = f + deviation(measured, value);
        value = computedOutflow.get(pos).solid;
        measured = myData.solid;
        f = f + deviation(measured, value);
      }
    }
    
    return f;
  }
  
  private double getResults(double[] aValue){
    double f1 = 0.0;
    segmentData = dataManager.readSegmentContents();
    double[] c = new double[segmentData.size()];
    for (int j=0; j<numberOfSegments; j++){
      f1 = f1 + computeFunctionValue(j);
      String out = sixDigits.format(aValue[0]).concat(",").concat(sixDigits.format(aValue[1])).concat(",").concat(sixDigits.format(aValue[2]));
      for (int i=0; i<segmentData.size(); i++){
        out=out.concat(",").concat(sixDigits.format(segmentData.get(i).content.get(j)));
      }
      out = out.concat(lineSeparator);
      dataManager.storeFile(outFile[j], out, true);  
    }
 //   dataManager.closeSurfaceContentFileAfterReading();
    
    double f2 = functionForOutflow();
    double functionValue = f1 + f2;
    return functionValue;
  }
  
  private void runPestPostCore(){
//    if (myThread != null){
//      waitForThread();
//      myThread = null;
//    }
     myThread.run();
  }
   
  private void fillCollection(){
    for (int i=0; i < minValue.length; i++){
      range[i] = maxValue[i] - minValue[i];
    }
    for (int i=0; i < collectionSize; i++){
      ParamSet mySet = new ParamSet(maxValue.length);
      for (int j=0; j<maxValue.length; j++){
        mySet.param[j] = minValue[j] + range[j]* generator.nextDouble();
      }
      dataManager.storeValuesToIni(mySet.param);
      runPestPostCore();
      mySet.functionValue = getResults(mySet.param);
      paramSet.add(mySet);
      System.out.println(i + " " + 
              fiveDigits.format(mySet.param[0]) + " " + 
              fiveDigits.format(mySet.param[1])+ " " + 
              fiveDigits.format(mySet.param[2])+ " " + 
              sixDigits.format(mySet.functionValue));
   }
  }
  
  private void swap(int i, int j){
    ParamSet mySet = new ParamSet(paramSet.get(0).param.length);
    mySet.functionValue = paramSet.get(i).functionValue;
    for (int k=0; k<paramSet.get(i).param.length; k++ ){
      mySet.param[k] = paramSet.get(i).param[k];
    }
    
    paramSet.get(i).functionValue = paramSet.get(j).functionValue;
    for (int k=0; k<paramSet.get(i).param.length; k++){
      paramSet.get(i).param[k] = paramSet.get(j).param[k];
    }
    
    paramSet.get(j).functionValue = mySet.functionValue;
    for (int k=0; k<paramSet.get(j).param.length; k++){
      paramSet.get(j).param[k] = mySet.param[k];
    }
    
  }
  
  private void sortParams(){
    for (int i=0; i<paramSet.size()-1; i++){
      for (int j=i+1; j<paramSet.size(); j++){
        if(paramSet.get(j).functionValue < paramSet.get(i).functionValue){
          swap(i,j);
        }
      }
    }
  }

  private ParamSet generateNewPoint(){
    ParamSet mySet = new ParamSet(paramSet.get(0).param.length);
    boolean next = true;
    int n = 0;
    int nLowest = 0;
    double lowestFunction = 1.0e30;

    int[] pos = new int[paramSet.get(0).param.length+1];
    while((next) & (n<1000)){
      n++;
      lowestFunction = 1.0e30;
      for (int i=0; i<pos.length; i++){
        pos[i] = generator.nextInt(paramSet.size());
        if (paramSet.get(pos[i]).functionValue < lowestFunction){
          nLowest = pos[i];
          lowestFunction = paramSet.get(nLowest).functionValue;
        }
      }
      for (int i=0; i<mySet.param.length; i++){
        mySet.param[i] = 0.0;
        for (int j=0; j<pos.length; j++){
          mySet.param[i] = mySet.param[i] + paramSet.get(pos[j]).param[i];
        }
        mySet.param[i] = mySet.param[i] / pos.length;
      }
      
      boolean withinLimits = true;
      for (int i=0; i<mySet.param.length; i++){
        if ((mySet.param[i] < minValue[i]) || (mySet.param[i] > maxValue[i])){
          withinLimits = false;
        }
      }
      
      if(withinLimits){
        computeFunctionValue(mySet);
        for (int i=0; i>mySet.param.length; i++){
          if (mySet.functionValue > lowestFunction){
            mySet.param[i] = paramSet.get(nLowest).param[i] - (mySet.param[i] - paramSet.get(nLowest).param[i]);            
          }
          else
          {
            mySet.param[i] = mySet.param[i] + (mySet.param[i] - paramSet.get(nLowest).param[i]);            
          }
        }
        
        withinLimits = true;
        for (int i=0; i<mySet.param.length; i++){
          if ((mySet.param[i] < minValue[i]) || (mySet.param[i] > maxValue[i])){
            withinLimits = false;
          }
        }
        
        if(withinLimits){
          computeFunctionValue(mySet);
        }      
      }
      next =  !withinLimits;
    }
    
    if (next){
      mySet.functionValue = 1.0e30;
      System.out.println(" ??? Error finding new point");
    }
    return mySet;
  }
  
  private void storeInArray(ParamSet aSet){
    if (aSet.functionValue <= paramSet.get(paramSet.size()-1).functionValue){
      for (int i=0; i<paramSet.size(); i++){
        if (aSet.functionValue <= paramSet.get(i).functionValue){
          paramSet.add(i, aSet);
          paramSet.remove(paramSet.size()-1);
          break;
        }
      }
    }
  }
  
  private void computeFunctionValue(ParamSet aSet){
    dataManager.storeValuesToIni(aSet.param);
    runPestPostCore();
    aSet.functionValue = getResults(aSet.param);   
  }
  
  private void iterate(){
    for (int k=0; k<numberOfIterations; k++){
      ParamSet mySet =  generateNewPoint();
      storeInArray(mySet);
      System.out.println(k + " " + fiveDigits.format(mySet.param[0]) + " " + 
              fiveDigits.format(mySet.param[1])+ " " + 
              fiveDigits.format(mySet.param[2])+ " " + 
              sixDigits.format(mySet.functionValue) +
              " " +  sixDigits.format(paramSet.get(0).functionValue) +
              " " + sixDigits.format(paramSet.get(paramSet.size()-1).functionValue));
      
      if (paramSet.get(paramSet.size()-1).functionValue - paramSet.get(0).functionValue < epsilon){
        break;
      }
      
      if ((k % 25 == 0) & (k > 0)){
        storeParamSet();;
      }
    }
  }
  
  private void storeParamSet(){
    String results = "";
    for (ParamSet myParam : paramSet){
      results = results + sixDigits.format(myParam.param[0]) + 
            ", " + sixDigits.format(myParam.param[1]) + 
            ", " + sixDigits.format(myParam.param[2]) + 
            ", " + sixDigits.format(myParam.functionValue) + lineSeparator;
    }
    dataManager.storeFile(paramSetFileName, results, Boolean.FALSE);
  }
  
   public void run(){
    if(continueOldRun){
      System.out.println("Resuming from previous parameter set");
      dataManager.readParamSet(paramSet, paramSetFileName);
    }
    else
    {
      System.out.println("Generating parameter set");
      fillCollection();
    }
    sortParams();
    storeParamSet();
    
    iterate();

    dataManager.storeValuesToIni(paramSet.get(0).param);
    runPestPostCore();
    paramSet.get(0).functionValue = getResults(paramSet.get(0).param);

    String result = "Optimum for params " + 
            sixDigits.format(paramSet.get(0).param[0]) + 
            " " + sixDigits.format(paramSet.get(0).param[1]) + 
            " " + sixDigits.format(paramSet.get(0).param[2]) + 
            "  f=" + sixDigits.format(paramSet.get(0).functionValue) + lineSeparator;
    
    dataManager.storeFile(optimumFileName, result, Boolean.FALSE);
  }
}
