
import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.List;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

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

import controlP5.*;

import processing.video.*;
import s373.flob.*;

public class TwitterBallsStreaming extends PApplet {
	
	
	boolean shrinking = false;
	Capture video;
	Flob flob;
	PImage videoinput;
	
	int videores = 128;
	int fps = 60;
	PFont font = createFont("arial", 10);

	boolean showcamera = false;
	boolean om = true, omset = false;
	float velmult = 10000.0f;
	int vtex = 0;

	public TwitterBallsStreaming theStage;
	public Timer timer = new Timer();
	public ControlFrame cf;
	public boolean clear;
	public boolean resetToDefaultFilter = false;
	public String defaultFilter = "drwho, doctorwho, biggerontheinside, #thewakingdead, #walkingdead, #dress, #amazing, #starwars, #3dprinter, #3dprinted, #diy, #Jedi, #sith, #community";
	public String filter = defaultFilter;
	private static final long serialVersionUID = -3813207765066128911L;
	CopyOnWriteArrayList<MagneticWord> words;
	CopyOnWriteArrayList<MagneticWord> queue;
	TwitterStream twitterStream;
	StatusListener listener;
	int lastPictureEndsX = 0;
	public int lastThumbnailWidthX = 0;
	public MagneticWord keepThisOneUp;
	public int lastWordX = 0;
	boolean state = Toolkit.getDefaultToolkit().getLockingKeyState(
			KeyEvent.VK_CAPS_LOCK);

	int padding = 10;

	public static void main(String args[]) {
		// PApplet.main(new String[] { "TwitterBallsStreaming" });
		PApplet.main(new String[] { "--present", "TwitterBallsStreaming" });

	}

	public void setup() {

		cf = addControlFrame("extra", 800, 200);
		textSize(24);
		theStage = this;
		size(1680, 1050);
		frameRate(fps);

		video = new Capture(this, 320, 240, fps);
		video.start();

		videoinput = createImage(videores, videores, RGB);

		flob = new Flob(this, videores, videores, width, height);

		flob.setMirror(true, false);
		flob.setThresh(10);
		flob.setFade(45);
		flob.setMinNumPixels(10);
		flob.setImage(vtex);

		words = new CopyOnWriteArrayList<MagneticWord>();
		queue = new CopyOnWriteArrayList<MagneticWord>();

		background(0);

		try {
			setupTwitter();
		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {

				if (queue.size() > 0) {
					words.add(queue.get(0));
					queue.remove(0);
				}
			}
		}, 1000, 150);
	}

	@Override
	public void draw() {
		stroke(255);
		background(0);
		if (queue.size() > 500) {
				twitterStream.shutdown();
				shrinking=true;
				
		}
		if (queue.size() < 50 && shrinking == true) {
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
					flob.setOm(Flob.CONTINUOUS_DIFFERENCE);
				else
					flob.setOm(Flob.STATIC_DIFFERENCE);
				omset = true;
			}

			video.read();

			// downscale video image to videoinput pimage
			videoinput.copy(video, 0, 0, 320, 240, 0, 0, videores, videores);

			flob.calcsimple(flob.binarize(videoinput));
		}

		fill(255, 255);
		image(flob.getSrcImage(), 0, 0, width, height);

		// report presence graphically
		fill(255, 152, 255);
		rect(0, 0, flob.getPresencef() * width, 10);

		fill(255, 100);
		stroke(255, 200);
		// get and use the data
		// int numblobs = flob.getNumBlobs();
		int numtrackedblobs = flob.getNumTBlobs();

		// text("numblobs> "+numtrackedblobs,5,height-10);
		int row = 20;
		textSize(14);
		fill(255, 150);
		text("Pending Tweets" + queue.size(), 5, height - 6 * row + padding);
		text("MagneticTweets", 5, height - 4 * row + padding);
		text("by", 5, height - 3 * row + padding);
		text("Graham.p.heath@gmail.com", 5, height - 2 * row + padding);
		text("Adrienne.Canzolino@loop.colum.edu", 5, height - 1 * row + padding);
		textSize(27);
		fill(255, 255);
		// TBlob tb;
		/*
		 * for(int i = 0; i < numtrackedblobs; i++) { tb = flob.getTBlob(i);
		 * rect(tb.cx, tb.cy, tb.dimx, tb.dimy ); line(tb.cx, tb.cy, tb.cx +
		 * tb.velx * velmult ,tb.cy + tb.vely * velmult ); String txt =
		 * ""+tb.id+" "+tb.cx+" "+tb.cy; text(txt,tb.cx, tb.cy); }
		 */
		// collison
		float cdata[] = new float[5];
		for (int i = 0; i < words.size(); i++) {
			float x = words.get(i).myX / (float) width;
			float y = words.get(i).myY / (float) height;

			cdata = flob.imageblobs.postcollidetrackedblobs(x, y,
					textWidth(words.get(i).myWord) / (float) width);

			if (cdata[0] > 0) {
				words.get(i).toca = true;
				words.get(i).myX += cdata[1] * width * 0.15;
				words.get(i).myY += cdata[2] * height * 0.15;
				// println("Collide W/ "+words.get(i).myWord);
			} else {
				words.get(i).toca = false;
			}
			// words.get(i).run();
		}

		if (showcamera) {
			tint(255, 150);
			image(flob.videoimg, width - videores, height - videores);
			image(flob.videotexbin, width - 2 * videores, height - videores);
			image(flob.videotexmotion, width - 3 * videores, height - videores);
		}

		fill(255, 10);
		rectMode(CENTER);
		stroke(127, 200);

		updateInterface();
		fill(255, 255);
		drawWords();
	}

	public ConfigurationBuilder twitterConnect(ConfigurationBuilder cb) {
		List lines = new List();
		try {
			FileInputStream fstream = new FileInputStream("src/Files/keys.txt");
			DataInputStream in = new DataInputStream(fstream);

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				lines.add(strLine);
			}
			cb.setOAuthConsumerKey(lines.getItem(0));
			cb.setOAuthConsumerSecret(lines.getItem(1));
			cb.setOAuthAccessToken(lines.getItem(2));
			cb.setOAuthAccessTokenSecret(lines.getItem(3));
		} catch (IOException e) {
			e.printStackTrace();
			exit();
		}
		return cb;
	}

	public void setupTwitter() throws TwitterException, FileNotFoundException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb = twitterConnect(cb);

		twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

		listener = new StatusListener() {

			@Override
			public void onStatus(Status status) {
				String input = status.getUser().getScreenName() + " - "
						+ status.getText();
				float aSpeed = (float) (random(1) + .5);
				if (status.getMediaEntities() != null) {

					for (MediaEntity entity : status.getMediaEntities()) {

						if (entity.getType().equals("photo")) {

							MagneticWord newWord = new MagneticWord(input,
									loadImage(entity.getMediaURL()), aSpeed);

							if ((lastPictureEndsX + newWord.myImage.width) <= width) {

								newWord.myX = lastPictureEndsX;
								lastPictureEndsX += newWord.myImage.width;

							} else {
								println("reset");
								newWord.myX = 0;
								lastPictureEndsX = 0;
							}

							queue.add(newWord);

						}
					}

				}
				String[] tweetWords = input.split(" ", -1);
				for (String tweetWord : tweetWords) {
					MagneticWord newWord = new MagneticWord(tweetWord, aSpeed);
					newWord.myX = lastWordX;

					queue.add(newWord);

					lastWordX += textWidth(tweetWord) + 4;
					if (lastWordX > width) {
						lastWordX = padding;
					}

				}
				lastWordX = (int) random(width - 100);

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
		// String keywords[] = {"@Magnetic_Tweets",
		// "@Magnetic_Tweets","#Magnetic_Tweets",
		// "#Magnetic_Tweets","Magnetic_Tweets","@Loadedsith",};
		String keywords[] = filter.split(",", -1);

		fq.track(keywords);

		twitterStream.addListener(listener);
		twitterStream.filter(fq);

	}

	public void queuePop() {

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

	public void drawWordThumbnails() {
		// for (int i = words.size()-1; i >= 0 ; i--) {
		// if(words.get(i)!=null){
		for (MagneticWord word : words) {
			// MagneticWord word = (MagneticWord) words.get(i);
			if (word.myThumbnail != null) {

				word = drawWordThumb(word);
			}
			// }
		}
	}

	public MagneticWord drawWordThumb(MagneticWord aWord) {

		aWord.thumbX = 0;
		int totalX = 0;
		/*
		 * for (int i = words.size()-1; i >= 0 ; i--) {
		 * 
		 * MagneticWord word = (MagneticWord) words.get(i);
		 * if(word.myThumbnail!=null){ totalX = totalX+word.myThumbnail.width;
		 * if(mouseX>=lastTotalX&&mouseX<=totalX){ keepThisOneUp = word; break;
		 * }
		 * 
		 * } lastTotalX=totalX; }
		 */
		for (int i = words.size() - 1; i >= 0; i--) {

			MagneticWord word = (MagneticWord) words.get(i);
			if (word.myThumbnail != null) {

				tint(255, word.myDecay);
				image(word.myThumbnail, totalX, height
						- word.myThumbnail.height);
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

			word = updateWord(word);
			word = drawWord(word);

		}
		if (keepThisOneUp != null) {
			drawWord(keepThisOneUp);
			keepThisOneUp.myDecay = 255;
		}
		drawWordThumbnails();

	}

	public MagneticWord updateWord(MagneticWord aWord) {
		if (aWord.myY <= height + 10) {
			aWord.myY += aWord.myRate;

		} else if (aWord.myImage == null) {

			words.remove(aWord);

		}
		if (aWord.myDecay < 0) {
			words.remove(aWord);
		}
		if (aWord.myY > height && aWord.myImage == null) {
			words.remove(aWord);
		}
		if (words.size() > 3) {
			aWord.myDecay -= .2;

			tint(255, aWord.myDecay);
		}
		return aWord;
	}

	public float getFontSizeToFitThisTextToThisWidth(String inText,
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

	public MagneticWord drawWord(MagneticWord aWord) {

		int testWidth;

		if (aWord.myImage != null) {
			testWidth = aWord.myImage.width - 5;
		} else {
			testWidth = width - 5;

		}
		// int fontSize = (int)
		// getFontSizeToFitThisTextToThisWidth(aWord.myWord,testWidth);
		int fontSize = 24;
		if (aWord.myImage != null) {
			tint(255, aWord.myDecay);
			image(aWord.myImage, aWord.myX, aWord.myY);

		}
		textSize((float) (fontSize - (fontSize * .1)));
		text(aWord.myWord, aWord.myX + 2, aWord.myY);
		// println(aWord.myWord);
		if (aWord.myImage != null) {

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

	public void filter() {
		filter = cf.cp5.get(Textfield.class, "filter").getText();
	}

	public void resetToDefaultFilter() {
		println("resetToDefaultFilter()");
		filter = defaultFilter;

		cf.cp5.get(Textfield.class, "filter").setText(defaultFilter);
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
		if (key == ' ')
			flob.setBackground(videoinput);
		if (key == 'o') {
			om ^= true;
			omset = false;
		}
		if (key == 'i')
			showcamera ^= true;
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
