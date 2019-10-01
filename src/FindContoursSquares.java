import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

public class FindContoursSquares {
    public List<List<Point>> FindTreeContours(Mat imgMat) {

        Mat imgMatCopy = new Mat();
        imgMat.copyTo(imgMatCopy);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(imgMatCopy, contours, hierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
        MatOfPoint2f approx = new MatOfPoint2f();

        List<List<Point>> listsOfPoints = new ArrayList<List<Point>>();
        try {
            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {

                contours.get(contourIdx).convertTo(mMOP2f1, CvType.CV_32FC2);
                Imgproc.approxPolyDP(mMOP2f1, approx, Imgproc.arcLength(mMOP2f1, true) * 0.02, true);
                MatOfPoint mMOP = new MatOfPoint();
                approx.convertTo(mMOP, CvType.CV_32S);

                if (approx.rows() == 4 && Imgproc.contourArea(approx) > 20000) {
                    // Imgproc.drawContours(imgMat,contours,contourIdx, new Scalar(255,0,0),10);
                    List<Point> list = new ArrayList<Point>();
                    Converters.Mat_to_vector_Point(mMOP, list);
                    listsOfPoints.add(list);
                }

            }
            System.out.println("this is the size of  listsOfPoints BG " + listsOfPoints.size());

        } catch (Exception e) {

        }
        return listsOfPoints;
    }

    public List<MatOfPoint> FindExternalContours(Mat imgMat) {

        Mat imgMatCopy = new Mat();
        imgMat.copyTo(imgMatCopy);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(imgMatCopy, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
        MatOfPoint2f approx = new MatOfPoint2f();

        List<MatOfPoint> approxList = new ArrayList<MatOfPoint>();

        int total = 0;

        try {
            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {

                contours.get(contourIdx).convertTo(mMOP2f1, CvType.CV_32FC2);
                Imgproc.approxPolyDP(mMOP2f1, approx, Imgproc.arcLength(mMOP2f1, true) * 0.02, true);
                MatOfPoint mMOP = new MatOfPoint();
                approx.convertTo(mMOP, CvType.CV_32S);

                if (approx.rows() == 4 && Imgproc.contourArea(approx) > 20000) {
                    // Imgproc.drawContours(imgMat,contours,contourIdx, new
                    // Scalar(255,0,0),10);
                    approxList.add(mMOP);
                    total++;
                }

            }

            System.out.println("this is the size of  approxList BGExternal " + approxList.size());

        } catch (Exception e) {

        }
        return approxList;

    }
}
