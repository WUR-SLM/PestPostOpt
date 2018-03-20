/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestopt;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author wesse016
 */
public class Runner {
  public Boolean Active;
  public Boolean ok;
  private Timer timer;
  private CoreRunner coreRunner;
  
  public Runner(String aRunFile, Integer seconds) {
    ok = false;
    timer = new Timer();
    coreRunner = new CoreRunner();
    coreRunner.runCommand = aRunFile;
    timer.schedule(new TimerReaction(), seconds * 1000);
    try {
      Active = true;
      coreRunner.execute();
      Integer result = coreRunner.get();
      if (result > -1){
        ok = true;
      }
      timer.cancel();
      Active = false;
  //    swapRunner = null;
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
    }
  }

  class TimerReaction extends TimerTask {
    @Override
    public void run() {
      System.out.println("Time's up!");
      coreRunner.kill();
//      swapRunner = null;
      timer.cancel();
      Active = false;
      ok = false;
     }
  }
    
}
