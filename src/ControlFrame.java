import processing.core.PApplet;
import processing.core.PFont;

import controlP5.*;

public class ControlFrame extends PApplet {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Textfield filterField;
	public String filter= "drwho, doctorwho, france, biggerontheinside, #thewakingdead, #walkingdead, #dress, #amazing, #starwars, #3dprinter, #3dprinted, #diy, #bigbang, big bang theory, #Jedi, #sith, #bigbangtheory, #community";
	public Bang reloadFilterButton;
	public Bang defaultFilterButton;
	public Bang clearButton;
	int w, h;
	PFont font = createFont("arial", 13);

	
  public void setup() {
    size(w, h);
    frameRate(25);
    cp5 = new ControlP5(this);
    filterField = cp5.addTextfield("filter")
		    .setSize(width-50, 28)
		      .setFont(font)
		        .setText(filter)
		          .setFocus(false)
		            .setColor(color(255, 0, 0))
		              ;
	
	  defaultFilterButton = cp5.addBang("defaultFilter").plugTo(parent,"resetToDefaultFilter");
	
	  defaultFilterButton.setSize(90, 40)
	    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
	      ; 
	  
	  reloadFilterButton = cp5.addBang("updateFilter").plugTo(parent,"updateFilter");
	  
	  reloadFilterButton.setSize(90, 40)
	    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)  
	      ;
	  
	  reloadFilterButton.setPosition(20, 90);
	  defaultFilterButton.setPosition(112, 90);
	  
	  filterField.setPosition(25, 25);
	  clearButton = cp5.addBang("clear").plugTo(parent,"clear");
	  clearButton.setSize(90,40);
	  clearButton.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
	  clearButton.setPosition(205,90);
  }

  public void draw() {
      background(0);
  }
  
  public ControlFrame() {
  }

  public ControlFrame(Object theParent, int theWidth, int theHeight) {
    parent = theParent;
    w = theWidth;
    h = theHeight;
  }

  public ControlP5 control() {
    return cp5;
  }
  
  
  ControlP5 cp5;

  Object parent;

  
}
