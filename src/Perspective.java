import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * This class takes the perspective of the images and transforms it in the perspective that the application needs.
 * @author Unima Diagnosticos
 * @version 2018/04/01
 * @since 1.0.0
 */

public class Perspective {
    private static final int offset = 5;

    /**
     * Applies perspective transformation to a cropped Mat by external contour
     * @param imgMat Mat: Cropped image by external contour
     * @param listOfPoints List&lt;Point&gt;: Points representing a contour
     * @return Mat: Transformed image by perspective change
     */
    public Mat applyPerspective(Mat imgMat, List<Point> listOfPoints) {

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        int width = imgMat.cols();
        int height = imgMat.rows();


        src_mat.put(0, 0, listOfPoints.get(0).x - offset, listOfPoints.get(0).y - offset,
                listOfPoints.get(1).x + offset, listOfPoints.get(1).y - offset,
                listOfPoints.get(2).x - offset, listOfPoints.get(2).y + offset,
                listOfPoints.get(3).x + offset, listOfPoints.get(3).y + offset);

        dst_mat.put(0, 0, 0, 0, width, 0, 0, height, width, height);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Mat perspectiveImg = new Mat();
        Imgproc.warpPerspective(imgMat, perspectiveImg, perspectiveTransform, new Size(width, height));


        return perspectiveImg;


    }

    /**
     * Transforms the perspective to an XMarker
     * @param imgMat Mat: Cropped XMarker
     * @param listOfPoints List&lt;Point&gt;: Points representing a contour
     * @return Mat: Transformed XMarker by perspective change
     */
    public Mat applyPerspectiveIndividual(Mat imgMat, List<Point> listOfPoints) {

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        int width = imgMat.cols();
        int height = imgMat.rows();


        src_mat.put(0, 0, listOfPoints.get(0).x - offset, listOfPoints.get(0).y - offset,
                listOfPoints.get(1).x + offset, listOfPoints.get(1).y - offset,
                listOfPoints.get(2).x - offset, listOfPoints.get(2).y + offset,
                listOfPoints.get(3).x + offset, listOfPoints.get(3).y + offset);


        dst_mat.put(0, 0, 0, 0, width, 0, 0, height, width, height);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Mat perspectiveImg = new Mat();
        Imgproc.warpPerspective(imgMat, perspectiveImg, perspectiveTransform, new Size(width, height));


        return perspectiveImg;


    }

    /**
     * Transforms perspective of the most external contour of the FIND test card
     * @param imgMat Mat: Original image
     * @param listOfPoints List&lt;Point&gt;: Points representing a contour
     * @return Mat: Transformed image by perspective change
     */
    public Mat applyPerspectiveWhiteSquare(Mat imgMat, List<Point> listOfPoints) {

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        int width = imgMat.cols();
        int height = imgMat.rows();


        src_mat.put(0, 0, listOfPoints.get(0).x + offset, listOfPoints.get(0).y + offset,
                listOfPoints.get(1).x - offset, listOfPoints.get(1).y + offset,
                listOfPoints.get(2).x + offset, listOfPoints.get(2).y - offset,
                listOfPoints.get(3).x - offset, listOfPoints.get(3).y - offset);

        dst_mat.put(0, 0, 0, 0, width, 0, 0, height, width, height);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Mat perspectiveImg = new Mat();
        Imgproc.warpPerspective(imgMat, perspectiveImg, perspectiveTransform, new Size(width, height));


        return perspectiveImg;


    }


}
