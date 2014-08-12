package CMU.Team2.A1.SystemB;
import CMU.Team2.A1.Common.FilterFramework;
import CMU.Team2.A1.Common.SinkFilter;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PlumberBB.
 */
public class PlumberBB {

        /**
         * The main method.
         *
         * @param args the arguments
         */
        public static void main(String args[]) {
                SourceFilter sourceFilter = new SourceFilter();
                int dropMode = StandardDataFilter.DROPATTITUDE
                                | StandardDataFilter.DROPVELOCITY;
                WildPointsMeasure psiFilter = new WildPointsMeasure();
                StandardDataFilter dataFilter = new StandardDataFilter(dropMode);
                MiddleFilter altitude = new MiddleFilter(MiddleFilter.ALTITUDE);
                MiddleFilter temperature = new MiddleFilter(MiddleFilter.TEMPERATURE);
               
                
                
                List<Integer> storeOutput = new ArrayList<Integer>();
                storeOutput.add(FilterFramework.TIME);
                storeOutput.add(FilterFramework.ALTITUDE);
                storeOutput.add(FilterFramework.PRESSURE);
                storeOutput.add(FilterFramework.TEMP);
                
                List<Integer> storePsi = new ArrayList<Integer>();
                storePsi.add(FilterFramework.TIME);
                storePsi.add(FilterFramework.PRESSURE);
                
                SinkFilter formatOutputB = new SinkFilter("OutputB.dat", storeOutput);
                SinkFilter formatPsi = new SinkFilter("WildPoints.dat", storePsi);
                
                formatPsi.Connect(0, psiFilter, 1);
                formatOutputB.Connect(0, psiFilter, 0);
                psiFilter.Connect(0, altitude, 0);
                altitude.Connect(0, temperature, 0);
                temperature.Connect(0, dataFilter, 0);
                dataFilter.Connect(0, sourceFilter, 0);
                
                sourceFilter.start();
                dataFilter.start();
                temperature.start();
                altitude.start();
                psiFilter.start();
                formatOutputB.start();
                formatPsi.start();
        }

}