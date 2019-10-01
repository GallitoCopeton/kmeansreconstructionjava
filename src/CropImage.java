import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

public class CropImage {
    public static Mat findBiggestSquare(Mat image) {
        Mat biggestSquare = null;
        FindContoursSquares findContoursSquares = new FindContoursSquares();
        image = preProcessing.ResizeImage(image, 728);

        Mat thres = new Mat();
        thres = preProcessing.binarizeImage(image);

        /*Sort points of contours found*/
        List<List<Point>> listsOfPoints = new ArrayList<List<Point>>();
        listsOfPoints = findContoursSquares.FindTreeContours(thres);
        List<Point> SquarePointsOrdered = new ArrayList<Point>();
        Sort sortList = new Sort();

        /*Perspective of image*/
        Perspective perspectiveSquare = new Perspective();
        Mat imgPerspective = new Mat();


        for (int i = 0; i < listsOfPoints.size(); i++) {

            /*Find and sort contours*/
            SquarePointsOrdered = sortList.sortIndividualList(listsOfPoints.get(i));
            imgPerspective = perspectiveSquare.applyPerspectiveWhiteSquare(thres, SquarePointsOrdered);
            List<MatOfPoint> approxList = new ArrayList<MatOfPoint>();
            approxList = findContoursSquares.FindExternalContours(imgPerspective);

            if (approxList.size() == 2) {
                Mat imgMatPerspective = new Mat();
                imgMatPerspective = perspectiveSquare.applyPerspectiveWhiteSquare(image, SquarePointsOrdered);
                System.out.println("The square was found");

                List<Point> bigSquare = new ArrayList<Point>();
                List<Point> smallSquare = new ArrayList<Point>();

                MatOfPoint Area1 = new MatOfPoint();
                Area1 = approxList.get(0);
                MatOfPoint Area2 = new MatOfPoint();
                Area2 = approxList.get(1);

                double area1 = Imgproc.contourArea(Area1);
                double area2 = Imgproc.contourArea(Area2);

                /* Label square for QR and XMarkers, select the bigger*/
                if (area1 > area2) {
                    Converters.Mat_to_vector_Point(approxList.get(0), bigSquare);
                    Converters.Mat_to_vector_Point(approxList.get(1), smallSquare);
                } else {
                    Converters.Mat_to_vector_Point(approxList.get(1), bigSquare);
                    Converters.Mat_to_vector_Point(approxList.get(0), smallSquare);
                }
                bigSquare = sortList.sortIndividualList(bigSquare);
                smallSquare = sortList.sortIndividualList(smallSquare);

                if (smallSquare.get(0).x > bigSquare.get(0).x && smallSquare.get(2).y > bigSquare.get(2).y) {
                    Perspective perspective = new Perspective();
                    biggestSquare = perspective.applyPerspective(imgMatPerspective, bigSquare);
                } else {
                    biggestSquare = null;
                }
                break;
            }
        }
        if (biggestSquare == null)
            System.out.println("biggestSquare = " + biggestSquare);
        return biggestSquare;
    }

    public static List<Mat> findIndividualTests(Mat mat, int testNum) {

        List<Mat> matList = new ArrayList<>();
        Mat thres = new Mat();

        /**
         * Find all internal squares and remove the most external from the list
         */
        FindContoursSquares findContoursSquares = new FindContoursSquares();
        thres = preProcessing.binarizeImage(mat);
        System.out.println("thres = " + thres);
        List<List<Point>> listsOfPoints = findContoursSquares.FindTreeContours(thres);
        if (listsOfPoints.size() > 1) {
            listsOfPoints.remove(0);
        } else {
            System.out.println("listsOfPoints = " + listsOfPoints);
        }

        /**
         * Check if all required squares were found
         */
        if (listsOfPoints.size() == testNum) {
            /*
            Sort individual points
             */
            for (int i = 0; i < listsOfPoints.size(); i++) {
                Sort sortList = new Sort();
                listsOfPoints.set(i, sortList.sortIndividualList(listsOfPoints.get(i)));
            }

            /*Sort squares*/
            Sort sortAll = new Sort();
            listsOfPoints = sortAll.sortAllList(listsOfPoints);

            for (int i = 0; i < listsOfPoints.size(); i++) {
                Perspective perspective = new Perspective();
                Mat individualMat = new Mat();
                individualMat = perspective.applyPerspectiveIndividual(mat, listsOfPoints.get(i));
                // scale the image
                Imgproc.resize(individualMat, individualMat, new Size(90, 90));
                matList.add(individualMat);
                String fileName = String.format("E:/Unima/Proyectos/kMeansReconstructionJava/testPictures/marker%d.png", i);
                Imgcodecs.imwrite(fileName, individualMat);
            }
        } else {
            System.out.println("matList = " + matList);
        }

        return matList;
    }
}
