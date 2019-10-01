import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.features2d.FeatureDetector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMarkers {
    private static final int PERCENTAGE = 33; //Empty Xmarker threshold
    private static final String TAG = "XMarkers";
    private static final double ANEM_THRESHOLD = 20.0;
    private static final String SHARED_KEY_ANEM = "anem_key";

    private static final char MSG_POSITIVE = 'p';
    private static final char MSG_NEGATIVE = 'n';
    private static final char MSG_ERROR = 'e';

    private static final int INDEX_CONTROL = 0;
    private static final int INDEX_GP120 = 1;
    private static final int INDEX_P24 = 2;
    private static final int INDEX_RV1681 = 3;
    private static final int INDEX_CFP10 = 4;
    private static final int INDEX_ESAT6 = 5;

    /**
     * Executes two analysis process for each XMarker:
     * 1) Areas: The results are given by the sum of blobs' diameter,
     * it is positive when the sum is above a threshold,
     * and negative when it is below the threshold
     * 2) CNN: The results are given by a convolutional neural network previously trained
     *
     * @param xmarkers List&lt;Mat&gt;: List of XMarkers
     * @return Map&lt;String, String&gt;: Results of each XMarker or error if it applies
     */
    public static Map<String, String> individualAnalysis(List<Mat> xmarkers) {


        Map<String, String> resultsMap = new HashMap<String, String>();

        String areasResults = "";
        String cnnResults = "";

        //Mask for areas
        String maskPath = "E:/Unima/Proyectos/kMeansReconstructionJava/testPictures/mask_inv.png";
        Mat matMask = ImageStats.getMask(maskPath);

        List<Marker> markerResultAreas = new ArrayList<>();

        FeatureDetector blobDetector = createBlobDetector();

        for (int i = 0; i < xmarkers.size(); i++) {
            //----------Mat used in Areas algorithm-----------------
            Mat imgMat = xmarkers.get(i);

            //RGB to BGR
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2BGR);

            //----------Mat used in CNN algorithm-----------------
            Mat imgMatCopy = imgMat.clone();

            // gray scales for the image
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2GRAY);

            // Gaussian Blur for the to reduce noise
            Imgproc.GaussianBlur(imgMat, imgMat, new Size(3, 3), 0);

            // apply the threshold
            Imgproc.threshold(imgMat, imgMat, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

            //bitmapBinarize = Bitmap.createBitmap(imgMat.cols(), imgMat.rows(), Bitmap.Config.RGB_565);
            //Utils.matToBitmap(imgMat, bitmapBinarize);

            double totalPx = imgMat.rows() * imgMat.cols();
            double whitePx = Core.countNonZero(imgMat);
            double percentaje = ((totalPx - whitePx) * 100) / totalPx;
            MatOfKeyPoint matOfKeyPoints = new MatOfKeyPoint();


            if (percentaje > PERCENTAGE) {

                Mat matAnd = new Mat();
                Core.bitwise_and(matMask, imgMat, matAnd);
                //bitmapAnd = Bitmap.createBitmap(matAnd.cols(), matAnd.rows(), Bitmap.Config.RGB_565);
                //Utils.matToBitmap(matAnd, bitmapAnd);

                Mat Ero = new Mat();
                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
                Imgproc.erode(matAnd, Ero, kernel);

                Mat Dil = new Mat();
                Imgproc.dilate(Ero, Dil, kernel);
                Imgproc.dilate(Dil, Dil, kernel);

                Core.bitwise_not(Dil, Dil);
                String fileName = String.format("E:/Unima/Proyectos/kMeansReconstructionJava/testPictures/dilatedBlobs%d.png", i);
                Imgcodecs.imwrite(fileName, Dil);
                blobDetector.detect(Dil, matOfKeyPoints);

                ////Areas thresholds algorithm
                float diameterBlob = blobSaveDiameter(matOfKeyPoints);
                String areaR = areasThreshold(diameterBlob, i);

                markerResultAreas.add(new Marker(diameterBlob, i, areaR)); //Add marker with areas result

            } else {
                ///------Empty results for XMarker--------///
                markerResultAreas.add(new Marker(0, i, "e")); //Add marker with areas result

                //// Line to add blobDiameter measures when empty
                blobSaveDiameter(null);
            }
        }
        resultsMap.put("areasResults", areasResults);
        resultsMap.put("cnnResults", cnnResults);
        System.out.println("markerResultAreas = " + markerResultAreas);
        return resultsMap;
    }

    private static float blobSaveDiameter(MatOfKeyPoint matOfKeyPoints) {
        float sumDiameter = 0;
        for (KeyPoint mat : matOfKeyPoints.toList()) {
            //Log.d(TAG, "KeyPoints" + mat.pt);
            //Log.d(TAG, "Diameter" + mat.size);
            sumDiameter += mat.size;
        }
        return sumDiameter;
    }


    /**
     * Implements a threshold to get the results for areas algorithm
     *
     * @param diameter  float: Sum of blobs' diameter of an XMarker
     * @param markerPos int: Location of XMarker on FIND test card
     * @return String: Result using areas algorithm for an XMarker
     */
    private static String areasThreshold(float diameter, int markerPos) {
        markerPos = 5 - markerPos;
        //Th for E6, CF, RV, P24, GP120, Ctrl = 26, 30, 36, 26
        //Th for E6, CF, RV, P24, GP120, Ctrl = 26, 30, 36, 26 **** 0.2.1
        int[] thresholds = {45, 48, 45, 37, 35, 35}; /// **** 0.3.1
        if (diameter >= thresholds[markerPos]) return "p";
        else return "n";
    }

    private static FeatureDetector createBlobDetector() {
        FeatureDetector blobDetector = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        //Create temp file for blob params
        try {
            File temp = File.createTempFile("tempFile", ".tmp");
            String settings = "%YAML:1.0\nthresholdStep: " + 10
                    + "\nminThreshold: " + 50
                    + "\nmaxThreshold: " + 220
                    + "\nminRepeatability: " + 2
                    + "\nminDistBetweenBlobs: " + 10
                    + "\nfilterByColor: " + 1
                    + "\nblobColor: " + 0
                    + "\nfilterByArea: " + 1
                    + "\nminArea: " + 25
                    + "\nmaxArea: " + 5000
                    + "\nfilterByCircularity: " + 0
                    + "\nminCircularity: " + 8.0000001192092896e-001
                    + "\nmaxCircularity: " + 3.4028234663852886e+038
                    + "\nfilterByInertia: " + 1
                    + "\nminInertiaRatio: " + 0.01
                    + "\nmaxInertiaRatio: " + 3.4028234663852886e+038
                    + "\nfilterByConvexity: " + 0
                    + "\nminConvexity: " + 0.05
                    + "\nmaxConvexity: " + 1.5;
            //System.out.println("\nFeature detector setting data: " + settings + "\n\n");

            FileWriter writer = new FileWriter(temp, false);
            writer.write(settings);
            writer.close();

            blobDetector.read(temp.getPath());

            temp.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blobDetector;
    }
}
