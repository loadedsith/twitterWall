
import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.List;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.net.*;

import processing.core.*;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;




import controlP5.*;

import processing.video.*;
import s373.flob.*;


public class TwitterBallsStreaming extends PApplet {
	
	boolean showImagePosts = true;
	boolean shrinking = false;
	Capture video;
	Flob flob;
	PImage videoinput;
	int wordFallRate =1000;
	int videores = 128;
	int fps = 30;
	String topic = "What is good, internets?";
	String instructions= "Respond by tweeting @Magnetic_Tweets";
	String[] authorizedUsers = {"Magnetic_Tweets","loadedsith"};
	PFont font = loadFont("InterstateBlackCompressed-28.vlw");
	int wordsFontSize = 27;
	PVector gravityVector;
	int foreground = color(0,255);
	int background = color(255,255);
	int creditsForeground = color(0,150);
	int topicsForeground = creditsForeground;
	int[] colors = {color(155),color(200),color(255)};
	float topicFontSize;//this is set dynamically based on the lenght of the topic, once in setup() once per listenForCommand() topic update
	int flobMode = Flob.CONTINUOUS_DIFFERENCE;
	//Flob.CONTINUOUS_DIFFERENCE;
	//Flob.STATIC_DIFFERENCE;
	float creditsFontSize = 14;
	int creditsRowHeight = 20;
	boolean showcamera = false;
	boolean om = true, omset = false;
	float velmult = 10000.0f;
	int vtex = 0;
	
	public TwitterBallsStreaming theStage;
	public Timer timer = new Timer();
	public ControlFrame cf;
	public boolean clear;
	public boolean resetToDefaultFilter = false;
	//public String defaultFilter = "@Magnetic_Tweets, @Magnetic_Tweets, #Magnetic_Tweets, #Magnetic_Tweets, Magnetic_Tweets, @Loadedsith";
	public String defaultFilter = "drwho, doctorwho, biggerontheinside, #thewakingdead, #walkingdead, #dress, #amazing, #starwars, #3dprinter, #3dprinted, #diy, #Jedi, #sith, #community";
	public String filter = defaultFilter;
	private static final long serialVersionUID = -3813207765066128911L;
	String OAuthConsumerKey;
	String OAuthConsumerSecret;
	String OAuthAccessToken;
	String OAuthAccessTokenSecret;
	String kulerKey;
	String kulerQuery ="blue";
	CopyOnWriteArrayList<MagneticWord> words = new CopyOnWriteArrayList<MagneticWord>();
	CopyOnWriteArrayList<Status> queue = new CopyOnWriteArrayList<Status>();
	TwitterStream twitterStream;
	TwitterStream twitterCommandStream;
	StatusListener listener;
	UserStreamListener commandListener;
	int lastPictureEndsX = 0;
	public int lastThumbnailWidthX = 0;
	public MagneticWord keepThisOneUp;
	public int lastWordX = 0;
	boolean state = Toolkit.getDefaultToolkit().getLockingKeyState(
			KeyEvent.VK_CAPS_LOCK);

	int padding = 10;
	
	int wordsSizeLimit = 100;
	int queueIndex = 0;
	int perlinScale= 10;
	double perlinSeedIncrement= .01;
	private boolean freeze = true;
	public static void main(String args[]) {
		//PApplet.main(new String[] { "TwitterBallsStreaming" });
		PApplet.main(new String[] { "--present", "TwitterBallsStreaming" });

	}
	public void getKulerPallet() throws Exception {

		URL url = new URL("https://kuler-api.adobe.com/rss/search.cfm?key="+kulerKey+"&searchQuery="+kulerQuery);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoOutput(true);
	    conn.setUseCaches(false);
	    conn.setAllowUserInteraction(false); 
	    
	    System.out.println(conn.getResponseCode());
	    System.out.println(conn.getResponseMessage());
	    
        BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
 
		String output;
		System.out.println("Output from Server .... \n");
		String kulerRawXML = "";
		while ((output = br.readLine()) != null) {
			kulerRawXML+=output;
			
		}
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();

		Document doc = dBuilder.parse((String) kulerRawXML);
		
		if (doc.hasChildNodes()) {
			 
			println(doc.getChildNodes());
	 
		}
	 
		
		conn.disconnect();
		}
	public void setup() {

		size(1680, 1050);
		
		theStage = this;

		gravityVector = new PVector((float)0.0, (float) .08,(float) 0.0);
		
		cf = addControlFrame("extra", 800, 200);
		readKeysFile();
		textSize(24);
		topicFontSize = findFontSizeToFitThisTextToThisWidth(topic,width-100);

		frameRate(fps);
		
		setupCamera();
		setupBlobDetection();

		background(0);

		try {
			setupTwitter();
			setupCommandTwitter();
			readTimeLineForCommands();
			} catch (TwitterException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				int size = queue.size();
				if (size > 0) {
					
					if(size <= queueIndex){
						queueIndex=0;
					}
					Status status = queue.get(queueIndex++);
					//println("status.getMediaEntities():"+status.getMediaEntities());
					
					if (status.getMediaEntities().length > 0 && showImagePosts==true) {
						
						addPhotoStatusToWords(status);
				
					}else{
						try{
							addStatusToWords(status);
						} catch (Exception e) {
							e.printStackTrace();
						}
					
					}
					
					//words.add(queue.get(0));
					//while(words.size()>wordsSizeLimit){
					//	queue.remove(0);
					//}
					
				}
			}
		}, 1000, wordFallRate);
		
		
	}
	public void setupCamera(){
		video = new Capture(this, 320, 240, 15);
		video.start();

		videoinput = createImage(videores, videores, RGB);

	}
	public void setupBlobDetection(){

		flob = new Flob(this, videores, videores, width, height);

		flob.setMirror(true, false);
		flob.setThresh(10);
		flob.setFade(45);
		flob.setMinNumPixels(10);
		flob.setImage(vtex);
		
	}
	
	public void setupCommandTwitter() throws TwitterException, FileNotFoundException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb = twitterConnect(cb);
	
		twitterCommandStream = new TwitterStreamFactory(cb.build()).getInstance();
	
		commandListener = new UserStreamListener() {

			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:"
						+ statusDeletionNotice.getStatusId());
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId
						+ " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				System.out.println("Got stall warning");
			}

			@Override
			public void onStatus(Status status) {
				
				listenForCommand(status);
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				
			}

			@Override
			public void onException(Exception arg0) {
				
			}

			@Override
			public void onBlock(User arg0, User arg1) {
				
			}

			@Override
			public void onDeletionNotice(long arg0, long arg1) {

				
			}

			@Override
			public void onDirectMessage(DirectMessage directMessage) {
				//println(directMessage.getText());

			}

			@Override
			public void onFavorite(User arg0, User arg1, Status arg2) {

				
			}

			@Override
			public void onFollow(User arg0, User arg1) {

				
			}

			@Override
			public void onFriendList(long[] arg0) {
				
			}

			@Override
			public void onUnblock(User arg0, User arg1) {

				
			}

			@Override
			public void onUnfavorite(User arg0, User arg1, Status arg2) {

				
			}

			@Override
			public void onUserListCreation(User arg0, UserList arg1) {

				
			}

			@Override
			public void onUserListDeletion(User arg0, UserList arg1) {
				
				
			}

			@Override
			public void onUserListMemberAddition(User arg0, User arg1,
					UserList arg2) {
			
				
			}

			@Override
			public void onUserListMemberDeletion(User arg0, User arg1,
					UserList arg2) {
			
				
			}

			@Override
			public void onUserListSubscription(User arg0, User arg1,
					UserList arg2) {
			
				
			}

			@Override
			public void onUserListUnsubscription(User arg0, User arg1,
					UserList arg2) {
			
				
			}

			@Override
			public void onUserListUpdate(User arg0, UserList arg1) {
			
				
			}

			@Override
			public void onUserProfileUpdate(User arg0) {
			
				
			}

		};
		
		twitterCommandStream.addListener(commandListener); 
		twitterCommandStream.user(authorizedUsers);
		
	
	}
	public void setupTwitter() throws TwitterException, FileNotFoundException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb = twitterConnect(cb);
	
		twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
	
		listener = new StatusListener() {
	
			@Override
			public void onStatus(Status status) {
				queue.add(status);
				
	
			}
	
			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:"
						+ statusDeletionNotice.getStatusId());
			}
	
			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:"
						+ numberOfLimitedStatuses);
			}
	
			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId
						+ " upToStatusId:" + upToStatusId);
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
		String keywords[] = filter.split(",", -1);
	
		fq.track(keywords);
	
		twitterStream.addListener(listener);
		twitterStream.filter(fq);
		
	
	}

	@Override
	public void draw() {
		
		stroke(255);
		background(255);
		if (queue.size() > 1000&& shrinking == false) {
				twitterStream.shutdown();
				shrinking=true;
				
		}else if (queue.size() < 50 && shrinking == true) {
			shrinking = false;
			FilterQuery fq = new FilterQuery();
			// String keywords[] = {"@Magnetic_Tweets",
			// "@Magnetic_Tweets","#Magnetic_Tweets",
			// "#Magnetic_Tweets","Magnetic_Tweets","@Loadedsith",};
			String keywords[] = filter.split(",", -1);

			fq.track(keywords);

			twitterStream.addListener(listener);
			twitterStream.filter(fq);

		}
		
		if (video.available()) {
			if (!omset) {
				if (om)
					flob.setOm(flobMode);
				else
					flob.setOm(flobMode);
				omset = true;
			}

			video.read();

			// downscale video image to videoinput pimage
			videoinput.copy(video, 0, 0, 320, 240, 0, 0, videores, videores);

			flob.calcsimple(flob.binarize(videoinput));
		}


		// report presence graphically
		fill(foreground);
		//rect(0, 0, flob.getPresencef() * width, 10);

		//debugBlobs();
		drawHUD();
		collideBlobs();

		if (showcamera==true) {
			showCamera();

		}

		updateInterface();
		
		drawWords();
	}
	private void showCamera() {
		//tint(foreground);
		//fill(foreground);

		image(flob.videoimg, width - videores, height - videores);
		image(flob.videotexbin, width - 2 * videores, height - videores);
		image(flob.videotexmotion, width - 3 * videores, height - videores);
		image(flob.getSrcImage(), 0, 0, width, height);
		g.removeCache(flob.videoimg);
		g.removeCache(flob.videotexbin);
		g.removeCache(flob.videotexmotion);
		g.removeCache(flob.getSrcImage());
	}
	private void collideBlobs() {
		//collison
		float cdata[] = new float[5];
		
		for (int i = 0; i < words.size(); i++) {
			MagneticWord word = words.get(i);
			float wordXCenter = (word.myLocation.x + (word.myWidth/4));
			float wordYCenter = (word.myLocation.y - (wordsFontSize/5));
			//line(wordXCenter-3, wordYCenter,wordXCenter+3, wordYCenter);
			//line(wordXCenter, wordYCenter-3,wordXCenter, wordYCenter+3);
			
			cdata = flob.imageblobs.postcollidetrackedblobs(wordXCenter/(float) width, wordYCenter/(float) height,
					word.myWidth/2/(float)width );
			fill(126,50);
			//ellipse(wordXCenter, wordYCenter, word.myWidth/2,word.myWidth/2 );

			if (cdata[0] > 0) {
				word.toca = true;
				
				float xForce;
				float yForce;
				
				if(freeze == true){
					xForce = 0;
					yForce = 0;	
				}else{
					xForce = (float) ((float) cdata[1] * width * 0.1 );
					yForce = (float) ((float) cdata[2] * height * 0.1 )+10;	
				}
								
				
				
				word.myForces.lerp(new PVector(xForce,yForce), (float).1);
				
				
				
				
				//word.myLocation.x += cdata[1] * width * 0.1;
				//word.myLocation.y += cdata[2] * height * 0.1;
				// println("Collide W/ "+words.get(i).myWord);
			} else {
				words.get(i).toca = false;
			}
			// words.get(i).run();
			stroke(126,50);

			//line(word.myLocation.x+(word.myWidth/2),word.myLocation.y+(word.myHeight/2), (word.myLocation.x+(word.myWidth/2))+word.myForces.x*3,(word.myLocation.y-(word.myHeight/2))+word.myForces.y*3 );
			
			word.myForces.lerp(new PVector((float)0, word.myForces.y, (float)0), (float) .003);
			word.myForces.add(gravityVector);
			word.myForces.limit(90);
			word.myLocation.add(word.myForces);
			
			
		}
	}
	public void debugBlobs(){
		// get and use the data
		// int numblobs = flob.getNumBlobs();
		 int numtrackedblobs = flob.getNumTBlobs();

		 text("numblobs> "+numtrackedblobs,5,height-10);
		 
		TBlob tb;
		for(int i = 0; i < numtrackedblobs; i++) {
			tb = flob.getTBlob(i);
			rect(tb.cx, tb.cy, tb.dimx, tb.dimy );
			line(tb.cx, tb.cy, tb.cx +
			tb.velx * velmult ,tb.cy + tb.vely * velmult );
			String txt =""+tb.id+" "+tb.cx+" "+tb.cy; 
			text(txt,tb.cx, tb.cy); 
		}
		
		}
	public void drawWordThumbnails() {
		for (MagneticWord word : words) {
			if (word.myThumbnail != null) {
	
				word = drawWordThumb(word);
			}
		}
	}

	public MagneticWord drawWordThumb(MagneticWord aWord) {
	
		aWord.thumbX = 0;
		int totalX = 0;
		for (int i = words.size() - 1; i >= 0; i--) {
	
			MagneticWord word = (MagneticWord) words.get(i);
			if (word.myThumbnail != null) {
	
				tint(255, word.myDecay);
				image(word.myThumbnail, totalX, height
						- word.myThumbnail.height);
				g.removeCache(word.myThumbnail);
				totalX = totalX + word.myThumbnail.width;
	
				word.thumbX += word.myThumbnail.width;
				if (totalX + word.myThumbnail.width > width) {
	
					words.remove(word);
				}
			}
	
		}
		return aWord;
	}

	public void drawWords() {
		for (MagneticWord word : words) {
			
			
			if (word.myImage != null) {
				word = updateWord(word);
				tint(255, word.myDecay);
				image(word.myImage, word.myLocation.x- (word.myWidth/2) , word.myLocation.y -(word.myHeight/2) );
				strokeWeight(3);
				stroke(word.myColor);
				fill(0,0);
				rect( word.myLocation.x- (word.myWidth/2) , word.myLocation.y-(word.myHeight/2),word.myWidth , word.myHeight);
				g.removeCache(word.myImage);
				
		
			}
			
			
		}
		for (MagneticWord word : words) {
			if (word.myImage == null) {
			word = updateWord(word);
			word = drawWord(word);
			}
		}
		
		if (keepThisOneUp != null) {
			drawWord(keepThisOneUp);
			keepThisOneUp.myDecay = 255;
		}
		drawWordThumbnails();
	
	}
	public void drawHUD(){
		fill(topicsForeground);
		
		textSize(topicFontSize);
		
		text(topic,width-textWidth(topic)-10,50);
		
		
		textSize(creditsFontSize);
		fill(creditsForeground);
		stroke(255, 200);
		
		text("Pending Tweets" + queue.size(), 5, height - 6 * creditsRowHeight + padding);
		text("MagneticTweets", 5, height - 4 * creditsRowHeight + padding);
		text("by", 5, height - 3 * creditsRowHeight + padding);
		text("Graham.p.heath@gmail.com", 5, height - 2 * creditsRowHeight + padding);
		text("Adrienne.Canzolino@loop.colum.edu", 5, height - 1 * creditsRowHeight + padding);
		
		textSize(wordsFontSize);
		fill(creditsForeground);
		//tint(creditsForeground);
	}

	public MagneticWord drawWord(MagneticWord aWord) {
	
		
		int fontSize = 24;
		
			fill(aWord.myColor);
			//tint(foreground);
			textSize((float) (fontSize - (fontSize * .1)));
			
			text(aWord.myWord, aWord.myLocation.x - 2-(aWord.myWidth/2), aWord.myLocation.y,10);
			// println(aWord.myWord);
			if (aWord.myImage != null) {
		
				tint(255, aWord.myDecay);
		
			}
		
		fill(foreground);
		return aWord;
	
	}

	public void updateInterface() {
		if (resetToDefaultFilter) {
			println("resetToDefaultFilter");
			resetToDefaultFilter();
			clear = false;
		}
		if (clear) {
			println("clear");
			clearFilter();
			clear = false;
		}
		try {
			if (filter != cf.cp5.get(Textfield.class, "filter").getText()) {
				filter = cf.cp5.get(Textfield.class, "filter").getText();
			}
		} catch (Exception e) {
			println(e.getMessage());
		}
	}

	public MagneticWord updateWord(MagneticWord aWord) {
		
		if (aWord.myLocation.y <= height + 10) {
			//aWord.myLocation.y += aWord.myRate;
	
		} else if (aWord.myImage == null) {
	
			words.remove(aWord);
	
		}
		if(aWord.myLocation.x + aWord.width > width||aWord.myLocation.x- aWord.width  < 0){
			aWord.myForces.mult(new PVector(-1,1),1);
		}
		if (aWord.myDecay < 0) {
			words.remove(aWord);
		}
		if (aWord.myLocation.y > height && aWord.myImage == null) {
			words.remove(aWord);
		}
		if (words.size() > 3) {
			aWord.myDecay -= .2;
		}
		return aWord;
	}
	public void mouseClicked() {

		//  println("x: "+mouseX+",y:"+ mouseY);
		}
	public void readKeysFile(){
		List lines = new List();
		try {
			System.out.println("Twitter OAuth Keys should be in = " + System.getProperty("user.dir")+"/src/Files/keys.txt");
			
			FileInputStream fstream;
			try{
				fstream = new FileInputStream("src/Files/keys.txt");
			}catch(Exception e){
				throw new IllegalArgumentException("Twitter OAuth Keys should be in = " + System.getProperty("user.dir")+"src/Files/keys.txt"+e.getMessage());
			}
			
			
			DataInputStream in = new DataInputStream(fstream);
	
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				lines.add(strLine);
			}
			OAuthConsumerKey = lines.getItem(0);
			OAuthConsumerSecret = lines.getItem(1);
			OAuthAccessToken = lines.getItem(2);
			OAuthAccessTokenSecret = lines.getItem(3);
			kulerKey = lines.getItem(4);
			br.close();
			
		}
		 catch (IOException e) {
			e.printStackTrace();
			exit();
		}
	}
	public ConfigurationBuilder twitterConnect(ConfigurationBuilder cb) {
		
		cb.setOAuthConsumerKey(OAuthConsumerKey);
		cb.setOAuthConsumerSecret(OAuthConsumerSecret);
		cb.setOAuthAccessToken(OAuthAccessToken);
		cb.setOAuthAccessTokenSecret(OAuthAccessTokenSecret);
		
		return cb;
	}

	public void listenForCommand(Status status){
		println("Got a command!");
		String userName = status.getUser().getName();
		println("user: "+userName);
		if(userName.equals("Magnetic Tweets") || userName.equals("loadedsith")){
			println("Got an authed user!");
			String tweet = status.getText();
			
			if(tweet.startsWith("Topic:")){
				tweet = tweet.substring(6, tweet.length());
				while(tweet.substring(0,1)==" "||tweet.substring(0,1)==":"){
					tweet = tweet.substring(1,tweet.length());
					
				}
				topic = tweet;
				topicFontSize = findFontSizeToFitThisTextToThisWidth(topic,width-100);
	
			}
		}
		
	}

	public void readTimeLineForCommands(){
		
		println(":");
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb = twitterConnect(cb);
	
		Twitter twitterTimeLine = new TwitterFactory(cb.build()).getInstance();
		  
		  //query.setRpp(100);
		  
		 
		  //Try making the query request.
		  try {
			  ResponseList<Status> statuses = twitterTimeLine.getUserTimeline();
		   //ResponseList<Status> statuses = twitterTimeLine.getHomeTimeline();
		    //System.out.println("Showing home timeline.");
		    for (Status status : statuses) {
		        System.out.println("Home Timeline readout: Listening for commands"+status.getUser().getName() + ":" +
		                           status.getText());
		      listenForCommand(status);
		    };
		  }
		  catch (TwitterException te) {
		    println("Couldn't connect: " + te);
		  };
	    twitterTimeLine.shutdown();
	}

	public void addPhotoStatusToWords(Status status){
		String tweetText = status.getUser().getScreenName() + " - "
				+ status.getText();
		tweetText.replaceAll("\r", " ");
		tweetText.replaceAll("\n", " ");
		tweetText.replaceAll("\\r", " ");
		tweetText.replaceAll("\\n", " ");
		float aSpeed = (float) (random(1) + .3);
		int newTweetColor = color(tweetText.hashCode()); 
		newTweetColor = blendColor(newTweetColor, foreground, BLEND);
		addStatusToWords(status);
		for (MediaEntity entity : status.getMediaEntities()) {
			print(entity.getType()+", ");
			if (entity.getType().equals("photo")) {
				PImage newImage;
				try{
					newImage = loadImage(entity.getMediaURL());
				}catch(Exception e){
					newImage = null;
				}
				if(newImage != null){
					MagneticWord newWord = new MagneticWord(tweetText, newImage, aSpeed);
					newWord.myColor=color(newTweetColor);
					if ((lastPictureEndsX + newWord.myImage.width) <= width) {
		
						newWord.myLocation.x = lastPictureEndsX+newWord.myImage.width/2;
						lastPictureEndsX += newWord.myImage.width;
		
					} else {
						println("reset");
						newWord.myLocation.x = newWord.myImage.width/2;
						lastPictureEndsX = newWord.myImage.width/2;
					}
					
					words.add(newWord);
				}
				
	
			}
		}
	}
	public static String intToARGB(int i){
	    return Integer.toHexString(((i>>24)&0xFF))+
	        Integer.toHexString(((i>>16)&0xFF))+
	        Integer.toHexString(((i>>8)&0xFF))+
	        Integer.toHexString((i&0xFF));
	}
	public void addStatusToWords(Status status){
		
		String tweetText = status.getUser().getScreenName() + " - "
				+ status.getText();
		tweetText.replaceAll("\r", " ");
		tweetText.replaceAll("\n", " ");
		tweetText.replaceAll("\\r", " ");
		tweetText.replaceAll("\\n", " ");
		String[] tweetWords = tweetText.split(" ", -1);
		float aSpeed = (float) (random(1) + .5);
		int newTweetColor = blendColor(blendColor(blendColor(blendColor(foreground,color(tweetText.hashCode()),BLEND),color(tweetText.hashCode()),BLEND),color(tweetText.hashCode()),BLEND),color(tweetText.hashCode()),BLEND); 
		float seedValue = (int)tweetText.hashCode()%100;
		//println(tweetText);
		for (String tweetWord : tweetWords) {

			try{
				URL url = new URL( tweetWord );
				println("dumping link: "+url);
				break;
			}catch (Exception e){
				//regular tweet, do nothing
			}
			MagneticWord newWord = new MagneticWord(tweetWord, aSpeed);
			perlinSeedIncrement=.1;
			perlinScale=100;
			float noise = noise(seedValue+=perlinSeedIncrement)*perlinScale;
			newWord.myLocation = new PVector(lastWordX,newWord.myLocation.y+noise-100);
			newWord.myHeight = wordsFontSize;
			newWord.myWidth = textWidth(tweetWord);
			
			newWord.myColor = newTweetColor;
			
			words.add(newWord);
	
			lastWordX += textWidth(tweetWord) + 4;
			if (lastWordX > width) {
				lastWordX = 0;
			}
			
		}
		//lastWordX = (int) random(width - 100);
	}



	public float findFontSizeToFitThisTextToThisWidth(String inText,
			float inWidth) {
		float fontSize = 1;
		float size = 0;
		while (size < inWidth) {
			textSize(fontSize);
			size = textWidth(inText);
			fontSize += .5;
		}
		return fontSize;
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

	public void filter() {
		filter = cf.cp5.get(Textfield.class, "filter").getText();
	}

	public void resetToDefaultFilter() {
		println("resetToDefaultFilter()");
		filter = defaultFilter;

		cf.cp5.get(Textfield.class, "filter").setText(defaultFilter);
	}

	public void cycleMode() {
		freeze = !freeze;
		
	}

	
	public void updateFilter() {
		println("updateFilter()");
		FilterQuery fq = new FilterQuery();
		String keywords[] = filter.split(",", -1);
		println(keywords);
		fq.track(keywords);

		twitterStream.filter(fq);

	}

	public void clearFilter() {
		filter = "";
		cf.cp5.get(Textfield.class, "filter").setText("");

	}

	public void keyPressed() {
		if (key == ' '){
			flob.setBackground(videoinput);
		}
		
		if (key == 'o') {
			om ^= true;
			omset = false;
		}
		
		if (key == 'i'){
			showcamera ^= true;
		}
		
		if (key == 'v') {
			vtex = (vtex + 1) % 4;
			flob.setVideoTex(vtex);
		}
		
	}

	public void mouseReleased() {
		if (mouseY > 200) {
			int totalX = 0;
			int lastTotalX = 0;
			for (int i = words.size() - 1; i >= 0; i--) {
				MagneticWord word = (MagneticWord) words.get(i);
				if (word.myThumbnail != null) {
					totalX = totalX + word.myThumbnail.width;
					if (mouseX >= lastTotalX && mouseX <= totalX) {
						words.remove(word);
						// println("Element "+i+" removed from words");
						keepThisOneUp = null;
						break;
					}
				}
				lastTotalX = totalX;
			}
		}
	}

	public void mouseMoved() {
		if (mouseY > 200) {
			int totalX = 0;
			int lastTotalX = 0;
			for (int i = words.size() - 1; i >= 0; i--) {

				MagneticWord word = (MagneticWord) words.get(i);
				if (word.myThumbnail != null) {
					totalX = totalX + word.myThumbnail.width;
					if (mouseX >= lastTotalX && mouseX <= totalX) {
						keepThisOneUp = word;
						break;
					}

				}
				lastTotalX = totalX;
			}
		}
	}

}
