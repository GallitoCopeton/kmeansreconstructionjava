import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This class has methods needed to estimate color statistics and dilution of an XMarker
 * @author Unima Diagnosticos
 * @version 2018/04/01
 * @since 1.0.0
 */

public class ImageStats {

    private static double H_mean, V_mean , G_median, G_mean, G_std;
    private static double dilution;
    private static final String TAG = "ImageStats";


    /**
     * Gets the XMarker Mask on binary (GRAY) format from a local resource
     * @param resource int: Location of the resource that contains the XMarker Mask
     * @return Mat: Mask on binary (GRAY) format
     */
    public static Mat getMask(String maskPath){
        Mat matMask = new Mat();
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat originalMask = imageCodecs.imread(maskPath);
        System.out.println("originalMask = " + originalMask);
        Imgproc.resize(originalMask, matMask, new Size(90, 90));
        Imgproc.cvtColor(matMask, matMask, Imgproc.COLOR_BGR2GRAY);
        // Gaussian Blur for the to reduce noise
        Imgproc.GaussianBlur(matMask, matMask, new Size(3, 3), 3);
        Imgproc.adaptiveThreshold(matMask, matMask, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
                75, 4);
        return matMask;
    }


    /**
     * Estimates the NOT values of every layer of a Mat image
     * @param img Mat: Input image
     * @return Mat: Image with NOT values
     */
    private static Mat negative(Mat img){
        Mat control = new Mat();
        Core.bitwise_not(img, control);
        return control;
    }

    /**
     * This method sets the color stats on each layer of the HSV model.
     * @param img  Mat: Input Mat Image in BGR color format
     * @param mask Mat: XMarker Mask
     */
    private static void setColorStats(Mat img, Mat mask){
        //Log.d("ImageStats", "Img Channels: " + img.channels() + ", Mask channels: " + mask.channels());
        Mat control = new Mat();
        control = andBgrMask(img,mask);
        control = negative(control);
        Imgproc.cvtColor(control, control, Imgproc.COLOR_BGR2HSV);
        List<Mat> HSV = new ArrayList<>();
        Core.split(control, HSV);
        Map<String,List<Double>> h = limitFlattenStats(HSV.get(0), 50, 150);
        //Map<String,List<Double>> s = limitFlattenStats(HSV.get(1), 15, 255);
        Map<String,List<Double>> v = limitFlattenStats(HSV.get(2), 10, 255);
        int nh = h.get("array").size();
        //int nv = v.size();
        //H_median = median(h.get("array"),nh);
        H_mean = h.get("stats").get(0);
        V_mean = v.get("stats").get(0);
    }

    //Only mantain ROI non-black

    /**
     * Logical AND operation between and image and its corresponding mask, it is applied pixel by pixel
     * It is useful for BGR Image and GRAY Mask
     * @param img Mat: Mat image in BGR color format
     * @param mask Mat: Mat XMarker Mask in GRAY color format
     * @return Mat: Result of AND operation between input image and its corresponding mask
     */
    private static Mat andBgrMask(Mat img, Mat mask){
        for(int i = 0; i < img.rows(); i++){
            for(int j = 0; j < img.cols(); j++){
                double[] pxs = mask.get(i,j);
                if(pxs[0] < 1)
                    img.put(i,j, new double[]{0,0,0});
            }
        }
        /*Bitmap bitDebugging = null;
        bitDebugging = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bitDebugging);*/
        return img;
    }

    /**
     * This method sets the gray stats for an input image
     * @param img Mat: Input image in BGR color format
     * @param mask Mat: XMarker Mask in binary (GRAY) format
     */
    private static void setGrayStats(Mat img, Mat mask){
        Mat control = new Mat();
        Imgproc.cvtColor(img, control, Imgproc.COLOR_BGR2GRAY);
        Core.bitwise_and(mask, control, control);
        Map<String,List<Double>> c = limitFlattenStats(control, 5, 255);
        int n = c.get("array").size();
        G_mean = c.get("stats").get(0);
        G_std = c.get("stats").get(1);
        G_median = median(c.get("array"), n);
    }

    /**
     * Estimates color stats for a Mat image in a linear way
     * @param img Mat: Input image in BGR color format
     * @param th1 int: Lower threshold
     * @param th2 int: Higher threshold
     * @return Map&lt;String,List&lt;Double&gt;&gt; Map with the stats by layer
     */
    public static Map<String,List<Double>> limitFlattenStats(Mat img, int th1, int th2){
        double mean = 0, std = 0, sum = 0, sumSq = 0;
        Map<String, List<Double>> map = new HashMap<>(); //Map return flatten list and statistics from px
        Mat flat = img.reshape(1,1);
        List<Double> limited = new ArrayList<>();
        for(int i = 0; i < flat.rows(); i++){
            for (int j = 0; j < flat.cols(); j++){
                double px = flat.get(i,j)[0];
                if(px >= th1 && px <= th2) {
                    limited.add(px);
                    sum += px; //Sum
                    sumSq += px * px; //Square Sum
                }
            }
        }
        int n = limited.size();

        //-----Mean------//
        mean = sum/n;

        //---Median----//
        //Double median = median(limited);

        //----Std-Naive algorithm-----https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance//
        double var = (sumSq - (sum * sum)/n)/n;
        std = Math.sqrt(var);
        //Log.d(TAG, "Efficient mean: " + mean + ", std: " + std + ", median: " + median);

        map.put("array", limited); //Flatten list
        //List of stats
        List<Double> stats = new ArrayList<>();
        stats.add(mean);
        stats.add(std);
        //stats.add(median);

        map.put("stats", stats);
        return map;
    }

    /**
     * Estimates mean value for a list of Double
     * @param list List&lt;Double&gt;: List whose values is the mean estimated
     * @param n: Size of list
     * @return double: Estimated mean
     * @deprecated
     */
    public static double mean(List<Double> list, int n){
        double sum = 0;
        double mean = 0;
        for (Double i: list){
            sum += i;
        }
        mean = sum/n;
        //Log.d(TAG, "Less efficient mean: " + mean);
        return mean;
    }

    /**
     * Estimates the median value of a List of Double values
     * @param list List&lt;Double&gt;: List whose values is the median estimated
     * @param n Size of list
     * @return double: Estimated median
     */
    public static double median(List<Double> list, int n){
        Collections.sort(list);
        double median;
        if (list.size() % 2 == 0)
            median = ((double) list.get(n/2) + (double) list.get((int)n/2 - 1))/2;
        else
            median = (double) list.get(n/2);
        //Log.d(TAG, "Less efficient median: " + median);
        return median;
    }

    /**
     * Estimates the standard deviation of a List of Double values
     * @param list List&lt;Double&gt;: List whose values is the std estimated
     * @param mean double: Input mean
     * @param n Size of list
     * @return double: Estimated standard deviation
     */
    public static double std(List<Double> list, double mean, int n){
        double sum = 0;
        double std = 0;
        for (Double i: list){
            sum += Math.pow((i-mean),2);
        }
        std = Math.sqrt(sum/n);
        //Log.d(TAG, "Less efficient std: " + std);
        return std;
    }

    /**
     * Normalization of the estimated stats using constant lists of means and stds
     */
    public static void normalization(){
        //Values extracted from training data

        double[] means = {102.7573694,   231.71432275,  100.22306322,   94.40167337,   38.29574989};
        //Values extracted from training data
        double[] stds = {5.60506244,   3.28995169, 17.29822933,  12.46255108,   5.67911964};

        //H_median = (H_median - means[0]) / stds[0];
        H_mean = (H_mean - means[0]) / stds[0];
        V_mean = (V_mean - means[1]) / stds[1];
        G_median = (G_median - means[2]) / stds[2];
        G_mean = (G_mean - means[3]) / stds[3];
        G_std = (G_std - means[4]) / stds[4];
    }

}
