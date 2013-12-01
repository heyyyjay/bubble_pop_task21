package advanced.physics.scenes;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.font.IFont;
import org.mt4j.components.visibleComponents.shapes.MTEllipse;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.ToolsMath;
import org.mt4j.util.math.Vector3D;
import advanced.physics.physicsShapes.PhysicsCircle;
import advanced.physics.physicsShapes.PhysicsRectangle;
import advanced.physics.util.UpdatePhysicsAction;

public class PhysicsScene extends AbstractScene {
	private float timeStep = 1.0f / 60.0f;
	private int constraintIterations = 10;
	
	/** THE CANVAS SCALE **/
	private float scale = 20;
	private MTApplication app;
	private World world;
	
	//New Variables
	private int timer;
	private int score;
	
	private MTComponent physicsContainer;
	
	public PhysicsScene(MTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.app = mtApplication;
		
		//Initialize new variables
		this.score = 0;
		this.timer = 15;
		
		float worldOffset = 10; //Make Physics world slightly bigger than screen borders
		//Physics world dimensions
		AABB worldAABB = new AABB(new Vec2(-worldOffset, -worldOffset), new Vec2((app.width)/scale + worldOffset, (app.height)/scale + worldOffset));
		Vec2 gravity = new Vec2(0, 10);
//		Vec2 gravity = new Vec2(0, 0);
		boolean sleep = true;
		//Create the pyhsics world
		this.world = new World(worldAABB, gravity, sleep);
		
		this.registerGlobalInputProcessor(new CursorTracer(app, this));
		
		//Update the positions of the components according the the physics simulation each frame
		this.registerPreDrawAction(new UpdatePhysicsAction(world, timeStep, constraintIterations, scale));
		
		physicsContainer = new MTComponent(app);
		//Scale the physics container. Physics calculations work best when the dimensions are small (about 0.1 - 10 units)
		//So we make the display of the container bigger and add in turn make our physics object smaller
		physicsContainer.scale(scale, scale, 1, Vector3D.ZERO_VECTOR);
		this.getCanvas().addChild(physicsContainer);
		
		//Create borders around the screen
		this.createScreenBorders(physicsContainer);
		
		//Create text area to display Score
        IFont fontArial = FontManager.getInstance().createFont(app, "arial.ttf", 30, MTColor.BLACK, MTColor.BLACK);
        final MTTextArea scoreTextField = new MTTextArea(app, fontArial);
        scoreTextField.setPositionGlobal(new Vector3D(mtApplication.width - 200, 50, 1, 1));
        scoreTextField.setNoStroke(true);
        scoreTextField.removeAllGestureEventListeners();
        scoreTextField.setText("SCORE: " + score);
        this.getCanvas().addChild(scoreTextField);
        
		//Create text area to display Timer
        final MTTextArea timerTextField = new MTTextArea(app, fontArial);
        timerTextField.setPositionGlobal(new Vector3D(mtApplication.width - 450, 50, 1, 1));
        timerTextField.setNoStroke(true);
        timerTextField.removeAllGestureEventListeners();
        timerTextField.setText("TIME LEFT: " + timer);
        this.getCanvas().addChild(timerTextField);
       
        //Create action listener to decrement Timer
        ActionListener timerListener = new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
    			if (timer > 0)
    			{
    				//Decrement timer and update the timer text area
        			timer--;
        			timerTextField.setText("TIME LEFT: " + timer);
    			}
    			else
    			{
    				//Clear canvas
    				MTComponent[] children = getCanvas().getChildren();
    				for (int i=0; i<children.length; i++)
    				{
    					MTComponent child = children[i];
    					getCanvas().removeChild(child);
    				}
    				
    				//Create text area to display End of Game Message and Final Score
    				IFont fontArial = FontManager.getInstance().createFont(app, "arial.ttf", 30, MTColor.BLACK, MTColor.BLACK);
    		        final MTTextArea endMessage = new MTTextArea(app, fontArial);
    		        endMessage.setPositionGlobal(new Vector3D(app.width - 750, 300, 1, 1));
    		        endMessage.setNoStroke(true);
    		        endMessage.removeAllGestureEventListeners();
    		        endMessage.setText("You are an awesome Bubble Popper!\nYour Score: " + score);
    		        getCanvas().addChild(endMessage);
    			}
        	}
        };
        
        //Start Timer with Action Listener
        Timer timer = new Timer(1000, timerListener);
        timer.start();
		
		//Create 80 bubbles for popping
		for (int i = 0; i < 80; i++) {
			PhysicsCircle c = new PhysicsCircle(app, new Vector3D(ToolsMath.getRandom(60, mtApplication.width-60), ToolsMath.getRandom(60, mtApplication.height-60)), 50, world, 1.0f, 0.3f, 0.4f, scale);
			MTColor col = new MTColor(ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255));
			c.setFillColor(col);
			c.setStrokeColor(col);
			c.registerInputProcessor(new TapProcessor(app));
			c.addGestureListener(TapProcessor.class,  new IGestureEventListener()
				{
					public boolean processGestureEvent(MTGestureEvent ge) {
						//Check for Tap Event
						TapEvent te = (TapEvent)ge;
						IMTComponent3D target = te.getTargetComponent();
						if (target instanceof MTEllipse) {
					        MTEllipse bubble = (MTEllipse) target;
							switch (te.getTapID()) {
								case TapEvent.BUTTON_DOWN:
									//Destroy bubble, update score and score text area
									bubble.destroy();
									score = score + 10;
									scoreTextField.setText("SCORE: " + score);
									break;
								default:
									break;
							}
						}
						return false;
					}
				});
			physicsContainer.addChild(c);
		}
	}
	
	private void createScreenBorders(MTComponent parent){
		//Left border 
		float borderWidth = 50f;
		float borderHeight = app.height;
		Vector3D pos = new Vector3D(-(borderWidth/2f) , app.height/2f);
		PhysicsRectangle borderLeft = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderLeft.setName("borderLeft");
		parent.addChild(borderLeft);
		//Right border
		pos = new Vector3D(app.width + (borderWidth/2), app.height/2);
		PhysicsRectangle borderRight = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderRight.setName("borderRight");
		parent.addChild(borderRight);
		//Top border
		borderWidth = app.width;
		borderHeight = 50f;
		pos = new Vector3D(app.width/2, -(borderHeight/2));
		PhysicsRectangle borderTop = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderTop.setName("borderTop");
		parent.addChild(borderTop);
		//Bottom border
		pos = new Vector3D(app.width/2 , app.height + (borderHeight/2));
		PhysicsRectangle borderBottom = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderBottom.setName("borderBottom");
		parent.addChild(borderBottom);
	}

	@Override
	public void init() {
	}

	@Override
	public void shutDown() {
	}

}
