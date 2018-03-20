/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestopt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.SwingWorker;

/**
 *
 * @author wesse016
 */
public class CoreRunner  extends SwingWorker<Integer, Integer>
{
  private Process p;
  public String runCommand;
 
  @Override
  protected Integer doInBackground() throws Exception {
    try{
      ProcessBuilder pb = new ProcessBuilder(runCommand);
      Process p = pb.start();
      BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String s = "";
      while((s = in.readLine()) != null){
        System.out.println(s);
      }
      int status = p.waitFor();
//      System.out.println("Exited with status: " + status);      
      return status;
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage() + "\n");
      return -1;
    }
  }

  @Override
  protected void done()
  {
    if (p != null){
       p = null;
    }
  }
  
  public void kill(){
    if (p != null){
      p.destroyForcibly();
      p = null;
    }
  }

  
}
