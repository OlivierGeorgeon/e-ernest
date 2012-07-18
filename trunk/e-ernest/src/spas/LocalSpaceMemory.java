package spas;

import imos.IAct;
import imos.ISchema;

import java.util.ArrayList;
import java.util.Iterator;
import javax.vecmath.Vector3f;
import utils.ErnestUtils;
import ernest.Ernest;
import ernest.ITracer;

/**
 * Ernest's spatial memory. 
 * @author Olivier
 */
public class LocalSpaceMemory implements ISpatialMemory, Cloneable
{
	
	/** The radius of a location. */
	public final static float LOCATION_RADIUS = 0.5f;
	public final static float LOCAL_SPACE_MEMORY_RADIUS = 20f;//4f;
	public final static float DISTANCE_VISUAL_BACKGROUND = 10f;
	public final static float EXTRAPERSONAL_DISTANCE = 1.5f;
	public final static float DIAG2D_PROJ = (float) (1/Math.sqrt(2));
	public final static Vector3f DIRECTION_HERE         = new Vector3f(0, 0, 0);
	public final static Vector3f DIRECTION_AHEAD        = new Vector3f(1, 0, 0);
	public final static Vector3f DIRECTION_BEHIND       = new Vector3f(-1, 0, 0);
	public final static Vector3f DIRECTION_LEFT         = new Vector3f(0, 1, 0);
	public final static Vector3f DIRECTION_RIGHT        = new Vector3f(0, -1, 0);
	public final static Vector3f DIRECTION_AHEAD_LEFT   = new Vector3f(DIAG2D_PROJ, DIAG2D_PROJ, 0);
	public final static Vector3f DIRECTION_AHEAD_RIGHT  = new Vector3f(DIAG2D_PROJ, -DIAG2D_PROJ, 0);
	public final static Vector3f DIRECTION_BEHIND_LEFT  = new Vector3f(-DIAG2D_PROJ, DIAG2D_PROJ, 0);
	public final static Vector3f DIRECTION_BEHIND_RIGHT = new Vector3f(-DIAG2D_PROJ, -DIAG2D_PROJ, 0);	
	public final static float    SOMATO_RADIUS = 1f;
	
	/** The duration of persistence in local space memory. */
	public static int PERSISTENCE_DURATION = 10;//50;
	
	/** The Local space structure. */
	private ArrayList<IPlace> m_places = new ArrayList<IPlace>();
	
	private int m_clock = 0;
		
	/**
	 * Clone spatial memory to perform simulations
	 * TODO clone the places 
	 * From tutorial here: http://ydisanto.developpez.com/tutoriels/java/cloneable/ 
	 * @return The cloned spatial memory
	 */
	public ISpatialMemory clone() 
	{
		LocalSpaceMemory cloneSpatialMemory = null;
		try {
			cloneSpatialMemory = (LocalSpaceMemory) super.clone();
		} catch(CloneNotSupportedException cnse) {
			cnse.printStackTrace(System.err);
		}

		// We must clone the place list because it is passed by reference by default

		ArrayList<IPlace> clonePlaces = new ArrayList<IPlace>();
		for (IPlace place : m_places)
			clonePlaces.add(place.clone());
		cloneSpatialMemory.setPlaceList(clonePlaces);

		//cloneSpatialMemory.m_places = clonePlaces;
		//cloneSpatialMemory.m_clock = m_clock;
		
		return cloneSpatialMemory;
	}

	public void tick()
	{
		m_clock++;
	}

	public void trace(ITracer tracer)
	{
		if (tracer != null && !m_places.isEmpty())
		{
			Object localSpace = tracer.addEventElement("local_space");
			tracer.addSubelement(localSpace, "position_8", ErnestUtils.hexColor(getValue(DIRECTION_HERE)));
			tracer.addSubelement(localSpace, "position_7", ErnestUtils.hexColor(getValue(DIRECTION_BEHIND)));
			tracer.addSubelement(localSpace, "position_6", ErnestUtils.hexColor(getValue(DIRECTION_BEHIND_LEFT)));
			tracer.addSubelement(localSpace, "position_5", ErnestUtils.hexColor(getValue(DIRECTION_LEFT)));
			tracer.addSubelement(localSpace, "position_4", ErnestUtils.hexColor(getValue(DIRECTION_AHEAD_LEFT)));
			tracer.addSubelement(localSpace, "position_3", ErnestUtils.hexColor(getValue(DIRECTION_AHEAD)));
			tracer.addSubelement(localSpace, "position_2", ErnestUtils.hexColor(getValue(DIRECTION_AHEAD_RIGHT)));
			tracer.addSubelement(localSpace, "position_1", ErnestUtils.hexColor(getValue(DIRECTION_RIGHT)));
			tracer.addSubelement(localSpace, "position_0", ErnestUtils.hexColor(getValue(DIRECTION_BEHIND_RIGHT)));
		}
	}
	
	/**
	 * Add a new place to the local space memory.
	 * Replace the bundle if it already exists.
	 * @param bundle The bundle in this location.
	 * @param position The position of this place.
	 * @return The new or already existing location.
	 */
	public IPlace addPlace(IBundle bundle, Vector3f position)
	{
		IPlace place = new Place(bundle, position);	
		if (bundle != null)
			place.setValue(bundle.getValue());
		m_places.add(place);
		return place;
	}
	
	public IPlace addPlace(Vector3f position, int type)
	{
		IPlace place = new Place(position, type);	
		m_places.add(place);
		return place;
	}
	
	/**
	 * Update the local space memory according to the agent's moves.
	 * @param translation The translation vector in egocentric referential (provide the opposite vector from the agent's movement).
	 * @param rotation The rotation value (provide the opposite value from the agent's movement).
	 */
	public void transform(IAct act)
	{
		rotate(act.getRotation());
		translate(act.getTranslation());
	}
	
	/**
	 * Rotate all the places of the given angle.
	 * @param angle The angle (provide the opposite angle from the agent's movement).
	 */
	private void rotate(float angle)
	{
		for (IPlace l : m_places)
			l.rotate(angle);
	}

	/**
	 * Translate all the places of the given vector.
	 * Remove places that are outside the local space memory radius.
	 * @param translation The translation vector (provide the opposite vector from the agent's movement).
	 */
	private void translate(Vector3f translation)
	{
		for (IPlace p : m_places)
			p.translate(translation);
			
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace l = (IPlace)it.next();
			if (l.getPosition().length() > LOCAL_SPACE_MEMORY_RADIUS)
			//if (l.getPosition().x < - LOCAL_SPACE_MEMORY_RADIUS)
				it.remove();
		}		
	}
	
	public boolean runSimulation(IAct act, boolean doubt)
	{
		boolean consistent = false;
		
		IPlace simulationPlace = addPlace(new Vector3f(), Spas.PLACE_SIMULATION);
		
		consistent = simulate(simulationPlace, act, doubt);
		
		return consistent;
	}
	
	private boolean simulate(IPlace simulationPlace, IAct act, boolean doubt)
	{
		boolean consistent = false;
		ISchema s = act.getSchema();
		if (s.isPrimitive())
		{
			Vector3f startPosition = new Vector3f(act.getStartPosition());
			ErnestUtils.rotate(startPosition, simulationPlace.getOrientation());
			Vector3f position = new Vector3f(simulationPlace.getPosition());
			position.add(startPosition);
			IBundle bundle = getBundleSimulation(position);
			if (bundle == null)	
				consistent = doubt;
			else
			{
				if (doubt)
					consistent =  bundle.isConsistent(act);
				else 
					consistent = bundle.afford(act);
			}
			Vector3f position2 = new Vector3f(act.getTranslation());
			position2.scale(-1);
			simulationPlace.translate(position2);
			simulationPlace.rotate( - act.getRotation());
			//transform(act);

			// Mark the simulation of this act in spatial memory;
			if (consistent)
			{
				IPlace sim = addPlace(position, Spas.PLACE_SIMULATION);
				sim.setAct(act);
				sim.setOrientation(simulationPlace.getOrientation());
				//if (bundle == null) 
				//	sim.setValue(0x808080);
				//else
					sim.setValue(act.getColor());
			}
		}
		else 
		{
			consistent = simulate(simulationPlace, act.getSchema().getContextAct(), doubt);
			if (consistent)
				consistent = simulate(simulationPlace, act.getSchema().getIntentionAct(), doubt);
		}
		return consistent;
	}
	
	public boolean simulate(IAct act, boolean doubt)
	{
		boolean consistent = false;
		ISchema s = act.getSchema();
		if (s.isPrimitive())
		{			
			IBundle bundle = getBundleSimulation(act.getStartPosition());
			if (bundle == null)	
				consistent = doubt;
			else
			{
				if (doubt)
					consistent =  bundle.isConsistent(act);
				else 
					consistent = bundle.afford(act);
			}
			transform(act);
		}
		else 
		{
			consistent = simulate(act.getSchema().getContextAct(), doubt);
			if (consistent)
				consistent = simulate(act.getSchema().getIntentionAct(), doubt);
		}
		return consistent;
	}
	
	/**
	 * Get the phenomena value at a given position.
	 * (The last bundle found in the list of places that match this position)
	 * @param position The position of the location.
	 * @return The bundle.
	 */
	public int getValue(Vector3f position)
	{
		int value = Ernest.UNANIMATED_COLOR;
		for (IPlace p : m_places)
		{
			if (p.isInCell(position) && p.isPhenomenon())
				value = p.getValue();
		}	
		return value;
	}

	private IBundle getBundleSimulation(Vector3f position)
	{
		IBundle bundle = null;
		for (IPlace p : m_places)
		{
			if (p.isInCell(position) && p.isPhenomenon())
				bundle = p.getBundle();
		}	
		return bundle;
	}

	/**
	 * Get the last place found at a given position.
	 * @param position The position of the location.
	 * @return The place.
	 */
	public IPlace getPlace(Vector3f position)
	{
		IPlace place = null;
		for (IPlace p : m_places)
		{
			//if (p.isInCell(position) && p.evokePhenomenon(m_spas.getClock()))
			if (p.isInCell(position) && (p.isPhenomenon() || p.getType() == Spas.PLACE_EVOKE_PHENOMENON))// for copresence!!
				place = p;
		}
		return place;
	}

	/**
	 * Clear a position in the local space memory.
	 * @param position The position to clear.
	 */
	public void clearPlace(Vector3f position)
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace l = (IPlace)it.next();
			if (l.isInCell(position))
				it.remove();
		}		
	}
	
	/**
	 * Clear the places farther than DISTANCE_VISUAL_BACKGROUND.
	 */
	public void clearBackground()
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace l = (IPlace)it.next();
			if (l.getDistance() > DISTANCE_VISUAL_BACKGROUND - 1)
				it.remove();
		}
	}
	
	/**
	 * Clear the places in front (but not below Ernest) 
	 * (will be replaced by new seen places).
	 */
	public void clearFront()
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace l = (IPlace)it.next();
			if (l.getDirection() > - Math.PI/2 && l.getDirection() < Math.PI/2 &&
				l.getDistance() > 1)
				it.remove();
		}
	}
	
	/**
	 * Clear all the places older than PERSISTENCE_DURATION.
	 */
	public void clear()
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace p = (IPlace)it.next();
			if (p.getType() == Spas.PLACE_SEE || p.getType() == Spas.PLACE_TOUCH || p.getType() == Spas.PLACE_EVOKE_PHENOMENON)
			{
				//if (p.getUpdateCount() < m_spas.getClock() - PERSISTENCE_DURATION +1) // -1
				if (p.getUpdateCount() < m_clock - PERSISTENCE_DURATION +1) // -1
					it.remove();
			}
			else
			{
				//if (p.getUpdateCount() < m_spas.getClock() - PERSISTENCE_DURATION)
				if (p.getUpdateCount() < m_clock - PERSISTENCE_DURATION)
					it.remove();
			}
		}
		//m_places.clear();
	}
	
	/**
	 * @return The list of places in Local Spave Memory
	 */
	public ArrayList<IPlace> getPlaceList()
	{
		return m_places;
	}
	
	public void setPlaceList(ArrayList<IPlace> places) 
	{
		m_places = places;
	}
		
	private ArrayList<IPlace> getEvokeList()
	{
		ArrayList<IPlace> evokeList = new ArrayList<IPlace>();
		for (IPlace p : m_places)
			//if (p.evokePhenomenon(m_spas.getClock()))
			if (p.evokePhenomenon(m_clock))
				evokeList.add(p);

		
		return evokeList;
	}
	
	/**
	 * Construct copresence places in spatial memory
	 * @param observation The observation 
	 * @param spas A reference to the spatial system to add bundles
	 */
	public void copresence(IObservation observation, ISpas spas)
	{
		// Clear the places that are older than the persistence of spatial memory
		clear();
		
		// Get the list of interaction places (that can evoke phenomena).
		ArrayList<IPlace> interactionPlaces = new ArrayList<IPlace>();
		for (IPlace p : m_places)
			//if (p.evokePhenomenon(m_spas.getClock()))
			if (p.evokePhenomenon(m_clock))
				interactionPlaces.add(p);

		// Create new copresence bundles 
		
		for (IPlace interactionPlace : interactionPlaces)
		{
			if (interactionPlace.getAct().concernOnePlace())
			{
				for (IPlace secondPlace : interactionPlaces)
				{
					if (secondPlace.getAct().concernOnePlace())
					{
						if (!interactionPlace.getAct().equals(secondPlace.getAct()) && interactionPlace.isInCell(secondPlace.getPosition()))
						{
							spas.addBundle(interactionPlace.getAct(), secondPlace.getAct());
						}
					}
				}
			}
		}
		
		// Create copresence places that match enacted interactions
		for (IPlace interactionPlace : interactionPlaces)
		{
			//if (interactionPlace.getUpdateCount() == m_spas.getClock())
			if (interactionPlace.getUpdateCount() == m_clock)
			{
				IBundle bundle = spas.evokeBundle(interactionPlace.getAct());

				if (bundle != null)
				{
					boolean newPlace = true;
				
					// If the copresence place already exists then refresh it.
					for (IPlace copresencePlace :  m_places)
					{
						if (copresencePlace.getType() == Spas.PLACE_COPRESENCE && copresencePlace.isInCell(interactionPlace.getPosition())
								&& copresencePlace.getBundle().equals(bundle))
						{
							//copresencePlace.setUpdateCount(m_spas.getClock());
							copresencePlace.setUpdateCount(m_clock);
							newPlace = false;
						}
					}
					if (newPlace)
					{
						// If the copresence place does not exist then create it.
						
						IPlace k = addPlace(bundle,interactionPlace.getPosition()); 
						k.setFirstPosition(interactionPlace.getFirstPosition()); 
						k.setSecondPosition(interactionPlace.getSecondPosition());
						k.setOrientation(interactionPlace.getOrientation());
						//k.setUpdateCount(m_spas.getClock());
						k.setUpdateCount(m_clock);
						k.setType(Spas.PLACE_COPRESENCE);
						k.setValue(interactionPlace.getValue());
					}
				}
			}
		}
	}

	public void clearSimulation() 
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace p = (IPlace)it.next();
			if (p.getType() == Spas.PLACE_SIMULATION)
				it.remove();
		}
	}
	
//	/**
//	 * Maintain the list of active phenomena in spatial memory
//	 * @param observation The observation 
//	 * @param clock The time in the spatial memory
//	 */
//	public void phenomenon(IObservation observation, int clock)
//	{
//		// Clear the places that are older than the persistence of spatial memory
//		clear();
//		
//		// Get the list of interaction places (that can evoke phenomena).
//		ArrayList<IPlace> interactionPlaces = new ArrayList<IPlace>();
//		for (IPlace p : m_places)
//			if (p.evokePhenomenon(m_spas.getClock()))
//				interactionPlaces.add(p);
//
//		// Confirm or create phenomenon places in local space memory 
//		
//		for (IPlace interactionPlace : interactionPlaces)
//		{
//			//if (interactionPlace.getAct().getSchema().isPrimitive() || interactionPlace.getAct().getLabel().equals("(^f>t)") || interactionPlace.getAct().getLabel().equals("(vf>t)"))
//			if (interactionPlace.getAct().concernOnePlace())
//			{
//				boolean newPlace = true;
//				// Look for a corresponding existing phenomenon in local space memory.
//				for (IPlace phenomenonPlace :  m_places)
//				{
//					// If the interaction overlaps a phenomenon already constituted 
//					
//					// Then the phenomenon is refreshed 
//					// TODO Add the interaction to the bundle list.
//					
//					if (phenomenonPlace.isPhenomenon() && interactionPlace.from(phenomenonPlace))
//							//&& phenomenonPlace.getValue() == interactionPlace.getValue() // Generates some confusion with walls in Ernest11
//							//&& bundlePlace.getBundle().equals(interactionPlace.getBundle()) // This version works wih Ernest11
//							
//					{
//						phenomenonPlace.setPosition(interactionPlace.getPosition());
//						phenomenonPlace.setFirstPosition(interactionPlace.getFirstPosition());
//						phenomenonPlace.setSecondPosition(interactionPlace.getSecondPosition());
//						phenomenonPlace.setSpeed(interactionPlace.getSpeed());
//						phenomenonPlace.setSpan(interactionPlace.getSpan());
//						phenomenonPlace.setOrientation(interactionPlace.getOrientation());
//						phenomenonPlace.setUpdateCount(m_spas.getClock());
//						//phenomenonPlace.getBundle().addAct(interactionPlace.getAct());
//						IBundle aggregate = m_spas.aggregateBundle(phenomenonPlace.getBundle(), interactionPlace.getAct());
//						//phenomenonPlace.setBundle(aggregate);
//						newPlace = false;
//					}
//				}
//				if (newPlace)
//				{
//					// Create a new bundle 
//					// we can't assume it's the same as an existing bundle unless this very interaction already belongs to a bundle
//					IBundle bundle = m_spas.addBundle(interactionPlace.getAct());
//					
//					// Add a new phenomenon place
//					IPlace k = addPlace(interactionPlace.getBundle(),interactionPlace.getPosition()); 
//					k.setSpeed(interactionPlace.getSpeed());
//					k.setSpan(interactionPlace.getSpan());
//					k.setFirstPosition(interactionPlace.getFirstPosition()); 
//					k.setSecondPosition(interactionPlace.getSecondPosition());
//					k.setOrientation(interactionPlace.getOrientation());
//					k.setUpdateCount(m_spas.getClock());
//					k.setType(Spas.PLACE_PHENOMENON);
//					k.setValue(interactionPlace.getValue());
//					k.setBundle(bundle);
//				}
//			}
//		}
//		
//		// The most attractive place in local space memory gets the focus (abs value) 
//		
//		int maxAttractiveness = 0;
//		IPlace focusPlace = null;
//		boolean newFocus = false;
//		for (IPlace place : m_places)
//		{
//            if (place.isPhenomenon())
//			{
//				int attractiveness =  place.getAttractiveness(clock);
//				if (Math.abs(attractiveness) >= Math.abs(maxAttractiveness))
//				{
//					maxAttractiveness = attractiveness;
//					focusPlace = place;
//				}				
//			}
//		}
//		
//		// Test if the focus has changed
//		
//		if (focusPlace != null && focusPlace != m_focusPlace)
//		{
//			// Reset the previous stick
//			if (m_focusPlace != null) m_focusPlace.setStick(0);
//			// Set the new stick
//			focusPlace.setStick(20);
//			m_focusPlace = focusPlace;
//			//m_localSpaceMemory.setFocusPlace(focusPlace);
//			newFocus = true;
//			
//			//try { Thread.sleep(500);
//			//} catch (InterruptedException e) {e.printStackTrace();}
//		}
//		// The new observation.
//		
//		observation.setFocusPlace(m_focusPlace);
//		observation.setAttractiveness(maxAttractiveness);
//		observation.setNewFocus(newFocus);
//		
//		if (focusPlace == null || focusPlace.getBundle() == null)
//		{
//			//observation.setBundle(m_spas.addBundle(Ernest.STIMULATION_VISUAL_UNSEEN, Ernest.STIMULATION_TOUCH_EMPTY));
//			observation.setPosition(new Vector3f(1,0,0));
//			observation.setSpan(0);
//			observation.setSpeed(new Vector3f());
//			observation.setUpdateCount(-1);
//		}
//		else
//		{
//			observation.setBundle(focusPlace.getBundle());
//			observation.setPosition(focusPlace.getPosition());
//			observation.setSpan(focusPlace.getSpan());
//			observation.setSpeed(focusPlace.getSpeed());
//			observation.setType(focusPlace.getType());
//			observation.setUpdateCount(focusPlace.getUpdateCount());
//			
//			IAct act = focusPlace.getBundle().activateAffordance(focusPlace.getPosition());
//			observation.setAffordanceAct(act);
//		}		
//	}
	
//	/**
//	 * @return the list of phenomena in local space memory
//	 */
//	public ArrayList<IPlace> getPhenomena() 
//	{
//		ArrayList<IPlace> phenomena = new ArrayList<IPlace>();
//		
//		for (IPlace place : m_places)
//		{
//			//if (place.getPosition().length() < 1.9 && place.getPosition().length() > .1 && place.isPhenomenon())
//			if (place.isPhenomenon())
//				phenomena.add(place);
//		}
//		
//		//trace();
//		return phenomena;
//	}	

}
