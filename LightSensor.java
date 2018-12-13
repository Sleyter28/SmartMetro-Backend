/*
 * LightSensor.java
 *
 * Created on Nov 29, 2018 6:14:42 PM;
 */

package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ILightSensor;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 *
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class LightSensor extends MIDlet {

    private ITriColorLEDArray leds = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
    private ILightSensor light = (ILightSensor)Resources.lookup(ILightSensor.class);
    private Datagram dg;
    private RadiogramConnection conn;
    private boolean fall;
    private LEDColor colors[] = {LEDColor.WHITE};
    private Datagram dgrx;
    private int c=0;

    protected void startApp() throws MIDletStateChangeException {

            while (true) {
            try {
                conn = (RadiogramConnection)Connector.open("radiogram://7f00.0101.0000.1001:69"); //Send data to the aggregator by using port 69
                dg = (Datagram) conn.newDatagram(conn.getMaximumLength());
                RadiogramConnection rx = (RadiogramConnection) Connector.open("radiogram://:67");  //Read data from the aggregator by using port 67
                dgrx = (Datagram) rx.newDatagram(rx.getMaximumLength());
                int lightValue = light.getValue() / 84; // cause the MIDlet to exit      
                //Lumenes in a train station are around 50lux/ [0-8] [3-50lux]
                if (rx.packetsAvailable()) {
                    rx.receive(dgrx);
                    boolean metroIn = dgrx.readBoolean();
                    dgrx.reset();
                    //If Metro is here and lumenes are less than 3 in a range of 1-7
                    if ((metroIn != true && lightValue < 3)) {
                         showLeds(0);
                         dg.reset();
                         fall=true;
                         dg.writeUTF("Fall " + String.valueOf(fall));
                         conn.send(dg);
                         if (c == 0){
                            System.out.println("Somebody has fallen onto tracks");
                          }
                            c++;
                    }else{
                        leds.getLED(0).setOff();
                        c=0;
                        fall=false;
                      }
                }
           } catch (IOException ex) {
            ex.printStackTrace();
            }
                Utils.sleep(50);
           }

   }

    protected void pauseApp() {
        // This is not currently called by the Squawk VM
    }

    /**
     * Called if the MIDlet is terminated by the system.
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true the MIDlet must cleanup and release all resources.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }
    public void showLeds (int color) throws IOException{

                    leds.getLED(color).setColor(colors[color]);
                    leds.getLED(color).setOn();

    }
}
