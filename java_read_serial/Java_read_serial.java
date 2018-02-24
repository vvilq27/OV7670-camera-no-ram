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
    
    private static final int WIDTH =  165;//640;
    private static final int HEIGHT = 120;//480;
    
    public static void main(String[] args) {
        SerialPort serialPort = new SerialPort("/dev/ttyUSB0");// check it with dmesg in cmd
        
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }
        
        try {
            while(true){
                serialPort.openPort();//Open serial port
                serialPort.setParams(1000000, 8, 1, 0);//Set params.
                int[] buffer = serialPort.readIntArray(19800);//Read x bytes from serial port
                serialPort.closePort();//Close serial port

                File file = new File("data.txt");

                PrintWriter pw = new PrintWriter(file);

                for(int r = 0; r < HEIGHT; r++){
                    for(int p = 0; p < WIDTH; p++){
                            pw.print(buffer[p+(r*165)] + " ");
                    }
                    pw.println("");
                }

                pw.close();

                new PictureMaker();
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
// BEFORE START CHECK PORT WITH DMESG
    }// main
    
}
