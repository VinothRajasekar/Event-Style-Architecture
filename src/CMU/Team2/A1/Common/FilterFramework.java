package CMU.Team2.A1.Common;

/******************************************************************************************************************
 * File:FilterFramework.java
 * Course: 17655
 * Project: Assignment 1
 * Copyright: Copyright (c) 2003 Carnegie Mellon University
 * Versions:
 *	1.0 November 2008 - Initial rewrite of original assignment 1 (ajl).
 *
 * Description:
 *
 * This superclass defines a skeletal filter framework that defines a filter in terms of the input and output
 * ports. All filters must be defined in terms of this framework - that is, filters must extend this class
 * in order to be considered valid system filters. Filters as standalone threads until the inputport no longer
 * has any data - at which point the filter finishes up any work it has to do and then terminates.
 *
 * Parameters:
 *
 * InputReadPort:	This is the filter's input port. Essentially this port is connected to another filter's piped
 *					output steam. All filters connect to other filters by connecting their input ports to other
 *					filter's output ports. This is handled by the Connect() method.
 *
 * OutputWritePort:	This the filter's output port. Essentially the filter's job is to read data from the input port,
 *					perform some operation on the data, then write the transformed data on the output port.
 *
 * FilterFramework:  This is a reference to the filter that is connected to the instance filter's input port. This
 *					reference is to determine when the upstream filter has stopped sending data along the pipe.
 *
 * Internal Methods:
 *
 *	public void Connect( FilterFramework Filter )
 *	public byte ReadFilterInputPort()
 *	public void WriteFilterOutputPort(byte datum)
 *	public boolean EndOfInputStream()
 *
 ******************************************************************************************************************/

import java.io.*;

public class FilterFramework extends Thread {
	// Define filter input and output ports

	// data related
	public final static int TIME = 0; // long
	public final static int VELOCITY = 1;// double
	public final static int ALTITUDE = 2;// double
	public final static int PRESSURE = 3;// double
	public final static int TEMP = 4;// double
	public static final int VELOCITY_ADJUST = -1;
	public static final int TEMP_ADJUST = -4;

	public static final int PRESSURE_ADJUST = -3;

	public static final int ALITITUDE_ADJUST = -2;

	public static final int PITCH_ADJUST = -5;
	/**
	 * PITCH is altitude direction with reference to nose position w.r.t ground
	 */
	public final static int PITCH = 5;// double

	private final PipedInputStream[] InputReadPort;
	private final PipedOutputStream[] OutputWritePort;

	// The following reference to a filter is used because java pipes are able
	// to reliably
	// detect broken pipes on the input port of the filter. This variable will
	// point to
	// the previous filter in the network and when it dies, we know that it has
	// closed its
	// output pipe and will send no more data.

	private FilterFramework[] InputFilter;

	/***************************************************************************
	 * InnerClass:: EndOfStreamExeception Purpose: This
	 * 
	 * 
	 * 
	 * Arguments: none
	 * 
	 * Returns: none
	 * 
	 * Exceptions: none
	 * 
	 ****************************************************************************/

	@SuppressWarnings("serial")
	public class EndOfStreamException extends Exception {

		EndOfStreamException() {
			super();
		}

		EndOfStreamException(String s) {
			super(s);
		}

	} // class

	/**
	 * Creates a FilterFramework with the specified number of input ports and
	 * output ports. It is possible to specify zero input ports for source
	 * filters and zero output ports for sink filters.
	 */
	protected FilterFramework(int numberOfInputPorts, int numberOfOutputPorts) {
		InputReadPort = new PipedInputStream[numberOfInputPorts];
		for (int i = 0; i < numberOfInputPorts; i++) {
			InputReadPort[i] = new PipedInputStream();
		}
		OutputWritePort = new PipedOutputStream[numberOfOutputPorts];
		for (int i = 0; i < numberOfOutputPorts; i++) {
			OutputWritePort[i] = new PipedOutputStream();
		}
		InputFilter = new FilterFramework[numberOfInputPorts];
	}

	/***************************************************************************
	 * CONCRETE METHOD:: Connect Purpose: This method connects filters to each
	 * other. All connections are through the inputport of each filter. That is
	 * each filter's inputport is connected to another filter's output port
	 * through this method.
	 * 
	 * Arguments: FilterFramework - this is the filter that this filter will
	 * connect to.
	 * 
	 * Returns: void
	 * 
	 * Exceptions: IOException
	 * 
	 ****************************************************************************/

	public void Connect(int numofinputPort, FilterFramework Filter,
			int numofoutputPort) {
		try {
			// Connect this filter's input to the upstream pipe's output stream

			InputReadPort[numofinputPort]
					.connect(Filter.OutputWritePort[numofoutputPort]);
			InputFilter[numofinputPort] = Filter;

		} // try

		catch (Exception Error) {
			System.out.println("\n" + this.getName()
					+ " FilterFramework error connecting::" + Error);

		} // catch

	} // Connect

	/***************************************************************************
	 * CONCRETE METHOD:: ReadFilterInputPort Purpose: This method reads data
	 * from the input port one byte at a time.
	 * 
	 * Arguments: void
	 * 
	 * Returns: byte of data read from the input port of the filter.
	 * 
	 * Exceptions: IOExecption, EndOfStreamException (rethrown)
	 * 
	 ****************************************************************************/

	protected byte ReadFilterInputPort(int numofinputPort)
			throws EndOfStreamException {
		byte datum = 0;

		/***********************************************************************
		 * Since delays are possible on upstream filters, we first wait until
		 * there is data available on the input port. We check,... if no data is
		 * available on the input port we wait for a quarter of a second and
		 * check again. Note there is no timeout enforced here at all and if
		 * upstream filters are deadlocked, then this can result in infinite
		 * waits in this loop. It is necessary to check to see if we are at the
		 * end of stream in the wait loop because it is possible that the
		 * upstream filter completes while we are waiting. If this happens and
		 * we do not check for the end of stream, then we could wait forever on
		 * an upstream pipe that is long gone. Unfortunately Java pipes do not
		 * throw exceptions when the input pipe is broken.
		 ***********************************************************************/

		try {
			while (InputReadPort[numofinputPort].available() == 0) {
				if (EndOfInputStream(numofinputPort)) {
					throw new EndOfStreamException(
							"End of input stream reached");

				} // if

				sleep(500);

			} // while

		} // try

		catch (EndOfStreamException Error) {
			throw Error;

		} // catch

		catch (Exception Error) {
			System.out.println("\n" + this.getName()
					+ " Error in read port wait loop::" + Error);

		} // catch

		/***********************************************************************
		 * If at least one byte of data is available on the input pipe we can
		 * read it. We read and write one byte to and from ports.
		 ***********************************************************************/

		try {
			datum = (byte) InputReadPort[numofinputPort].read();
			return datum;

		} // try

		catch (Exception Error) {
			System.out.println("\n" + this.getName() + " Pipe read error::"
					+ Error);
			return datum;

		} // catch

	} // ReadFilterPort

	/***************************************************************************
	 * CONCRETE METHOD:: WriteFilterOutputPort Purpose: This method writes data
	 * to the output port one byte at a time.
	 * 
	 * Arguments: byte datum - This is the byte that will be written on the
	 * output port.of the filter.
	 * 
	 * Returns: void
	 * 
	 * Exceptions: IOException
	 * 
	 ****************************************************************************/

	protected void WriteFilterOutputPort(int numofoutputPort, byte datum) {
		try {
			OutputWritePort[numofoutputPort].write((int) datum);
			OutputWritePort[numofoutputPort].flush();

		} // try

		catch (Exception Error) {
			System.out.println("\n" + this.getName() + " Pipe write error::"
					+ Error);

		} // catch

		return;

	} // WriteFilterPort

	/***************************************************************************
	 * CONCRETE METHOD:: EndOfInputStream Purpose: This method is used within
	 * this framework which is why it is private It returns a true when there is
	 * no more data to read on the input port of the instance filter. What it
	 * really does is to check if the upstream filter is still alive. This is
	 * done because Java does not reliably handle broken input pipes and will
	 * often continue to read (junk) from a broken input pipe.
	 * 
	 * Arguments: void
	 * 
	 * Returns: A value of true if the previous filter has stopped sending data,
	 * false if it is still alive and sending data.
	 * 
	 * Exceptions: none
	 * 
	 ****************************************************************************/

	private boolean EndOfInputStream(int numofinputPort) {
		if (InputFilter[numofinputPort].isAlive()) {
			return false;

		} else {

			return true;

		} // if

	} // EndOfInputStream

	/***************************************************************************
	 * CONCRETE METHOD:: ClosePorts Purpose: This method is used to close the
	 * input and output ports of the filter. It is important that filters close
	 * their ports before the filter thread exits.
	 * 
	 * Arguments: void
	 * 
	 * Returns: void
	 * 
	 * Exceptions: IOExecption
	 * 
	 ****************************************************************************/

	protected void ClosePorts() {
		try {
			for (int i = 0; InputReadPort != null && i < InputReadPort.length; i++)
				InputReadPort[i].close();
			for (int i = 0; OutputWritePort != null
					&& i < OutputWritePort.length; i++)
				OutputWritePort[i].close();

		} catch (Exception Error) {
			System.out.println("\n" + this.getName() + " ClosePorts error::"
					+ Error);

		} // catch

	} // ClosePorts

	/***************************************************************************
	 * CONCRETE METHOD:: run Purpose: This is actually an abstract method
	 * defined by Thread. It is called when the thread is started by calling the
	 * Thread.start() method. In this case, the run() method should be
	 * overridden by the filter programmer using this framework superclass
	 * 
	 * Arguments: void
	 * 
	 * Returns: void
	 * 
	 * Exceptions: IOExecption
	 * 
	 ****************************************************************************/

	public void run() {
		// The run method should be overridden by the subordinate class. Please
		// see the example applications provided for more details.

	} // run

	
	/**
	 * Reads BYTES input port and converts them into a single int.
	 */
	protected int readNextInt(int numofinputPort) throws EndOfStreamException {
		int value = 0;
		byte b;
		for (int i = 0; i < 4; i++) {
			b = ReadFilterInputPort(numofinputPort);
			value = value | (b & 0xFF);
			if (i < 3)
				value = value << 8;
		}
		return value;
	}

	protected void writeInt(int numofoutputPort, int value) {
		byte b;
		int mask = 0xFF000000;
		for (int shift = 24; shift >= 0; shift -= 8) {
			b = (byte) ((value & mask) >> shift);
			WriteFilterOutputPort(numofoutputPort, b);
			mask = mask >> 8;
		}
	}
	
	

	/**
	 * Reads bytes from input port and converts them into a single long.
	 */
	protected long readNextLong(int numofinputPort) throws EndOfStreamException {
		long value = 0;
		byte b;
		for (int i = 0; i < 8; i++) {
			b = ReadFilterInputPort(numofinputPort);
			value = value | (b & 0xFF);
			if (i < 7)
				value = value << 8;
		}
		return value;
	}
	
	/**
	 * Separates value into eight bytes and writes them to output
	 */
	protected void writeLong(int numofoutputPort, long value) {
		byte b;
		long mask = 0xFF00000000000000L;
		for (int shift = 56; shift >= 0; shift -= 8) {
			b = (byte) ((value & mask) >> shift);
			WriteFilterOutputPort(numofoutputPort, b);
			mask = mask >> 8;
		}
	}


	/**
	 * Reads bytes from input port and converts them into a single double.
	 */
	protected double readNextDouble(int numofinputPort)
			throws EndOfStreamException {
		return Double.longBitsToDouble(readNextLong(numofinputPort));
	}

	protected void writeDouble(int numofoutputPort, double value) {
		writeLong(numofoutputPort, Double.doubleToRawLongBits(value));
	}




} // FilterFramework class
