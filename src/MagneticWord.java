import processing.core.*;

 public class MagneticWord
{
  float myX;
  float myY;
  int mySize;
  float myDecay = 300;
  int mx;
  int my;
  PImage myImage;
  PImage myThumbnail;
  int thumbX;
  String myWord;
  float myRate=1;
  MagneticWord(String inWord) {
		myWord = inWord;
		mySize=100; 
  };
  MagneticWord(String inWord, int inX, int inY) {
    
    myWord = inWord;
    myX = inX;
    myY = inY;
  }
  MagneticWord(String inWord, int inX, int inY, int inSize) {
	
    myWord = inWord;
    myX = inX;
    myY = inY;
    mySize = inSize;
    if (mySize<1) {
      mySize=10;
    }
    
  }
  public MagneticWord(String inWord, PImage newImg) {
	  	myWord = inWord;
	  	
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
	    myY = -24;
	}
public MagneticWord(String inWord, PImage newImg, double inRate) {
	myWord = inWord;
  	myRate = (float) inRate;
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
    myY = -24;
}
public MagneticWord(String inWord, double inRate) {
	myWord = inWord;
	mySize=100;
	myRate = (float)inRate;
}

}