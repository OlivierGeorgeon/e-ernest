package ernest;

import imos.IAct;

import org.w3c.dom.Element;

import spas.IObservation;
import spas.IStimulation;
import spas.LocalSpaceMemory;
import spas.Stimulation;

/**
 * Implement Ernest 10.0's sensorimotor system.
 * Ernest 10.0 has a visual resolution of 2x12 pixels and a kinesthetic resolution of 3x3 pixels.
 * @author ogeorgeon
 */
public class Visual100SensorymotorSystem  extends BinarySensorymotorSystem
{
	/** The current observation generated by the spatial system */
	private IObservation m_observation = null;
	
	private String m_visualStimuli = "";
	private String m_stimuli = "";
	private int m_satisfaction = 0;

	public IAct enactedAct(IAct act, int[][] stimuli) 
	{
		IStimulation kinematicStimulation;
		
		// Vision =====
		
		IStimulation[] visualStimulations = new Stimulation[Ernest.RESOLUTION_RETINA];
		for (int i = 0; i < Ernest.RESOLUTION_RETINA; i++)
			visualStimulations[i] = m_spas.addStimulation(Ernest.MODALITY_VISUAL, stimuli[i][1] * 65536 + stimuli[i][2] * 256 + stimuli[i][3]);

		if (m_tracer != null) 
		{
			Object retinaElmt = m_tracer.addEventElement("retina");
			for (int i = Ernest.RESOLUTION_RETINA - 1; i >= 0 ; i--)
				m_tracer.addSubelement(retinaElmt, "pixel_0_" + i, visualStimulations[i].getHexColor());
		}
		
		// Touch =====
		
		IStimulation [] tactileStimulations = new IStimulation[9];
		
//		for (int j = 0; j < 3; j++)
//			for (int i = 0; i < 3; i++)
//				tactileSimulations[i][j] = m_spas.addStimulation(Ernest.MODALITY_TACTILE, stimuli[i][9 + j]);
		for (int i = 0; i < 9; i++)
			tactileStimulations[i] = m_spas.addStimulation(Ernest.MODALITY_TACTILE, stimuli[i][9]);

//		tactileSimulations[0] = m_spas.addStimulation(Ernest.MODALITY_TACTILE, stimuli[0][9], LocalSpaceMemory.DIRECTION_BEHIND_RIGHT);
		
		if (m_tracer != null)
		{
			Object s = m_tracer.addEventElement("tactile");
			m_tracer.addSubelement(s, "here", tactileStimulations[8].getHexColor());
			m_tracer.addSubelement(s, "rear", tactileStimulations[7].getHexColor());
			m_tracer.addSubelement(s, "touch_6", tactileStimulations[6].getHexColor());
			m_tracer.addSubelement(s, "touch_5", tactileStimulations[5].getHexColor());
			m_tracer.addSubelement(s, "touch_4", tactileStimulations[4].getHexColor());
			m_tracer.addSubelement(s, "touch_3", tactileStimulations[3].getHexColor());
			m_tracer.addSubelement(s, "touch_2", tactileStimulations[2].getHexColor());
			m_tracer.addSubelement(s, "touch_1", tactileStimulations[1].getHexColor());
			m_tracer.addSubelement(s, "touch_0", tactileStimulations[0].getHexColor());
		}
			
		// Kinematic ====
		
		kinematicStimulation = m_spas.addStimulation(Ernest.STIMULATION_KINEMATIC, stimuli[1][8]);

		// Taste =====
		
		IStimulation gustatoryStimulation = m_spas.addStimulation(Ernest.STIMULATION_GUSTATORY, stimuli[0][8]); 

		// Process the spatial implications of the enacted interaction ====
		
		IAct enactedAct = null;		
		IObservation newObservation = m_spas.step(act, visualStimulations, tactileStimulations, kinematicStimulation, gustatoryStimulation);
		
		// Process the sequential implications of the enacted interaction ===
		
		// If the intended act was null (during the first cycle), then the enacted act is null.
		if (act != null)
		{
			setDynamicFeature(act, m_observation, newObservation);
			enactedAct = m_imos.addInteraction(act.getSchema().getLabel(), m_stimuli, m_satisfaction);
		}
		

		if (act != null && m_tracer != null) 
		{
			m_tracer.addEventElement("primitive_enacted_schema", act.getSchema().getLabel());
			Object e = m_tracer.addEventElement("current_observation");
			m_tracer.addSubelement(e, "direction", newObservation.getPlace().getDirection() + "");
			m_tracer.addSubelement(e, "distance", newObservation.getPlace().getDistance() + "");
			m_tracer.addSubelement(e, "span", newObservation.getPlace().getSpan() + "");
			m_tracer.addSubelement(e, "attractiveness", newObservation.getAttractiveness() + "");
			m_tracer.addSubelement(e, "stimuli", m_stimuli);
			m_tracer.addSubelement(e, "dynamic_feature", m_visualStimuli);
			m_tracer.addSubelement(e, "satisfaction", m_satisfaction + "");
			m_tracer.addSubelement(e, "kinematic", newObservation.getKinematicStimulation().getHexColor());
			m_tracer.addSubelement(e, "gustatory", newObservation.getGustatoryStimulation().getHexColor());
		}
		m_observation = newObservation;

		return enactedAct;
	}
	
	/**
	 * Generate the dynamic stimuli from the impact in the local space memory.
	 * The stimuli come from: 
	 * - The kinematic feature.
	 * - The variation in attractiveness and in direction of the object of interest. 
	 * @param act The enacted act.
	 */
	private void setDynamicFeature(IAct act, IObservation previousObservation, IObservation newObservation)
	{
		int   newAttractiveness = newObservation.getAttractiveness();
		float newDirection = newObservation.getPlace().getDirection();
		int   previousAttractiveness = previousObservation.getAttractiveness();
		float previousDirection = previousObservation.getPlace().getDirection();
		
		String dynamicFeature = "";
		
		float minFovea =  - (float)Math.PI / 4 + 0.01f;
		float maxFovea =    (float)Math.PI / 4 - 0.01f;
		
		int satisfaction = 0;

		if (newAttractiveness >= 0)
		{
			// Positive attractiveness
			{
				// Attractiveness
				if (previousAttractiveness > newAttractiveness)
					// Farther
					dynamicFeature = "-";		
				else if (previousAttractiveness < newAttractiveness)
					// Closer
					dynamicFeature = "+";
				else if (Math.abs(previousDirection) < Math.abs(newDirection))
					// More outward (or same direction, therefore another salience)
					dynamicFeature = "-";
				else if (Math.abs(previousDirection) > Math.abs(newDirection))
					// More inward
					dynamicFeature = "+";
		
				if (dynamicFeature.equals("-"))
					satisfaction = -100;
				if (dynamicFeature.equals("+"))
					satisfaction = 20;
	
				// Direction
				
				if (!dynamicFeature.equals(""))
				{
					if (newDirection <= minFovea)
						dynamicFeature = "|" + dynamicFeature;
					else if (newDirection >= maxFovea )
						dynamicFeature = dynamicFeature + "|";
				}		
			}
		}
		else
		{
			// Negative attractiveness (repulsion)
			
			// Variation in attractiveness
			if (previousAttractiveness >= 0)
				// A wall appeared with a part of it in front of Ernest
				dynamicFeature = "*";		
			else if (Math.abs(previousDirection) < Math.abs(newDirection))
				// The wall went more outward (Ernest closer to the edge)
				dynamicFeature = "_";
			else if (Math.abs(previousDirection) > Math.abs(newDirection))
				// The wall went more inward (Ernest farther to the edge)
				dynamicFeature = "*";
	
			if (dynamicFeature.equals("*"))
				satisfaction = -100;
			if (dynamicFeature.equals("_"))
				satisfaction = 20;
			
			// Direction feature
			
			if (!dynamicFeature.equals(""))
			{
				if (newDirection < -0.1f ) 
					dynamicFeature = "|" + dynamicFeature;
				else if (newDirection > 0.1f )
					dynamicFeature = dynamicFeature + "|";
			}		
		}
		
		// Gustatory
		
		if (newObservation.getGustatoryStimulation().getValue() != Ernest.STIMULATION_GUSTATORY_NOTHING)
		{
			dynamicFeature = "e";
			satisfaction = 100;
		}
		
		m_visualStimuli = dynamicFeature;
		
		// Kinematic
		
		boolean status = true;
		if (newObservation.getKinematicStimulation().getValue() == Ernest.STIMULATION_KINEMATIC_BUMP) 
			status = false;
		
		dynamicFeature = (status ? " " : "w") + dynamicFeature;
		if (act != null)
		{
			if (act.getSchema().getLabel().equals(">"))
				satisfaction = satisfaction + (status ? 20 : -100);
			else
				satisfaction = satisfaction + (status ? -10 : -20);
		}
				
		m_stimuli = dynamicFeature;
		m_satisfaction = satisfaction;
	}
}