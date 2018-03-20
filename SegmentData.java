/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestopt;

import java.util.ArrayList;

/**
 *
 * @author wesseling
 */
public class SegmentData {
  public double seconds;
  public ArrayList<Double> content;
  
  public SegmentData(){
    seconds = 0.0;
    content = new ArrayList<Double>();
  }
}
