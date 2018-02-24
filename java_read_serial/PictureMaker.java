//creates monochrome bmp from file data
package java_read_serial;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;


public class PictureMaker {
    
    private static final int WIDTH =  160;//640;    160 + 5 for end indication
    private static final int HEIGHT = 120;//480;
    int[][] rgb = new int[HEIGHT][WIDTH];
    int[][]rgb2 = new int[WIDTH][HEIGHT];
    int[][] picArray = new int[HEIGHT][WIDTH];
    
    public PictureMaker(){
        try{
        
        //read file and put data to array
        Scanner s = new Scanner(new File("data.txt"));
        int[] rowIntBuf = new int[WIDTH];
        String rowStringBuf[];
        int heightIndex = 0;
        

        System.out.println("im in picture maker class now");
        //gets every line, splits nums to different string arrays, converts those arrays to int
        //after converting all lines from file we've got pic array
        while(s.hasNextLine() && heightIndex < 120){
            
            if(heightIndex == 0)    //skip first line cus always gonna be mess there
                s.nextLine();
            String dum = s.nextLine();
            rowStringBuf = dum.trim().split(" ");
            rowIntBuf = Arrays.stream(rowStringBuf).mapToInt(Integer::parseInt).toArray();          //convert string array to int array
           
            //write row array to final pic array
            System.out.println("size of row buffer "+ rowIntBuf.length + "row number: " + heightIndex);
            //starts from index 1 cuz at 0 we got frame number
            for(int i = 1; i < rowIntBuf.length; i++){
                if(i> 159)
                    break;
                picArray[119 - rowIntBuf[0]][i] = rowIntBuf[i]; //put that row in propper place of picArray and coppy all data into it
                System.out.print( i + " ");
            }
            heightIndex++;  // array vertical index
            
            /* old version
            heightIndex++;  // array vertical index
            if(heightIndex == 0)    //skip first line cus always gonna be mess there
                s.nextLine();
            String dum = s.nextLine();
            rowStringBuf = dum.trim().split(" ");
            rowIntBuf = Arrays.stream(rowStringBuf).mapToInt(Integer::parseInt).toArray();          //convert string array to int array
           
            //write row array to final pic array
            System.out.println("size of row buffer "+ rowIntBuf.length);
            for(int i = 0; i < rowIntBuf.length; i++){
                if(i> 159)
                    break;
                picArray[heightIndex][i] = rowIntBuf[i];
                System.out.println(heightIndex + " "+ i);
            }
            heightIndex++;  // array vertical index
            */
        }
            
        s.close();
        
        /*
        System.out.println("printing pic array:");
        //print array
        for(int j = 0; j<HEIGHT; j++){
            for(int k =0; k< WIDTH; k++)
                System.out.print(picArray[j][k] + ",");
            System.out.println("");
        }
        */
            
        } catch(IOException e){
            e.printStackTrace();
        }
        
       //create picture
        BMP bmp = new BMP();
	bmp.saveBMP("/home/aras/Pictures/out9" + ".bmp", picArray);
	      		
        System.out.println("Saved image");
    }
}
