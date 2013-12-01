import org.mt4j.MTApplication;
import advanced.physics.scenes.PhysicsScene;

public class INF133Assignment21 extends MTApplication
{
	private static final long serialVersionUID = 1L;

	public static void main(String args[]){
		initialize();
	}
	
	@Override
	public void startUp(){
		this.addScene(new PhysicsScene(this, "BubblePop"));
	}
}
