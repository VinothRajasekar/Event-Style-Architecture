package CMU.Team2.A1.Common;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * Implementation of sink filter
 */
public class SinkFilter extends FilterFramework {
	private static final int INPUT_PORT = 0;
	private static final Map<Integer, Integer> FIELD_WIDTH;
	private final int lastFrameID;
	private final int displayLastItem;
	private static final int[] ATTRIBUTES_LIST = new int[] { TIME, TEMP,
			ALTITUDE, PRESSURE, VELOCITY, PITCH };
	private final String outputFilename;
	private final List<Integer> idsToDisplay;

	static {
		HashMap<Integer, Integer> fieldWidth = new HashMap<Integer, Integer>();
		fieldWidth.put(TIME, 20);
		fieldWidth.put(TEMP, 20);
		fieldWidth.put(ALTITUDE, 17);
		fieldWidth.put(PRESSURE, 19);
		fieldWidth.put(VELOCITY, 23);
		fieldWidth.put(PITCH, 0);
		FIELD_WIDTH = Collections.unmodifiableMap(fieldWidth);
	}

	public SinkFilter(String outputFilename, List<Integer> displayIDs) {
		super(1, 0);
		this.outputFilename = outputFilename;
		this.idsToDisplay = displayIDs;
		lastFrameID = displayIDs.get(displayIDs.size() - 1);
		displayLastItem = findLastIDToDisplay();
	}

	private int findLastIDToDisplay() {
		int lastIDToDisplay = -1;
		for (int i = ATTRIBUTES_LIST.length - 1; i >= 0
				&& lastIDToDisplay == -1; i--)
			if (idsToDisplay.contains(ATTRIBUTES_LIST[i]))
				lastIDToDisplay = ATTRIBUTES_LIST[i];
		return lastIDToDisplay;
	}

	@Override
	public void run() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:dd:HH:mm:ss");
		Calendar date = Calendar.getInstance();

		DecimalFormat temperatureFormat = new DecimalFormat("000.00000");
		temperatureFormat.setDecimalSeparatorAlwaysShown(true);

		DecimalFormat altitudeFormat = new DecimalFormat("000000.00000");
		altitudeFormat.setDecimalSeparatorAlwaysShown(true);

		DecimalFormat pressureFormat = new DecimalFormat("00.00000");
		pressureFormat.setDecimalSeparatorAlwaysShown(true);

		DecimalFormat velocityFormat = new DecimalFormat("000.00000");
		velocityFormat.setDecimalSeparatorAlwaysShown(true);

		DecimalFormat attitudeFormat = new DecimalFormat("00.00000");
		velocityFormat.setDecimalSeparatorAlwaysShown(true);

		FileWriter writer = null;
		try {
			writer = new FileWriter(outputFilename);
			for (int idToPrint : ATTRIBUTES_LIST) {
				if (idsToDisplay.contains(idToPrint)) {
					StringBuilder output = new StringBuilder();
					switch (idToPrint) {
					case TIME:
						output.append("Time:");
						if (idToPrint != displayLastItem)
							addSpaces(idToPrint, output);
						writer.write(output.toString());
						break;
					case TEMP:
						output.append("Temperature (C):");
						if (idToPrint != displayLastItem)
							addSpaces(idToPrint, output);
						writer.write(output.toString());
						break;
					case ALTITUDE:
						output.append("Altitude (m):");
						if (idToPrint != displayLastItem)
							addSpaces(idToPrint, output);
						writer.write(output.toString());
						break;
					case PRESSURE:
						output.append("Pressure (psi):");
						if (idToPrint != displayLastItem)
							addSpaces(idToPrint, output);
						writer.write(output.toString());
						break;
					case VELOCITY:
						output.append("Velocity (kn):");
						if (idToPrint != displayLastItem)
							addSpaces(idToPrint, output);
						writer.write(output.toString());
						break;
					case PITCH:
						output.append("Attitude (degrees):");
						if (idToPrint != displayLastItem)
							addSpaces(idToPrint, output);
						writer.write(output.toString());
						break;
					}
				}
			}
			writer.write("\n");
			while (true) {
				Frame frame = readFrame();
				for (int idToPrint : ATTRIBUTES_LIST) {
					if (idsToDisplay.contains(idToPrint)) {
						StringBuilder output = new StringBuilder();
						switch (idToPrint) {
						case TIME:
							date.setTimeInMillis(frame.time);
							output.append(dateFormat.format(date.getTime()));
							if (idToPrint != displayLastItem)
								addSpaces(idToPrint, output);
							writer.write(output.toString());
							break;
						case TEMP:
							output.append(temperatureFormat
									.format(frame.temperature));
							if (frame.correctedTemperature)
								output.append("*");
							if (idToPrint != displayLastItem)
								addSpaces(idToPrint, output);
							writer.write(output.toString());
							break;
						case ALTITUDE:
							output.append(altitudeFormat.format(frame.altitude));
							if (frame.correctedAltitude)
								output.append("*");
							if (idToPrint != displayLastItem)
								addSpaces(idToPrint, output);
							writer.write(output.toString());
							break;
						case PRESSURE:
							output.append(pressureFormat.format(frame.pressure));
							if (frame.correctedPressure)
								output.append("*");
							if (idToPrint != displayLastItem)
								addSpaces(idToPrint, output);
							writer.write(output.toString());
							break;
						case VELOCITY:
							output.append(velocityFormat.format(frame.velocity));
							if (frame.correctedVelocity)
								output.append("*");
							if (idToPrint != displayLastItem)
								addSpaces(idToPrint, output);
							writer.write(output.toString());
							break;
						case PITCH:
							output.append(attitudeFormat.format(frame.attitude));
							if (frame.correctedAttitude)
								output.append("*");
							if (idToPrint != displayLastItem)
								addSpaces(idToPrint, output);
							writer.write(output.toString());
							break;
						}
					}
				}
				writer.write("\n");
			}
		} catch (EndOfStreamException e) {
		} catch (IOException e) {
			System.err.println("IO Error in FormatFilter: " + e.getMessage());
		}
		try {
			if (writer != null)
				writer.close();
		} catch (IOException e) {
			System.err.println("IO Error in FormatFilter: " + e.getMessage());
		}
		ClosePorts();
	}

	private void addSpaces(int idToPrint, StringBuilder output) {
		for (int i = output.length(); i < FIELD_WIDTH.get(idToPrint); i++)
			output.append(' ');
	}

	private Frame readFrame() throws EndOfStreamException {
		Frame frame = new Frame();
		int id = readNextInt(INPUT_PORT);
		while (id != TIME) {
			readNextDouble(INPUT_PORT);
			id = readNextInt(INPUT_PORT);
		}
		frame.time = readNextLong(INPUT_PORT);
		do {
			id = readNextInt(INPUT_PORT);
			switch (id) {
			case VELOCITY_ADJUST:
				frame.correctedVelocity = true;
			case VELOCITY:
				frame.velocity = readNextDouble(INPUT_PORT);
				break;
			case ALITITUDE_ADJUST:
				frame.correctedAltitude = true;
			case ALTITUDE:
				frame.altitude = readNextDouble(INPUT_PORT);
				break;
			case PRESSURE_ADJUST:
				frame.correctedPressure = true;
			case PRESSURE:
				frame.pressure = readNextDouble(INPUT_PORT);
				break;
			case TEMP_ADJUST:
				frame.correctedTemperature = true;
			case TEMP:
				frame.temperature = readNextDouble(INPUT_PORT);
				break;
			case PITCH_ADJUST:
				frame.correctedAttitude = true;
			case PITCH:
				frame.attitude = readNextDouble(INPUT_PORT);
				break;
			}
		} while (Math.abs(id) != lastFrameID);
		return frame;
	}

	private static class Frame {
		public long time;
		public double velocity;
		public double altitude;
		public double pressure;
		public double temperature;
		public double attitude;
		public boolean correctedVelocity = false;
		public boolean correctedAltitude = false;
		public boolean correctedPressure = false;
		public boolean correctedTemperature = false;
		public boolean correctedAttitude = false;
	}
}    
