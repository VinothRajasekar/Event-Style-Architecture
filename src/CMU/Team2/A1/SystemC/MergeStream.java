package CMU.Team2.A1.SystemC;

import CMU.Team2.A1.Common.FilterFramework;

// TODO: Auto-generated Javadoc
/**
 * The Class MergeStream.
 */
public class MergeStream extends FilterFramework {

	/** The Constant INPUT_PORT_A. */
	private static final int INPUT_PORT_A = 0;

	/** The Constant OUTPUT_PORT. */
	private static final int OUTPUT_PORT = 0;

	/** The Constant INPUT_PORT_B. */
	private static final int INPUT_PORT_B = 1;

	/**
	 * Instantiates a new merge stream.
	 */
	public MergeStream() {
		super(2, 1);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CMU.Team2.A1.Common.FilterFramework#run()
	 * This implemetation performs the merge and sort data.
	 */
	public void run() {

		CFrame frame1 = new CFrame();
		CFrame frame2 = new CFrame();

		try {
			frame1 = readFrame(INPUT_PORT_A);
		} catch (EndOfStreamException e) {
			copyInput(INPUT_PORT_B);
			ClosePorts();
			return;
		}

		try {
			frame2 = readFrame(INPUT_PORT_B);
		} catch (EndOfStreamException e) {
			copyInput(INPUT_PORT_A);
			ClosePorts();
			return;
		}

		while (true) {

			if (frame1.time < frame2.time) {
				frameOutput(OUTPUT_PORT, frame1);
				try {
					frame1 = readFrame(INPUT_PORT_A);
				} catch (EndOfStreamException e) {
					copyInput(INPUT_PORT_B);
					ClosePorts();
					return;
				}
			}

			else {
				frameOutput(OUTPUT_PORT, frame2);
				try {
					frame2 = readFrame(INPUT_PORT_B);
				} catch (EndOfStreamException e) {
					copyInput(INPUT_PORT_A);
					ClosePorts();
					return;
				}
			}
		}

	}

	/**
	 * Copy input.
	 * 
	 * @param inputPortIndex
	 *            the input port index
	 */
	private void copyInput(int inputPortIndex) {
		try {
			while (true)
				WriteFilterOutputPort(OUTPUT_PORT,
						ReadFilterInputPort(inputPortIndex));
		} catch (EndOfStreamException e) {

		}
	}

	/**
	 * Read frame.
	 * 
	 * @param inputPortIndex
	 *            the input port index
	 * @return the c frame
	 * @throws EndOfStreamException
	 *             the end of stream exception
	 */
	private CFrame readFrame(int inputPortIndex) throws EndOfStreamException {
		CFrame frame = new CFrame();

		int id = readNextInt(inputPortIndex);
		assert id == TIME;
		frame.time = readNextLong(inputPortIndex);

		id = readNextInt(inputPortIndex);
		assert id == VELOCITY;
		frame.velocity = readNextDouble(inputPortIndex);

		id = readNextInt(inputPortIndex);
		assert id == ALTITUDE;
		frame.altitude = readNextDouble(inputPortIndex);

		id = readNextInt(inputPortIndex);
		assert id == PRESSURE;
		frame.pressure = readNextDouble(inputPortIndex);

		id = readNextInt(inputPortIndex);
		assert id == TEMP;
		frame.temperature = readNextDouble(inputPortIndex);

		id = readNextInt(inputPortIndex);
		assert id == PITCH;
		frame.attitude = readNextDouble(inputPortIndex);

		return frame;
	}

	/**
	 * Frame output.
	 * 
	 * @param outputPortIndex
	 *            the output port index
	 * @param frame
	 *            the frame
	 */
	private void frameOutput(int outputPortIndex, CFrame frame) {
		writeInt(outputPortIndex, TIME);
		writeLong(outputPortIndex, frame.time);

		writeInt(outputPortIndex, VELOCITY);
		writeDouble(outputPortIndex, frame.velocity);

		writeInt(outputPortIndex, ALTITUDE);
		writeDouble(outputPortIndex, frame.altitude);

		writeInt(outputPortIndex, PRESSURE);
		writeDouble(outputPortIndex, frame.pressure);

		writeInt(outputPortIndex, TEMP);
		writeDouble(outputPortIndex, frame.temperature);

		writeInt(outputPortIndex, PITCH);
		writeDouble(outputPortIndex, frame.attitude);
	}

	/**
	 * The Class CFrame.
	 */
	private static class CFrame {

		/** The time. */
		public long time;

		/** The velocity. */
		public double velocity;

		/** The altitude. */
		public double altitude;

		/** The pressure. */
		public double pressure;

		/** The temperature. */
		public double temperature;

		/** The attitude. */
		public double attitude;
	}

}
