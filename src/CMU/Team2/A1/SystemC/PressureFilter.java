package CMU.Team2.A1.SystemC;

import java.util.ArrayList;

import CMU.Team2.A1.Common.FilterFramework;

// TODO: Auto-generated Javadoc
/**
 * The Class PressureFilter implementation
 * Filters wildpoint measurement lessthan 45
 *  or greater than 90 psi.
 */
public class PressureFilter extends FilterFramework
{
        
        /** The Constant INPUT_PORT. */
        private static final int INPUT_PORT = 0;
        
        /** The Constant VALID_OUTPUT_PORT. */
        private static final int VALID_OUTPUT_PORT = 0;
        
        /** The Constant PSI_OUTPUT_PORT. */
        private static final int PSI_OUTPUT_PORT = 1;
        
        /** The Constant INVALID_PSI_PORT. */
        private static final int INVALID_PSI_PORT = 2;
        
        /**
         * Instantiates a new pressure filter.
         */
        public PressureFilter()
        {
                super(1, 3);
        }
        
        /** The frames list. */
        private ArrayList<CFrame> framesList = new ArrayList<CFrame>();
        
        /* (non-Javadoc)
         * @see CMU.Team2.A1.Common.FilterFramework#run()
         */
        @Override
        public void run()
        {
                
                boolean nextValidFrame = false;
                CFrame validFrame = null;
                try
                {
                        while (true)
                        {
                                CFrame currentFrame = readFrame();
                                if (currentFrame.pressure > 65 && currentFrame.attitude > 10)
                                {
                                        writeFrame(INVALID_PSI_PORT, currentFrame);
                                        if (currentFrame.pressure > 90)
                                                writeFrame(PSI_OUTPUT_PORT, currentFrame);
                                        currentFrame.correctedFrame = true;
                                        framesList.add(currentFrame);
                                        nextValidFrame = true;
                                }
                                else if (currentFrame.pressure < 45 || currentFrame.pressure > 90)
                                {
                                        writeInt(PSI_OUTPUT_PORT, TIME);
                                        writeLong(PSI_OUTPUT_PORT, currentFrame.time);
                                        writeInt(PSI_OUTPUT_PORT, PRESSURE);
                                        writeDouble(PSI_OUTPUT_PORT, currentFrame.pressure);
                                        currentFrame.correctedPressure = true;
                                        framesList.add(currentFrame);
                                        nextValidFrame = true;
                                }
                                else
                                {
                                        if (nextValidFrame)
                                        {
                                                nextValidFrame = false;
                                                CFrame correctedFrame;
                                                if (validFrame == null)
                                                        correctedFrame = currentFrame;
                                                else
                                                        correctedFrame = calculateFrame(validFrame, currentFrame);
                                                storedFrames(correctedFrame);
                                                for (CFrame frameToSend : framesList)
                                                        writeFrame(VALID_OUTPUT_PORT, frameToSend);
                                                framesList.clear();
                                        }
                                        writeFrame(VALID_OUTPUT_PORT, currentFrame);
                                        validFrame = currentFrame;
                                }
                        }
                }
                catch (EndOfStreamException e)
                {
                        if (nextValidFrame)
                        {
                                storedFrames(validFrame);
                                for (CFrame frameToSend : framesList)
                                        writeFrame(VALID_OUTPUT_PORT, frameToSend);
                        }
                        ClosePorts();
                }
        }
        
        /**
         * Stored frames.
         *
         * @param correctedFrame the corrected frame
         */
        private void storedFrames(CFrame correctedFrame)
        {
                for (CFrame currentFrame : framesList)
                {
                        if (currentFrame.correctedFrame)
                        {
                                currentFrame.velocity = correctedFrame.velocity;
                                currentFrame.altitude = correctedFrame.altitude;
                                currentFrame.pressure = correctedFrame.pressure;
                                currentFrame.temperature = correctedFrame.temperature;
                                currentFrame.attitude = correctedFrame.attitude;
                        }
                        else
                                currentFrame.pressure = correctedFrame.pressure;
                }
        }
        
        /**
         * Calculate frame.
         *
         * @param a the a
         * @param b the b
         * @return the c frame
         */
        private CFrame calculateFrame(CFrame a, CFrame b)
        {
                CFrame result = new CFrame();
                result.velocity = (a.velocity + b.velocity) / 2;
                result.altitude = (a.altitude + b.altitude) / 2;
                result.pressure = (a.pressure + b.pressure) / 2;
                result.temperature = (a.temperature + b.temperature) / 2;
                result.attitude = (a.attitude + b.attitude) / 2;
                return result;
        }
        

        
        /**
         * Read frame.
         *
         * @return the c frame
         * @throws EndOfStreamException the end of stream exception
         */
        private CFrame readFrame() throws EndOfStreamException
        {
                CFrame frame = new CFrame();
                
                int id = readNextInt(INPUT_PORT);
                assert id == TIME;
                frame.time = readNextLong(INPUT_PORT);
                
                id = readNextInt(INPUT_PORT);
                assert id == VELOCITY;
                frame.velocity = readNextDouble(INPUT_PORT);
                
                id = readNextInt(INPUT_PORT);
                assert id == ALTITUDE;
                frame.altitude = readNextDouble(INPUT_PORT);
                
                id = readNextInt(INPUT_PORT);
                assert id == PRESSURE;
                frame.pressure = readNextDouble(INPUT_PORT);
                
                id = readNextInt(INPUT_PORT);
                assert id == TEMP;
                frame.temperature = readNextDouble(INPUT_PORT);
                
                id = readNextInt(INPUT_PORT);
                assert id == PITCH;
                frame.attitude = readNextDouble(INPUT_PORT);
                
                return frame;
        }
        
        /**
         * Write frame.
         *
         * @param outputPortIndex the output port index
         * @param frame the frame
         */
        private void writeFrame(int outputPortIndex, CFrame frame)
        {
                writeInt(outputPortIndex, TIME);
                writeLong(outputPortIndex, frame.time);
                
                if (frame.correctedFrame)
                        writeInt(outputPortIndex, VELOCITY_ADJUST);
                else
                        writeInt(outputPortIndex, VELOCITY);
                writeDouble(outputPortIndex, frame.velocity);
                
                if (frame.correctedFrame)
                        writeInt(outputPortIndex, ALITITUDE_ADJUST);
                else
                        writeInt(outputPortIndex, ALTITUDE);
                writeDouble(outputPortIndex, frame.altitude);
                
                if (frame.correctedFrame || frame.correctedPressure)
                        writeInt(outputPortIndex, PRESSURE_ADJUST);
                else
                        writeInt(outputPortIndex, PRESSURE);
                writeDouble(outputPortIndex, frame.pressure);
                
                if (frame.correctedFrame)
                        writeInt(outputPortIndex, TEMP_ADJUST);
                else
                        writeInt(outputPortIndex, TEMP);
                writeDouble(outputPortIndex, frame.temperature);
                
                if (frame.correctedFrame)
                        writeInt(outputPortIndex, PITCH_ADJUST);
                else
                        writeInt(outputPortIndex, PITCH);
                writeDouble(outputPortIndex, frame.attitude);
        }
        
        /**
         * The Class CFrame.
         */
        private static class CFrame
        {
                
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
                
                /** The corrected pressure. */
                public boolean correctedPressure = false;
                
                /** The corrected frame. */
                public boolean correctedFrame = false;
        }
}