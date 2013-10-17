import processing.core.PApplet;
import processing.core.PFont;
import controlP5.*;

public class ControlFrame extends PApplet {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Textfield filterField;
	public String filter= "drwho, doctorwho, biggerontheinside, #thewakingdead, #walkingdead, #dress, #amazing, #starwars, #3dprinter, #3dprinted, #diy, #Jedi, #sith, #community";

	public Bang reloadFilterButton;
	public Bang defaultFilterButton;
	public Bang cycleModeButton;
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
	
      reloadFilterButton = cp5.addBang("updateFilter").plugTo(parent,"updateFilter");
	  
	  reloadFilterButton.setSize(90, 40)
	    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)  
	      ;
	
	  defaultFilterButton = cp5.addBang("defaultFilter").plugTo(parent,"resetToDefaultFilter");
	
	  defaultFilterButton.setSize(90, 40)
	    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
	      ; 
	  
	  clearButton = cp5.addBang("clear").plugTo(parent,"clear");
	  clearButton.setSize(90,40);
	  clearButton.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
	  
	  cycleModeButton = cp5.addBang("cycleMode").plugTo(parent,"cycleMode");
	  
	  cycleModeButton.setSize(90, 40)
	    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)  
	      ;
	  
	  
	  
	  int leftPadding = 25;
	  int topPadding = 25;
	  int rowHeight = 90;
	  int rowCount = 0;
	  int buttonColumnCount = 0;
	  int buttonWidth = 90;
	  filterField.setPosition(leftPadding, topPadding+rowCount*rowHeight);
	  rowCount++;
	  
	  reloadFilterButton.setPosition(leftPadding+buttonWidth*buttonColumnCount++, topPadding+rowCount*rowHeight);
	  defaultFilterButton.setPosition(leftPadding+buttonWidth*buttonColumnCount++, topPadding+rowCount*rowHeight);
	  clearButton.setPosition(leftPadding+buttonWidth*buttonColumnCount++,topPadding+rowCount*rowHeight);
	  cycleModeButton.setPosition(leftPadding+buttonWidth*buttonColumnCount++, topPadding+rowCount*rowHeight);
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
