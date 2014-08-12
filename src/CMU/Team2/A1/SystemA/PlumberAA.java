package CMU.Team2.A1.SystemA;

import java.util.ArrayList;
import java.util.List;

import CMU.Team2.A1.Common.FilterFramework;
import CMU.Team2.A1.Common.SinkFilter;

// TODO: Auto-generated Javadoc
/**
 * The Class PlumberAA.
 */
public class PlumberAA {

        /**
         * The main method.
         *
         * @param args the arguments
         */
        public static void main(String args[]) {
                SourceFilterA sourceFilter = new SourceFilterA();
                int dropMode = StandardDataFilter.DROPATTITUDE
                                | StandardDataFilter.DROPPRESSURE
                                | StandardDataFilter.DROPVELOCITY;
                StandardDataFilter dropUnusedDataFilter = new StandardDataFilter(dropMode);
                MiddleFilter altitudeConverter = new MiddleFilter(MiddleFilter.ALTITUDE);
                MiddleFilter temperatureConverter = new MiddleFilter(MiddleFilter.TEMPERATURE);
                List<Integer> idList = new ArrayList<Integer>();
                idList.add(FilterFramework.TIME);
                idList.add(FilterFramework.ALTITUDE);
                idList.add(FilterFramework.TEMP);
                SinkFilter formatFilter = new SinkFilter("OutputA.dat", idList);
            
                
                formatFilter.Connect(0, altitudeConverter, 0);
                altitudeConverter.Connect(0, temperatureConverter, 0);
                temperatureConverter.Connect(0, dropUnusedDataFilter, 0);
                dropUnusedDataFilter.Connect(0, sourceFilter, 0);
                
                sourceFilter.start();
                dropUnusedDataFilter.start();
                temperatureConverter.start();
                altitudeConverter.start();
                formatFilter.start();
        }
}