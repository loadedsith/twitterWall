

import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import processing.core.*;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.awt.Frame;
import controlP5.*;


public class TwitterBallsStreaming extends PApplet{
	public TwitterBallsStreaming theStage;
	
	public ControlFrame cf;
	public boolean clear;
	public boolean resetToDefaultFilter = false;
	public String defaultFilter = "drwho, doctorwho, france, biggerontheinside, #thewakingdead, #walkingdead, #dress, #amazing, #starwars, #3dprinter, #3dprinted, #diy, #bigbang, big bang theory, #Jedi, #sith, #bigbangtheory, #community";
	public String filter= defaultFilter;
	private static final long serialVersionUID = -3813207765066128911L;
	CopyOnWriteArrayList<MagneticWord> words;
	TwitterStream twitterStream;
	StatusListener listener;
	int lastPictureEndsX= 0;
	public int lastThumbnailWidthX = 0;
	public MagneticWord keepThisOneUp;
	
	boolean state= Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
	
    int padding = 10;
	public static void main(String args[]) {
	    PApplet.main(new String[] {  "TwitterBallsStreaming" });
	    
	  }
	public void setup() {
		
		cf = addControlFrame("extra", 800,200);
		
		theStage = this;
		size(1240,600);
		words = new CopyOnWriteArrayList<MagneticWord>();
	    background(0);
	     try {
	      setupTwitter();
	    } catch (TwitterException e) {
	      e.printStackTrace();
	    }
	    
  }

	public void updateInterface(){
		 if(resetToDefaultFilter){
		    	println("resetToDefaultFilter");
			resetToDefaultFilter();
			clear=false;
		}
		if(clear){
			println("clear");
			clearFilter();
			clear=false;
		}
		try{
			if(filter!=cf.cp5.get(Textfield.class,"filter").getText()){
				filter=cf.cp5.get(Textfield.class,"filter").getText();
			}
		}catch(Exception e){
			println(e.getMessage());
		}
	}
	public void drawWordThumbnails(){
		for (int i = words.size()-1; i >= 0 ; i--) {
	    	MagneticWord word = (MagneticWord) words.get(i);
	    	word = drawWordThumb(word);
			
	    	}
	}
	public MagneticWord drawWordThumb(MagneticWord aWord){
		 
		aWord.thumbX = 0;
		
	    for (int i = words.size()-1; i >= 0 ; i--) {
	    	MagneticWord word = (MagneticWord) words.get(i);
			if(word!=aWord){
				if(word.myThumbnail!=null){
					aWord.thumbX+=word.myThumbnail.width;
				}
			}else{
				break;
			}
			
		}
	    return aWord;
	}

	public void drawWords(){
		for (MagneticWord word :words) {
	    	if(lastThumbnailWidthX>width){
	     		lastThumbnailWidthX=0;
	     		words.remove(0);
	     		} 
	    	
	    	word = updateWord(word);
	    	word = drawWord(word);
	    	
	    	}
		drawWordThumbnails();
	    if(keepThisOneUp!=null){
	    	drawWord(keepThisOneUp);
	    	keepThisOneUp.myDecay = 255;	
	    }

	}
	
	@Override
	public void draw() {
	    stroke(255);
	    background(0);
	    updateInterface();
	    drawWords();
	  }
public float getFontSizeToFitThisTextToThisWidth(String inText, float inWidth){
	float fontSize = 1;
	float size = 0;
	while(size < inWidth){
	      textSize(fontSize);
	      size = textWidth(inText);
	      fontSize+=.5; 
	    }
	return fontSize;
}
public MagneticWord drawWord(MagneticWord aWord){
    
    
    int testWidth;

    if(aWord.myImage!=null){
    	testWidth = aWord.myImage.width-5;
    }else{
    	testWidth = width-5;
    	
    }
    int fontSize = (int) getFontSizeToFitThisTextToThisWidth(aWord.myWord,testWidth);
    
    if(aWord.myImage!=null){
    	tint(255, aWord.myDecay);
    	image(aWord.myImage, aWord.myX,height-aWord.myY);
    	 
    }
    textSize((float) (fontSize-(fontSize*.1)));
    text(aWord.myWord, aWord.myX+2, height-aWord.myY);
    
    if(aWord.myImage!=null){
    	
   	 tint(255,aWord.myDecay);
   	 
   	 if(aWord.myThumbnail!=null){
   		image(aWord.myThumbnail, aWord.thumbX, height-aWord.myThumbnail.height);
   		}
   	 
	}
    return aWord;
	  
}
public MagneticWord updateWord(MagneticWord aWord){
	if (aWord.myY <= height+10) {
			aWord.myY+=aWord.myRate;
			
	    }else if (aWord.myImage==null){
	    	
	    	words.remove(aWord);
	    	
	    }
	if(aWord.myDecay<0){
		words.remove(aWord);
	}
	if(words.size()>3){
		aWord.myDecay -=.2;
		
    	tint(255, aWord.myDecay);
	}
	return aWord;
}
	ControlFrame addControlFrame(String theName, int theWidth, int theHeight) {
		  Frame f = new Frame(theName);
		  ControlFrame p = new ControlFrame(this, theWidth, theHeight);
		  f.add(p);
		  p.init();
		  f.setTitle(theName);
		  f.setSize(p.w, p.h);
		  f.setLocation(100, 100);
		  f.setResizable(false);
		  f.setVisible(true);
		  return p;
		}
	
	public void filter(){
		filter=cf.cp5.get(Textfield.class,"filter").getText();
	}
	public void resetToDefaultFilter(){
		println("resetToDefaultFilter()");
		filter=defaultFilter;
		
		cf.cp5.get(Textfield.class,"filter").setText(defaultFilter);
	}
	public void updateFilter(){
		println("updateFilter()");
		 FilterQuery fq = new FilterQuery();
	      //String keywords[] = {"@Magnetic_Tweets", "@Magnetic_Tweets","#Magnetic_Tweets",
	        //                       "#Magnetic_Tweets","Magnetic_Tweets","@Loadedsith",};
	      String keywords[] = filter.split(",", -1);
	      println(keywords);    
	      fq.track(keywords);

	
	      twitterStream.filter(fq);
	
	}
	public void clearFilter(){
		filter="";
		cf.cp5.get(Textfield.class,"filter").setText("");
		
	}
    public  void setupTwitter() throws TwitterException{
      
      ConfigurationBuilder cb = new ConfigurationBuilder();
      cb.setOAuthConsumerKey("1fTyqjPVm602kiT7w5M5Rw");
      cb.setOAuthConsumerSecret("ZORA2RpOIZdi9zNZR1BfPzRoVjOY4W1FYhjHzuU8");
      cb.setOAuthAccessToken("153055060-HpgWFAbN3kuj36d2In2Nde1VVFmEiwktyIRwSMJM");
      cb.setOAuthAccessTokenSecret("NoCa6Wu8FNKvnsDZrOWK6ZL9nuyrAEmZgl0itsLkyCI");

       twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
       
       listener = new StatusListener() {

          @Override
       public void onStatus(Status status) {
              //System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
              //System.out.println(status.getCreatedAt());
              String input = status.getUser().getScreenName() + " - " + status.getText();
              
               
                //Put each word into the words ArrayList
              PImage NewImg = null;
              if(status.getMediaEntities()!=null){
            	  
            	  for(MediaEntity entity : status.getMediaEntities()){
            		  if(entity.getType().equals("photo")){
            			   NewImg = loadImage(entity.getMediaURL());
            			  
            		  }
            	  }
            	  
              }
              if(NewImg != null){
              	MagneticWord newWord = new MagneticWord(input,NewImg, random(1)+.25);
                
              	if((lastPictureEndsX+newWord.myImage.width)<=width){
    	    	  
              		newWord.myX = lastPictureEndsX;
        			lastPictureEndsX += newWord.myImage.width;
    	    	  
              	}else{
              		println("reset");
              		newWord.myX = 0;
              		lastPictureEndsX =0;
              	}
              	
              	
              	words.add( newWord);
                
    	      
              }else{
            	words.add( new MagneticWord(input, random(1)+.25));
              }
              
          }

          @Override
       public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
              System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
          }

          @Override
       public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
              System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
          }

          @Override
       public void onScrubGeo(long userId, long upToStatusId) {
              System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
          }

          @Override
       public void onException(Exception ex) {
              ex.printStackTrace();
          }

       @Override
       public void onStallWarning(StallWarning arg0) {

              System.out.println("Got stall warning");

         
       }
      };

      FilterQuery fq = new FilterQuery();
      //String keywords[] = {"@Magnetic_Tweets", "@Magnetic_Tweets","#Magnetic_Tweets",
        //                       "#Magnetic_Tweets","Magnetic_Tweets","@Loadedsith",};
      String keywords[] = filter.split(",", -1);
      
      
       
      
      fq.track(keywords);

      twitterStream.addListener(listener);
      twitterStream.filter(fq);
      
    }


  public void mouseReleased(){
	  if(mouseY>200){
  		  int totalX=0;
  		  int lastTotalX=0;
  		   for (int i = words.size()-1; i >= 0 ; i--) {
  		    	MagneticWord word = (MagneticWord) words.get(i);
  		    	if(word.myThumbnail!= null){
  		    		totalX = totalX+word.myThumbnail.width;
  		    		if(mouseX>=lastTotalX&&mouseX<=totalX){
  		    			words.remove(word);
  		    			println("1");
  		    			keepThisOneUp = null;
  		    			break;
  		    		}
  		    	}
  		    	lastTotalX=totalX;
  		    }
  	    }
  }
  public void mouseMoved() {
	  if(mouseY>200){
		  int totalX=0;
		  int lastTotalX=0;
		   for (int i = words.size()-1; i >= 0 ; i--) {
  
		    	MagneticWord word = (MagneticWord) words.get(i);
		    	if(word.myThumbnail!=null){
		    		totalX = totalX+word.myThumbnail.width;
		 			if(mouseX>=lastTotalX&&mouseX<=totalX){
		    				keepThisOneUp = word;
		    				break;
		 			}
		    		
		    	}
		    lastTotalX=totalX;
		    }
	    }
	}

}
