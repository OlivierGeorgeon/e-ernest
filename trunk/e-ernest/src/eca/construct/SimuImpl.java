package eca.construct;


import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import tracing.ITracer;
import utils.ErnestUtils;
import eca.Primitive;
import eca.PrimitiveImpl;
import eca.construct.egomem.Area;
import eca.construct.egomem.AreaImpl;
import eca.construct.egomem.Layout;
import eca.construct.egomem.LayoutImpl;
import eca.construct.egomem.Transformation;
import eca.construct.egomem.TransformationImpl;
import eca.construct.experiment.Experiment;
import eca.construct.experiment.ExperimentImpl;
import eca.ss.enaction.Act;
import eca.ss.enaction.ActImpl;
import eca.ss.enaction.Enaction;
import ernest.IEffect;

public class SimuImpl implements Simu {

	public static int SCALE = 3;
	
	/** Predefined areas */
	public static Area A = AreaImpl.createOrGet("A");
	public static Area B = AreaImpl.createOrGet("B");
	public static Area C = AreaImpl.createOrGet("C");
	public static Area O = AreaImpl.createOrGet("O");

	/** Predefined phenomena */
	public static Phenomenon EMPTY = PhenomenonImpl.createOrGet("_");
	
	/** Predefined transformations */
	public static Transformation UNKNOWN = TransformationImpl.createOrGet("?");
	public static Transformation IDENTITY = TransformationImpl.createOrGet("<");
	public static Transformation SHIFT_LEFT = TransformationImpl.createOrGet("^");
	public static Transformation SHIFT_RIGHT = TransformationImpl.createOrGet("v");
	
	private Layout layout  = LayoutImpl.createOrGet(EMPTY, EMPTY, EMPTY);
	
	/**
	 * Gives the area to which a point belongs.
	 * @param point The point
	 * @return The area of interest
	 */
	public static Area getArea(Point3f point) 
	{
		if (point.epsilonEquals(new Point3f(), .1f))
			return O;
		else if (ErnestUtils.polarAngle(point) > .1f)
			return A; 
		else if (ErnestUtils.polarAngle(point) >= -.1f)
			return B; 
		else
			return C; 
	}
	
	public static Point3f spasPoint(Area area){
		Point3f spasPoint = new Point3f(1, 0, 0);
		if (area.equals(A))
			spasPoint.set((float)Math.cos(Math.PI/4), (float)Math.sin(Math.PI/4), 0);
		else if (area.equals(C))
			spasPoint.set((float)Math.cos(Math.PI/4),-(float)Math.sin(Math.PI/4), 0);
		else if (area.equals(O))
			spasPoint.set(0,0, 0);
		spasPoint.scale(3);
		return spasPoint;
	}
	
	public static Transform3D spasTransform(Transformation transformation){
		Transform3D spasTransform = new Transform3D();
		spasTransform.setIdentity();
		if (!transformation.equals(UNKNOWN)){
			if (transformation.equals(SHIFT_LEFT))		
				spasTransform.rotZ(Math.PI/2);
			else if (transformation.equals(SHIFT_RIGHT))		
				spasTransform.rotZ(-Math.PI/2);
			else
				spasTransform.setTranslation(new Vector3f(0,0,0));
		}
		
		return spasTransform;
	}
	
	public static Act getAct(Action action, Observation observation){
		
		Experiment exp = ExperimentImpl.createOrGet(action, observation);
		Act act = exp.predictAct();
		
		if (act == null){
			Primitive interaction = action.getPrimitives().get(0);
			for (Primitive i : PrimitiveImpl.getINTERACTIONS()){
				if (i.getAction().equals(action) && i.getPhenomenonType().equals(observation.getPhenomenon()))
					interaction = i;
			}
			act = ActImpl.createOrGetPrimitiveAct(interaction, observation.getArea());
		}

		return act;
	}

	public void track(Enaction enaction){
		Area area = enaction.getEnactedPrimitiveAct().getArea();
		Phenomenon aspectA = EMPTY;
		Phenomenon aspectB = EMPTY;
		Phenomenon aspectC = EMPTY;
		
		//Transformation transformation = transformation(enaction.getEffect());
		Transformation transformation = enaction.getTransformation();
		//previousLayout = LayoutImpl.transform(layout, transformation);

		if (enaction.getEnactedPrimitiveAct() != null){
			if (area.equals(A)){
				aspectA = enaction.getEnactedPrimitiveAct().getPrimitive().getPhenomenonType();
			}
			else if (area.equals(B)){
				aspectB = enaction.getEnactedPrimitiveAct().getPrimitive().getPhenomenonType();
			}
			else if (area.equals(C)){
				aspectC = enaction.getEnactedPrimitiveAct().getPrimitive().getPhenomenonType();
			}
		}

		layout = LayoutImpl.createOrGet(aspectA, aspectB, aspectC);
		
		enaction.getEnactedPrimitiveAct().getPrimitive().getAction().setTransformation(enaction.getTransformation());
		//enaction.setTransformation(transformation);
	}
	
	public static Transformation transformation(IEffect effect){
//		Transformation transform = SimuImpl.UNKNOWN;
//
//		transform = SimuImpl.IDENTITY;
//		Transform3D t = effect.getTransformation();
//		float angle = ErnestUtils.angle(t);
//		if (Math.abs(angle) > .1){
//			if ( angle > 0)		
//				transform = SimuImpl.SHIFT_LEFT;
//			else 		
//				transform = SimuImpl.SHIFT_RIGHT;
//		}

		return transformation(effect.getTransformation());
		//return transform;
	}
	
	public static Transformation transformation(Transform3D t){
		Transformation transform = SimuImpl.UNKNOWN;

		transform = SimuImpl.IDENTITY;
		float angle = ErnestUtils.angle(t);
		if (Math.abs(angle) > .1){
			if ( angle > 0)		
				transform = SimuImpl.SHIFT_LEFT;
			else 		
				transform = SimuImpl.SHIFT_RIGHT;
		}

		return transform;
	}
	
	public Layout predict(Action action){
		return LayoutImpl.transform(layout, action.getTransformation()); 
		
//		Layout nextLayout = LayoutImpl.transform(layout, action.getTransformation()); 
//		Observation observation = nextLayout.observe();		
//		return observation;
	}

	public void trace(ITracer tracer)
	{
		if (tracer != null)
		{
			Object localSpace = tracer.addEventElement("local_space");
			if (layout.isEmpty())
				tracer.addSubelement(localSpace, "position_8", "9680FF");
			else
				tracer.addSubelement(localSpace, "position_8", "FFFFFF");
	
			tracer.addSubelement(localSpace, "position_7", "FFFFFF");
			tracer.addSubelement(localSpace, "position_6", "FFFFFF");
			
			if (!layout.isEmpty(A)){
				tracer.addSubelement(localSpace, "position_5", "9680FF");
				tracer.addSubelement(localSpace, "position_4", "9680FF");
			}
			else {
				tracer.addSubelement(localSpace, "position_5", "FFFFFF");
				tracer.addSubelement(localSpace, "position_4", "FFFFFF");
			}
			if (!layout.isEmpty(B)){
				tracer.addSubelement(localSpace, "position_3", "9680FF");
			}
			else {
				tracer.addSubelement(localSpace, "position_3", "FFFFFF");
			}
			if (!layout.isEmpty(C)){
				tracer.addSubelement(localSpace, "position_2", "9680FF");
				tracer.addSubelement(localSpace, "position_1", "9680FF");
			}
			else {
				tracer.addSubelement(localSpace, "position_2", "FFFFFF");
				tracer.addSubelement(localSpace, "position_1", "FFFFFF");
			}
			tracer.addSubelement(localSpace, "position_0", "FFFFFF");
			
			Object layoutElmt = tracer.addEventElement("layout");
			tracer.addSubelement(layoutElmt, "Aspect_A", layout.getPhenomenon(A).getLabel());
			tracer.addSubelement(layoutElmt, "Aspect_B", layout.getPhenomenon(B).getLabel());
			tracer.addSubelement(layoutElmt, "Aspect_C", layout.getPhenomenon(C).getLabel());
		}
	}
}
