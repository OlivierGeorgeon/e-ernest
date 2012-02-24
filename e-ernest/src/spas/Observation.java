package spas;

import javax.vecmath.Vector3f;

import ernest.Ernest;

/**
 * An observation holds the significant consequences that the enacted interaction had on the spatial system.
 * It is the structure that supports the interaction between the spatial system (spas) 
 * and the intrinsic motivation system (imos).
 * @author Olivier
 */
public class Observation implements IObservation 
{

	private float m_span = 0;

	private Vector3f m_position = new Vector3f();
	
	private Vector3f m_speed = new Vector3f();
	
	/** The attractiveness of Ernest's interest. */
	private int m_attractiveness = 0;

	/** The kinematic stimulation. */
	private int m_kinematicValue;
	
	/** The gustatory stimulation. */
	private int m_gustatoryValue;
	
	/** The focus bundle. */
	private IBundle m_bundle; 
	
	/** The initial feedback obtained when the act starts. */
	private String m_instantaneousFeedback = "";
	
	/** The resulting stimuli of the enacted act. */
	private String m_stimuli;
	
	private int m_type;
	
	private int m_updateCount;
	
	private boolean m_newFocus = false;
	
	private Vector3f m_translation = new Vector3f();
	private float m_rotation;

	public void setAttractiveness(int attractiveness) 
	{
		m_attractiveness = attractiveness;
	}

	public int getAttractiveness() 
	{
		return m_attractiveness;
	}

	public void setKinematicValue(int value)
	{
		m_kinematicValue = value;
	}

	public int getKinematicValue()
	{
		return m_kinematicValue;
	}

	public void setGustatoryValue(int Value)
	{
		m_gustatoryValue = Value;
	}
	
	public int getGustatoryValue()
	{
		return m_gustatoryValue;
	}

	public void setPosition(Vector3f position) 
	{
		m_position.set(position);
	}

	public Vector3f getPosition() 
	{
		return m_position;
	}

	public float getDirection() 
	{
		return (float)Math.atan2((double)m_position.y, (double)m_position.x);
	}

	public float getDistance() 
	{
		return m_position.length();
	}

	public void setSpan(float span) 
	{
		m_span = span;
	}

	public float getSpan() 
	{
		return m_span;
	}

	public void setBundle(IBundle bundle) 
	{
		m_bundle = bundle;
	}

	public IBundle getBundle() 
	{
		return m_bundle;
	}

	public void setSpeed(Vector3f speed) 
	{
		m_speed = speed;
	}

	public Vector3f getSpeed()
	{
		return m_speed;
	}

	public void setInstantaneousFeedback(String instantaneousFeedback) 
	{
		m_instantaneousFeedback = instantaneousFeedback;
	}

	public void setStimuli(String stimuli) 
	{
		m_stimuli = stimuli;
	}

	public String getInstantaneousFeedback() 
	{
		return m_instantaneousFeedback;
	}

	public String getStimuli() 
	{
		return m_stimuli;
	}

	public void setType(int type) 
	{
		m_type = type;
	}

	public int getType() 
	{
		return m_type;
	}

	public void setUpdateCount(int updateCount)
	{
		m_updateCount = updateCount;
	}

	public int getUpdateCount() 
	{
		return m_updateCount;
	}

	public void setNewFocus(boolean newFocus) 
	{
		m_newFocus = newFocus;
	}

	public boolean getNewFocus() 
	{
		return m_newFocus;
	}

	public void setTranslation(Vector3f translation) 
	{
		m_translation.set(translation);
	}

	public Vector3f getTranslation() 
	{
		return m_translation;
	}

	public void setRotation(float rotation) 
	{
		m_rotation = rotation;
	}

	public float getRotation() 
	{
		return m_rotation;
	}
}
