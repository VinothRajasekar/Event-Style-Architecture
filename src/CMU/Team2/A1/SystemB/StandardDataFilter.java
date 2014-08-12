package CMU.Team2.A1.SystemB;

import CMU.Team2.A1.Common.FilterFramework;

// TODO: Auto-generated Javadoc
/**
 * The Class StandardDataFilter.
 */
public class StandardDataFilter extends FilterFramework {

	/** The mode. */
	int mode = 0;

	/**
	 * Instantiates a new standard data filter.
	 */
	public StandardDataFilter() {
		super(1, 1);
	}

	/**
	 * Instantiates a new standard data filter.
	 * 
	 * @param mode
	 *            the mode
	 */
	public StandardDataFilter(int mode) {
		super(1, 1);
		this.mode = mode;

	}

	/** The Constant DROPTIME. */
	public static final int DROPTIME = 0x01;

	/** The Constant DROPVELOCITY. */
	public static final int DROPVELOCITY = 0x02;

	/** The Constant DROPALTITUDE. */
	public static final int DROPALTITUDE = 0x04;

	/** The Constant DROPPRESSURE. */
	public static final int DROPPRESSURE = 0x08;

	/** The Constant DROPTEMPERATURE. */
	public static final int DROPTEMPERATURE = 0x10;

	/** The Constant DROPATTITUDE. */
	public static final int DROPATTITUDE = 0x20;

	/**
	 * Send filter data.
	 * 
	 * @param mode
	 *            the mode
	 * @param filter
	 *            the filter
	 * @param id
	 *            the id
	 * @param measurement
	 *            the measurement
	 */
	private void sendFilterData(int mode, int filter, int id, long measurement) {
		if ((mode & filter) == 0) {
			writeInt(0, id);
			writeLong(0, measurement);
		}
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
				 * // We know that the first data coming to this filter is going
				 * to be an ID so decomutate the id bytes. And all data is read
				 * as stream of bytes and using bitwise manipulation it is
				 * necessary to convert the stream into data bytes.
				 ****************************************************************************/

				id = 0;

				for (i = 0; i < idLength; i++) {
					databyte = ReadFilterInputPort(0);

					id = id | (databyte & 0xFF);

					if (i != idLength - 1) {
						id = id << 8;

					}

					bytesread++;

				}

				measurement = 0;

				for (i = 0; i < measurementLength; i++) {
					databyte = ReadFilterInputPort(0);
					measurement = measurement | (databyte & 0xFF);

					if (i != measurementLength - 1)

					{
						measurement = measurement << 8;

					}

					bytesread++;

				}

				switch (id) {
				case FilterFramework.TIME:
					sendFilterData(mode, DROPTIME, id, measurement);
					break;

				case FilterFramework.ALTITUDE:
					sendFilterData(mode, DROPALTITUDE, id, measurement);
					break;

				case FilterFramework.VELOCITY:
					sendFilterData(mode, DROPVELOCITY, id, measurement);
					break;

				case FilterFramework.PRESSURE:
					sendFilterData(mode, DROPPRESSURE, id, measurement);
					break;

				case FilterFramework.TEMP:
					sendFilterData(mode, DROPTEMPERATURE, id, measurement);
					break;

				case FilterFramework.PITCH:
					sendFilterData(mode, DROPATTITUDE, id, measurement);
					break;
				default:
					break;
				}
			}

			catch (EndOfStreamException e) {
				ClosePorts();
				break;

			}
		}
	}
}
     
 
