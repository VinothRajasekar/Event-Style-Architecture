package CMU.Team2.A1.SystemC;

import java.util.ArrayList;
import java.util.List;

import CMU.Team2.A1.Common.FilterFramework;
import CMU.Team2.A1.Common.SinkFilter;

// TODO: Auto-generated Javadoc
/**
 * The Class PlumberC.
 */
public class PlumberC {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String args[]) {
		SourceFilterC1 sourceFilter1 = new SourceFilterC1();
		SourceFilterC2 sourceFilter2 = new SourceFilterC2();
		
		/*sssss*/

		MergeStream mergeSortFilter = new MergeStream();
		PressureFilter psiFilter = new PressureFilter();
		MiddleFilterC temperature = new MiddleFilterC(MiddleFilterC.TEMPERATURE);
		MiddleFilterC altitude = new MiddleFilterC(MiddleFilterC.ALTITUDE);

		List<Integer> showOutput = new ArrayList<Integer>();
		showOutput.add(FilterFramework.TIME);
		showOutput.add(FilterFramework.VELOCITY);
		showOutput.add(FilterFramework.ALTITUDE);
		showOutput.add(FilterFramework.PRESSURE);
		showOutput.add(FilterFramework.TEMP);
		showOutput.add(FilterFramework.PITCH);

		List<Integer> listPressure = new ArrayList<Integer>();
		listPressure.add(FilterFramework.TIME);
		listPressure.add(FilterFramework.PRESSURE);

		List<Integer> listPsi = new ArrayList<Integer>();
		listPsi.add(FilterFramework.TIME);
		listPsi.add(FilterFramework.PRESSURE);
		listPsi.add(FilterFramework.PITCH);

		SinkFilter formatOutputC = new SinkFilter("OutputC.dat", showOutput);
		SinkFilter formatFilterPsi = new SinkFilter("Extrapolatedpsi.dat",
				listPressure);
		SinkFilter formatFilterComplexPsi = new SinkFilter(
				"Altittudepsi.dat", listPsi);

		formatFilterComplexPsi.Connect(0, psiFilter, 2);

		formatFilterPsi.Connect(0, psiFilter, 1);
		formatOutputC.Connect(0, psiFilter, 0);
		psiFilter.Connect(0, altitude, 0);
		altitude.Connect(0, temperature, 0);
		temperature.Connect(0, mergeSortFilter, 0);
		mergeSortFilter.Connect(1, sourceFilter2, 0);
		mergeSortFilter.Connect(0, sourceFilter1, 0);

		sourceFilter1.start();
		sourceFilter2.start();
		mergeSortFilter.start();
		temperature.start();
		altitude.start();
		psiFilter.start();
		formatOutputC.start();
		formatFilterPsi.start();
		formatFilterComplexPsi.start();
	}

}
