package eca.spas;

import java.util.ArrayList;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import tracing.ITracer;
import eca.construct.Action;
import eca.construct.Observation;
import eca.ss.enaction.Enaction;

/**
 * The spatial system.
 * Maintains the local space map and the persistence memory.
 * @author Olivier
 */
public interface ISpas 
{

	/**
	 * @param tracer The tracer
	 */
	public void setTracer(ITracer tracer);
	
	/**
	 * The main routine of the Spatial System that is called on each interaction cycle.
	 * @param enaction The current enaction.
	 */
	public void track(Enaction enaction);

	/**
	 * Provide a rgb code to display the local space map in the environment.
	 * @param i x coordinate.
	 * @param j y coordinate.
	 * @return The value of the bundle in this place in local space memory.
	 */
	public int getValue(int i, int j);
	
	/**
	 * Return the identifier of an area to which a point belongs.
	 * @param point The point.
	 * @return The code of the area.
	 */
	//public Area categorizePosition(Point3f point);

	/**
	 * @return The list of places in Ernest's local space memory.
	 */
	public ArrayList<Place> getPlaceList();
	
	/**
	 * Tick the clock of spatial memory
	 */
	public void tick();
	
	/**
	 * @param position
	 * @return The value to display at this position
	 */
	public int getValue(Point3f position);
	
	//public ISpatialMemory getSpatialMemory();
	
	/**
	 * @param action The next action
	 * @return The Phenomenon instance to which the next action will apply 
	 */
	public Observation predictPhenomenonInst(Action action);
	
	/**
	 * @return The transformation of spatial memory to anim.
	 */
	public Transform3D getTransformToAnim();
}
