

import processing.core.PVector;
import processing.core.PImage;

 public class MagneticWord
{
  float x,y,vx,vy;
  float g = (float) 0.025;
  boolean toca=false;
  PVector myLocation;
  PVector myForces;
  float width;
  int mySize;
  float myDecay = 300;
  
  PImage myImage;
  PImage myThumbnail;
  int thumbX;
  String myWord;
  float myRate=1;
  int myColor;
  float myWidth;
  float myHeight;
  
  public MagneticWord(String inWord, PImage newImg, double inRate) {
		myLocation = new PVector();
		myForces = new PVector();
		myWord = inWord;
	  	myRate = (float) inRate;
	  	myWidth =  newImg.width;
		myHeight = newImg.height;
		try {
			myThumbnail = (PImage) newImg.clone();
			
			int old_x=myThumbnail.width;
			int old_y=myThumbnail.height;
			int thumb_w=myThumbnail.width;
			int thumb_h=myThumbnail.height;
			int new_w = 100;
			int new_h = 100;
			if (old_x > old_y) {
				thumb_w= new_w;
				thumb_h= old_y*( new_h / old_x);
			}
			if (old_x < old_y) {
				thumb_w = old_x*(new_w / old_y);
				thumb_h = new_h;
			}
			if (old_x == old_y) {
				thumb_w = new_w;
				thumb_h = new_h;
			}
			myThumbnail.resize( thumb_h, thumb_w);
			
			
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 myImage = newImg;
	    mySize=100;
	    myLocation.y = -24;
	}
public MagneticWord(String inWord, double inRate) {
	myWord = inWord;
	mySize=100;
	myRate = (float)inRate;
	myLocation = new PVector();
	myForces = new PVector();
	myForces.lerp(PVector.random2D(),(float)0.3);
	
	myWidth =  50;//default width because we need the stage to do the math for us
	myHeight = 26;//default height because we need the stage to do the math for us
}

}