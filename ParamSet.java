/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestopt;

/**
 *
 * @author wesse016
 */
public class ParamSet {
  public double[] param;
  public double functionValue;
  
  public ParamSet(int aSize){
    param = new double[aSize];
    functionValue = 1.0e30;
  }
}
