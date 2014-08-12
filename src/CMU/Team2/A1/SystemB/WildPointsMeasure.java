package CMU.Team2.A1.SystemB;

import java.util.ArrayList;

import CMU.Team2.A1.Common.FilterFramework;

// TODO: Auto-generated Javadoc
/**
 * 
 * This filter removes pressure wildpoints that is less tha 50 psi or greater
 * than 80 psi. The filter has two ports the first one for corrected data and
 * the second one is for invalid data.
 */
public class WildPointsMeasure extends FilterFramework {

	/** The Constant PSI_PORT. */
	private static final int PSI_PORT = 1;

	/** The Constant INPUT_PORT. */
	private static final int INPUT_PORT = 0;

	/** The Constant FILTER_DATA_PORT. */
	private static final int FILTER_DATA_PORT = 0;

	/** The id list. */
	private ArrayList<Integer> idList = new ArrayList<Integer>();

	/** The measurements list. */
	private ArrayList<Long> measurementsList = new ArrayList<Long>();

	/** The changing measurements. */
	private ArrayList<Integer> changingMeasurements = new ArrayList<Integer>();

	/**
	 * Instantiates a new wild points measure.
	 */
	public WildPointsMeasure() {
		super(1, 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CMU.Team2.A1.Common.FilterFramework#run()
	 */
	@Override
	public void run() {
		double lastValidPressure = 0;
		boolean waitingForNextValidPressure = false;
		try {
			long lastTime = 0;
			while (true) {
				int id = readNextInt(INPUT_PORT);
				switch (id) {
				case TIME:
					lastTime = readNextLong(INPUT_PORT);
					if (waitingForNextValidPressure) {
						idList.add(id);
						measurementsList.add(lastTime);
					} else {
						writeInt(FILTER_DATA_PORT, id);
						writeLong(FILTER_DATA_PORT, lastTime);
					}
					break;
				case PRESSURE:
					double pressure = readNextDouble(INPUT_PORT);
					if (pressure >= 50 && pressure <= 80) {
						if (waitingForNextValidPressure) {
							waitingForNextValidPressure = false;
							double correctedPressure;
							if (lastValidPressure > 0)
								correctedPressure = (lastValidPressure + pressure) / 2;
							else
								correctedPressure = pressure;
							psiMeasurements(correctedPressure);
							sendData();
						}
						writeInt(FILTER_DATA_PORT, id);
						writeDouble(FILTER_DATA_PORT, pressure);
						lastValidPressure = pressure;
					} else {
						writeInt(PSI_PORT, TIME);
						writeLong(PSI_PORT, lastTime);
						writeInt(PSI_PORT, id);
						writeDouble(PSI_PORT, pressure);
						waitingForNextValidPressure = true;
						idList.add(PRESSURE_ADJUST);
						measurementsList.add(Double
								.doubleToRawLongBits(pressure));
						changingMeasurements.add(measurementsList.size() - 1);
					}
					break;
				default:
					if (waitingForNextValidPressure) {
						idList.add(id);
						measurementsList.add(readNextLong(INPUT_PORT));
					} else {
						writeInt(FILTER_DATA_PORT, id);
						for (int i = 0; i < 8; i++)
							WriteFilterOutputPort(FILTER_DATA_PORT,
									ReadFilterInputPort(INPUT_PORT));
					}
				}
			}
		} catch (EndOfStreamException e) {
			if (waitingForNextValidPressure) {
				psiMeasurements(lastValidPressure);
				sendData();
			}
			ClosePorts();
		}
	}

	/**
	 * Psi measurements.
	 * 
	 * @param correctedPressure
	 *            the corrected pressure
	 */
	private void psiMeasurements(double correctedPressure) {
		for (int i : changingMeasurements)
			measurementsList.set(i,
					Double.doubleToRawLongBits(correctedPressure));
		changingMeasurements.clear();
	}

	/**
	 * Send data.
	 */
	private void sendData() {
		for (int i = 0; i < idList.size(); i++) {
			writeInt(FILTER_DATA_PORT, idList.get(i));
			writeLong(FILTER_DATA_PORT, measurementsList.get(i));
		}
		idList.clear();
		measurementsList.clear();
	}
}