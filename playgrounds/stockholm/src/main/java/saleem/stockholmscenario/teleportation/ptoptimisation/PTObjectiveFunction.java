package saleem.stockholmscenario.teleportation.ptoptimisation;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.roadpricing.CalcPaidToll;

import com.google.inject.Inject;

import floetteroed.opdyts.ObjectiveFunction;
import floetteroed.opdyts.SimulatorState;
import floetteroed.opdyts.example.roadpricing.RoadpricingState;
import floetteroed.utilities.math.Vector;

/**
 * Returns the negative sum of the scores of the selected plans of all agents,
 * excluding toll.
 * 
 * @author Gunnar Flötteröd
 *
 */
public class PTObjectiveFunction implements ObjectiveFunction {

	@Override
	public double value(SimulatorState state) {//Simple summation of selected plan scores
		double result = 0;
		// TODO Auto-generated method stub
		final PTState ptstate = (PTState) state;
		for (Id<Person> personId : ptstate.getPersonIdView()) {
			final Plan selectedPlan = ptstate
				.getSelectedPlan(personId);
			result += selectedPlan.getScore();
		}
		result /= ptstate.getPersonIdView().size();
		return result;	
		}
}
