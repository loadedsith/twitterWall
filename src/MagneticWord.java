import processing.core.*;

 public class MagneticWord
{
  int myX;
  int myY;
  int mySize;
  float myDecay;
  int mx;
  int my;
  String word;
  PImage myImage;
  PImage myThumbnail;
  int thumbX;
  TwitterBallsStreaming stage;
 

  MagneticWord(TwitterBallsStreaming inStage,String inWord) {
		stage = inStage;
//		font2=stage.createFont("arial", 20);
		word = inWord;
		mySize=100; 
  };
  MagneticWord(String inWord, int inX, int inY) {
    
    word = inWord;
    myX = inX;
    myY = inY;
  }
  MagneticWord(TwitterBallsStreaming inStage,String inWord, int inX, int inY, int inSize) {
	stage = inStage;
    word = inWord;
    myX = inX;
    myY = inY;
    mySize = inSize;
    if (mySize<1) {
      mySize=10;
    }
    
  }
  public MagneticWord(TwitterBallsStreaming inStage,String inWord, PImage newImg) {
	  word = inWord;
	  stage=inStage;
	  myDecay = 255;
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
	      
	    
	    myY = 0;
	    
	  
}
void draw() {
    
    
    int fontSize = 1;
    float myWidth = 5;
    int testWidth = 0;
    
    if(myImage!=null){
    	testWidth = myImage.width-5;
    }else{
    	testWidth = stage.getWidth()-5;
    	
    }
    while(myWidth < testWidth){
      stage.textSize(fontSize);
      myWidth = stage.textWidth(word);
      fontSize++; 
    }
    
    if(myImage!=null){
    	stage.tint(255, myDecay);
    	 stage.image(myImage, myX,stage.getHeight()-myY);
    	 
    }
    //stage.textFont(font2);
    stage.textSize((float) (fontSize-(fontSize*.1)));
    stage.text(word, myX+2, stage.getHeight()-myY);
  
  }

  void update() {
    //println("updating" + myX + ", " + myY);

    if (myY <= (stage.getHeight())) {
      myY++;
    }
    else {
    	
    	if(stage.words.size()>3){
	    	myDecay -=.2;
	    	stage.tint(255, myDecay);
	    	
	    	if(myDecay<0){
	    		stage.words.remove(this);
	    	}
    	}
       // words.remove(this);
      }
  }
}