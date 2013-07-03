package eca.construct;


import java.util.HashMap;
import java.util.Map;
import eca.construct.egomem.Area;

public class AppearanceImpl implements Appearance {
	
	private static Map<String , Appearance> OBSERVATIONS = new HashMap<String , Appearance>() ;
	private static int index = 0;
	private String label;
	private PhenomenonType phenomenonType;
	private Area area;
	
	/**
	 * @param phenomenonType The observation's aspect
	 * @param area The observation's area
	 * @return The new or old observation
	 */
	public static Appearance createOrGet(PhenomenonType phenomenonType, Area area){
		String label = createKey(phenomenonType, area);
		if (!OBSERVATIONS.containsKey(label))
			OBSERVATIONS.put(label, new AppearanceImpl(phenomenonType, area));			
		return OBSERVATIONS.get(label);
	}

//	public static Observation createOrGet(String label){
//		if (!OBSERVATIONS.containsKey(label))
//			OBSERVATIONS.put(label, new ObservationImpl(label));			
//		return OBSERVATIONS.get(label);
//	}

	private static String createKey(PhenomenonType phenomenonType, Area area) {
		String key = phenomenonType.getLabel() + area.getLabel();
		return key;
	}
	
//	public static Observation createNew(){
//		index++;
//		return createOrGet(index + "");
//		OBSERVATIONS.put(index + "", new ObservationImpl(index +""));			
//		return OBSERVATIONS.get(index + "");
//	}
	
//	private ObservationImpl(String label){
//		this.label = label;
//	}	
	
	private AppearanceImpl(PhenomenonType phenomenonType, Area area){
		this.label = phenomenonType.getLabel() + area.getLabel();
		this.phenomenonType = phenomenonType;
		this.area = area;
	}	
	
	public String getLabel() {
		return this.label;
		//return this.aspect.getLabel() + this.area.getLabel();
	}
	
	public PhenomenonType getPhenomenonType(){
		return this.phenomenonType;
	}
	
	public Area getArea(){
		return this.area;
	}
	
	/**
	 * Observations are equal if they have the same label. 
	 */
	public boolean equals(Object o)
	{
		boolean ret = false;
		
		if (o == this)
			ret = true;
		else if (o == null)
			ret = false;
		else if (!o.getClass().equals(this.getClass()))
			ret = false;
		else
		{
			Appearance other = (Appearance)o;
			ret = (other.getLabel().equals(this.getLabel()));
		}
		return ret;
	}
	
}
