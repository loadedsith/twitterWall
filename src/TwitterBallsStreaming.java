

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
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    
  }

	
	@Override
	public void draw() {
	    stroke(255);
	    background(0);
	    if(state){
	    	cf.cp5.hide();
	    }else{
	    	cf.cp5.show();
	    	}
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
	    if(filter!=cf.cp5.get(Textfield.class,"filter").getText()){
	    	filter=cf.cp5.get(Textfield.class,"filter").getText();
	    }
	    
	    /*
		thumbX = 0;
		
	    for (int i = words.size()-1; i >= 0 ; i--) {
	    	MagneticWord word = (MagneticWord) words.get(i);
			if(word!=this){
				if(word.myThumbnail!=null){
					thumbX+=word.myThumbnail.width;
				}
			}else{
				break;
			}
			
		}
	     */
	    for (MagneticWord word :words) {
	    	if(lastThumbnailWidthX>width){
	     		lastThumbnailWidthX=0;
	     		words.remove(0);
	     		} 
	    	
	    	word.update();
	    	word.draw();
	    	 
	    	}
	    if(keepThisOneUp!=null){
	    	keepThisOneUp.draw();
	    	keepThisOneUp.myDecay = 255;
	
	    }
	    for (int i = words.size()-1; i >= 0 ; i--) {
	    	/*/
	    	 * 	//lastThumbnailWidthX = lastThumbnailWidthX+myThumbnail.width;
			
	    	 *if((lastPictureEndsX+myImage.width)<=width){
		    	  
		    	  myX = lastPictureEndsX;
		    	  lastPictureEndsX += myImage.width;
		    	  
		      }else{
		    	  print("reset");
		    	  myX = 0;
		    	  lastPictureEndsX =0;
		      }
		      print(myX+" , "+myY);
	    	 */
	    	MagneticWord word = (MagneticWord) words.get(i);
	    	tint(255,word.myDecay);
	    	if(word.myThumbnail!=null){
	    		image(word.myThumbnail, word.thumbX, height-word.myThumbnail.height);
	    		}
	    	}
	  
	
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
		println("bonk");	
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
      cb.setOAuthConsumerKey("xxx");
      cb.setOAuthConsumerSecret("xxx");
      cb.setOAuthAccessToken("xxx-xxx");
      cb.setOAuthAccessTokenSecret("xxx");

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
              	
                words.add( new MagneticWord(theStage,input,NewImg));
              }else{
            	words.add( new MagneticWord(theStage,input));
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
