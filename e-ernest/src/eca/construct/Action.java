package eca.construct;

import java.util.List;
import eca.Primitive;
import eca.construct.egomem.Displacement;
import eca.ss.Appearance;
import eca.ss.enaction.Act;

/**
 * An Action that can be chosen by the agent.
 * An action is the atomic grain of decision of the agent
 * 
 * An action records interactions based on the fact that they  may end up being actually enacted 
 * when the agent chooses this action.
 * 
 * An action maintains the list of interactions that are considered a success of the action
 * and a list of interactions that are considered a failure of the action.
 * 
 * @author Olivier
 */
public interface Action
{
	/**
	 * @return This action's label
	 */
	public String getLabel();
	
	/**
	 * @param primitive The primitive interaction to add to this action.
	 */
	public void addPrimitive(Primitive primitive);

	/**
	 * @return The list of primitive interactions that perform this action.
	 */
	public List<Primitive> getSuccessInteractions();
	
	public boolean contains(Primitive primitive);
	
	/**
	 * Predicts the act that will likely result from performing this action on this appearance based on previous experiments
	 * if no previous experiment then return the act made of the first primitive of this action in area O.
	 * @param appearance The appearance on which the action is performed.
	 * @return The Act that will likely result from performing this action on this appearance.
	 */
	public Act predictAct(Appearance appearance);
	
	/**
	 * @param appearance The appearance on which the action is performed.
	 * @return The Displacement that will likely result from performing this action on this appearance.
	 */
	public Displacement predictDisplacement(Appearance appearance);

	/**
	 * @param preAppearance The Appearance 
	 * @return The predicted post-appearance.
	 */
	public Appearance predictPostAppearance(Appearance preAppearance);

}
