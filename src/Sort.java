import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This is a sort class to make lists of points.
 * @author: Unima Diagnosticos.
 * @version: 2018/04/01
 * @since: 1.0.0
 */
public class Sort {


    /**
     * Method that sort a list
     * @param listOfPoints
     * @return List&lt;Point&gt;
     */
    public List<Point> sortIndividualList(List<Point> listOfPoints) {


        Collections.sort(listOfPoints, new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.compare(lhs.y, rhs.y);
            }
        });

        if (listOfPoints.get(0).x > listOfPoints.get(1).x) {

            double fx = listOfPoints.get(0).x;
            double fy = listOfPoints.get(0).y;

            listOfPoints.get(0).x = listOfPoints.get(1).x;
            listOfPoints.get(0).y = listOfPoints.get(1).y;

            listOfPoints.get(1).x = fx;
            listOfPoints.get(1).y = fy;
        }

        if (listOfPoints.get(2).x > listOfPoints.get(3).x) {

            double fx = listOfPoints.get(2).x;
            double fy = listOfPoints.get(2).y;

            listOfPoints.get(2).x = listOfPoints.get(3).x;
            listOfPoints.get(2).y = listOfPoints.get(3).y;

            listOfPoints.get(3).x = fx;
            listOfPoints.get(3).y = fy;
        }


        return listOfPoints;
    }

    /**
     * Method that sort a list of list
     * @param listsOfPoints
     * @return List&lt;List&lt;Point&gt;&gt;
     */
    public List<List<Point>> sortAllList(List<List<Point>> listsOfPoints) {

        for (int i = 0; i < listsOfPoints.size(); i++) {
            if (i % 2 == 0) {
                //System.out.println("entra en el for" + i);
                List<Point> PointListTemp = new ArrayList<Point>();
                if (listsOfPoints.get(i).get(0).x < listsOfPoints.get(i + 1).get(0).x) {
                    PointListTemp = listsOfPoints.get(i);
                    listsOfPoints.set(i, listsOfPoints.get(i + 1));
                    listsOfPoints.set(i + 1, PointListTemp);
                }
            }
            //System.out.println("entra " + listsOfPoints.size() + i);

        }

        return listsOfPoints;

    }


}
