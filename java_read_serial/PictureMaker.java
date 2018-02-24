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
    
    private static final int WIDTH =  165;//640;    160 + 5 for end indication
    private static final int HEIGHT = 120;//480;
    int[][] rgb = new int[HEIGHT][WIDTH];
    int[][]rgb2 = new int[WIDTH][HEIGHT];
    int[][] picArray = new int[HEIGHT][WIDTH];
    
    public PictureMaker(){
        try{
        
        //read file and put data to array
        Scanner s = new Scanner(new File("data.txt"));
//        ArrayList<Integer> list = new ArrayList<>();
        List rowList;
        int[] rowIntBuf = new int[165];
        String rowStringBuf[];
        int heightIndex = 0;
        
        while(s.hasNextLine()){
            String dum = s.nextLine();
            rowStringBuf = dum.trim().split(" ");
            
            rowIntBuf = Arrays.stream(rowStringBuf).mapToInt(Integer::parseInt).toArray();          //convert string array to int array
            
            //write row array to final pic array
            for(int i = 0; i < 165; i++){
                picArray[heightIndex][i] = rowIntBuf[i];
            }
            heightIndex++;
            
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
