package CMU.Team2.A1.SystemC;

import CMU.Team2.A1.Common.FilterFramework;

// TODO: Auto-generated Javadoc
/**
 * The Class MiddleFilter implementation to convert data.
 */
public class MiddleFilterC extends FilterFramework {

	/** The Constant TEMPERATURE. */
	public final static int TEMPERATURE = 1;

	/** The Constant ALTITUDE. */
	public final static int ALTITUDE = 2;

	/** The mode. */
	protected int mode = MiddleFilterC.TEMPERATURE;

	/**
	 * Instantiates a new middle filter.
	 */
	public MiddleFilterC() {
		super(1, 1);
	}

	/**
	 * Instantiates a new middle filter.
	 * 
	 * @param mode
	 *            the mode
	 */
	public MiddleFilterC(int mode) {
		super(1, 1);
		this.mode = mode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CMU.Team2.A1.Common.FilterFramework#run()
	 */
	@Override
	public void run() {
		int measurementLength = 8;

		int idLength = 4;

		byte databyte = 0;
		@SuppressWarnings("unused")
		int bytesread = 0;

		long measurement;

		int id;
		int i;

		while (true) {
			try {
				/***************************************************************************
				 * The firstdata coming to this filters is to be an ID i.e;
				 * Idlength long So we first decommutate the ID bytes.
				 ****************************************************************************/

				id = 0;

				for (i = 0; i < idLength; i++) {
					databyte = ReadFilterInputPort(0);

					id = id | (databyte & 0xFF);

					if (i != idLength - 1)

					{
						id = id << 8;

					}

					bytesread++;

				}

				/****************************************************************************
				 * Here all the data is read as stream of bytes which permits us
				 * to do the bit manipulation that converts streams into
				 * datawords.
				 *****************************************************************************/

				measurement = 0;

				for (i = 0; i < measurementLength; i++) {
					databyte = ReadFilterInputPort(0);
					measurement = measurement | (databyte & 0xFF);
					// append
					// the
					// byte
					// on to
					// measurement...

					if (i != measurementLength - 1)

					{
						measurement = measurement << 8;
						// the next byte we
						// append to the
						// measurement
					}

					bytesread++;

				}

				switch (mode) {
				case TEMPERATURE:
					if (id == FilterFramework.TEMP) {
						double fahrenheitTemperature = Double
								.longBitsToDouble(measurement);
						double celsiusTemperature = (fahrenheitTemperature - 32) * 5 / 9;
						measurement = Double
								.doubleToLongBits(celsiusTemperature);
					}
					break;
				case ALTITUDE:
					if (id == FilterFramework.ALTITUDE) {
						double feet = Double.longBitsToDouble(measurement);
						double meter = 0.3048 * feet;
						measurement = Double.doubleToLongBits(meter);
					}
					break;
				}
				writeInt(0, id);
				writeLong(0, measurement);
			}

			catch (EndOfStreamException e) {
				ClosePorts();
				break;

			}
		}
	}
}