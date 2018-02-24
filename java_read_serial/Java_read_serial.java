/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package java_read_serial;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import jssc.SerialPort;
import jssc.SerialPortList;

/**
 *
 * @author aras
 */
public class Java_read_serial {
    
    private static final int WIDTH =  160;//640;
    private static final int HEIGHT = 120;//480;
    
    public static void main(String[] args) {
        SerialPort serialPort = new SerialPort("/dev/ttyUSB0");// check it with dmesg in cmd
        
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }
        
        boolean newFrame = false;
        
        try {
            while(true){
                serialPort.openPort();//Open serial port
                serialPort.setParams(1000000, 8, 1, 0);//Set params.
                int[] buffer = serialPort.readIntArray(19801);//Read x bytes from serial port
                serialPort.closePort();//Close serial port

                File file = new File("data.txt");

                PrintWriter pw = new PrintWriter(file);
                int r = 0;
                int p = 0;
                
                while( r < HEIGHT || p < 19800){
                    //if beginning of the frame
                    if(buffer[p + 1] == 63 && buffer[p + 2] == 63 && buffer[p + 3] == 63){
                        
                        if(r == 0){// skip first row of data
                            pw.println("43 43 43"); // dummy data
                            pw.print(buffer[p] + " ");
                        }
                        else{
                            pw.println();
                            pw.print(buffer[p] + " ");
                        }
//                        System.out.println("got frame! " + buffer[p]);
                        p += 3;
                        r++;
                    //if pointer somewhere in data of row
                    } else{
                        pw.print(buffer[p] + " ");
                    }
                    p++;
                }//end while
                        
                pw.close();

                new PictureMaker();
            }// end main loop
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
// BEFORE START CHECK PORT WITH DMESG
    }// main
    
}
