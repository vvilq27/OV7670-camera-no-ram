//OV7670 camera computer nerd project, working/testing it

#define F_CPU 16000000UL
#include <stdint.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/twi.h>
#include <util/delay.h>
#include <avr/pgmspace.h>
#include "ov7670.h"

/* Configuration: this lets you easily change between different resolutions
 * You must only uncomment one
 * no more no less*/
//#define useVga
//#define useQvga
#define useQqvga

static inline void serialWrB(uint8_t dat){
	while(!( UCSR0A & (1<<UDRE0)));//wait for byte to transmit
	UDR0=dat;
	while(!( UCSR0A & (1<<UDRE0)));//wait for byte to transmit
}

static void StringPgm(const char * str){
	do{
		serialWrB(pgm_read_byte_near(str));
	}while(pgm_read_byte_near(++str));
}

//wg - pic width in px; hg - height
static void captureImg(uint16_t wg,uint16_t hg){
		uint16_t lg2;

	#ifdef useQvga
		uint8_t buf[640];

	#elif defined(useQqvga)
		uint8_t buf[160]; //320 for rgb stuff, 160 for n=monochrome
	#endif

		StringPgm(PSTR("RDY"));
		//Wait for vsync it is on pin 3 (counting from 0) portD
		while(!(PIND&8));//wait for high
		while((PIND&8));//wait for low

	#ifdef useVga
		while(hg--){				//get all rows in pic
			lg2=wg;
			while(lg2--){			//get all pixls in row
				while((PIND&4));// PIND&4 - checks PD3 pin(PCLK) or waits for PCLK to go low
				UDR0=(PINC&15)|(PIND&240);	//send data on D7:0 when low on PCLK
				while(!(PIND&4));//wait for PCLK high
			}
		}


	#elif defined(useQvga)
		/*We send half of the line while reading then half later */
		while(hg--){
			uint8_t*b=buf,*b2=buf;
			lg2=wg/2;
			while(lg2--){
				while((PIND&4));//wait for low
				*b++=(PINC&15)|(PIND&240);
				while(!(PIND&4));//wait for high
				while((PIND&4));//wait for low
				*b++=(PINC&15)|(PIND&240);
				UDR0=*b2++;
				while(!(PIND&4));//wait for high
			}
			/* Finish sending the remainder during blanking */
			lg2=wg/2;
			while(!( UCSR0A & (1<<UDRE0)));//wait for byte to transmit
			while(lg2--){
				UDR0=*b2++;
				while(!( UCSR0A & (1<<UDRE0)));//wait for byte to transmit
			}
		}
	#else
		/* This code is very similar to qvga sending code except we have even more blanking time to take advantage of */
		//start of picture

		//capturing data on data bus every PCLK cicle
		//saving it to buffer with first pointer
		//send over uart using second pointer to read buffer
		while(hg--){		//120
			uint8_t*b=buf, *b2=buf; //first pointer to index saving, other to index sending
			lg2=wg/4;		//80, divided by 4 because in while loop it chcks 4 pixels in one go so it takes 80 checks of 4 pixels to get whole row

			while(lg2--){
				//byte 1 / pixel
				while((PIND&4));//wait for low CLK
				while(!(PIND&4));//wait for high
				*b++=(PINC&15)|(PIND&240);				//save in buf using save index

				//save nothing, ommit color byte
				while((PIND&4));//wait for low
				while(!(PIND&4));//wait for high

				//byte 3 / pixel
				while((PIND&4));//wait for low
				while(!(PIND&4));//wait for high
				*b++=(PINC&15)|(PIND&240);				//save

				//save nothing, ommit color byte
				while((PIND&4));//wait for low
				while(!(PIND&4));//wait for high

			}//end collecting row data

			UDR0 = hg;	//send row number of this frame
			while(!( UCSR0A & (1<<UDRE0)));
			//mark row number section, numbers of rows are going from 119 till 0
			for(int i = 0; i< 3; i++){
				UDR0 = '?';	//mark end of row
				while(!( UCSR0A & (1<<UDRE0)));
			}


			//send row data, 160 bytes, no delimiters
			lg2=160; //-(wg/5); commented end since im not sending in upper loop
			while(lg2--){
				UDR0=*b2++;
				while(!( UCSR0A & (1<<UDRE0)));//wait for byte to transmit
			}


		}// end of pic

	#endif
}

int main(void){
	cli();//disable interrupts
	/* Setup the 8mhz PWM clock
	 * This will be on pin 11*/
	DDRB|=(1<<3);//pin 11 XCLK, used for fast pwm
	ASSR &= ~(_BV(EXCLK) | _BV(AS2));	//asynchronus clock mode?
	//setup pwm for camera clock - Xclk
	TCCR2A=(1<<COM2A0)|(1<<WGM21)|(1<<WGM20); // toggle OC2A (PB3) on comp match
	TCCR2B=(1<<WGM22)|(1<<CS20);	// fast pwm OCRA top, prescal: 1
	OCR2A=0;//(F_CPU)/(2*(X+1)) freq: 8mhz

	DDRC&=~15;//low d0-d3 camera
	DDRD&=~252;//d7-d4 and interrupt pins d3 and d2 too
	_delay_ms(3000);
	//set up twi for 100khz
	TWSR&=~3;//disable prescaler for TWI
	TWBR=72;//set to 100khz

	//enable serial
	UBRR0H=0;
	UBRR0L=1;//0 = 2M baud rate. 1 = 1M baud. 3 = 0.5M. 7 = 250k 207 is 9600 baud rate.
	UCSR0A|=(1<<U2X0);//double speed aysnc
	UCSR0B = (1<<RXEN0)|(1<<TXEN0);//Enable receiver and transmitter
	UCSR0C= (1<<UCSZ01) | (1<<UCSZ00);//async 1 stop bit 8bit char no parity bits

	camInit();

	#ifdef useVga
		setRes(VGA);
		setColorSpace(BAYER_RGB);
		wrReg(0x11,25);
	#elif defined(useQvga)
		setRes(QVGA);
		setColorSpace(YUV422);
		wrReg(0x11,12);
	#else
		setRes(QQVGA);
		setColorSpace(YUV422);
		wrReg(0x11,3);
	#endif
	/* If you are not sure what value to use here for the divider (register 0x11)
	 * Values I have found to work raw vga 25 qqvga yuv422 12 qvga yuv422 21
	 * run the commented out test below and pick the smallest value that gets a correct image */
	/*====================================
	 * 				main loop
	 ====================================*/
	while (1){
		/* captureImg operates in bytes not pixels in some cases pixels are two bytes per pixel
		 * So for the width (if you were reading 640x480) you would put 1280 if you are reading yuv422 or rgb565 */
		/*uint8_t x=63;//Uncomment this block to test divider settings note the other line you need to uncomment
		  do{
		  wrReg(0x11,x);
		  _delay_ms(1000);*/
	#ifdef useVga
			captureImg(640,480);
	#elif defined(useQvga)
			captureImg(320*2,240);
	#else
			captureImg(160*2,120);// 160*2 cuz each pixel is given in 2 bytes
	#endif
			//}while(--x);//Uncomment this line to test divider settings
	}
}


