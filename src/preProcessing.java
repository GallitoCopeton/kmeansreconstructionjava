import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class preProcessing {
    public static Mat binarizeImage(Mat imgMat) {
        Mat gray = new Mat();

        Imgproc.cvtColor(imgMat, gray, Imgproc.COLOR_BGR2GRAY);
        System.out.println(gray.size());
        Mat gaussian = new Mat();
        Imgproc.GaussianBlur(gray, gaussian, new Size(3, 3), 0);

        Mat medianBlur = new Mat();
        Imgproc.medianBlur(gaussian, medianBlur, 7);

        Mat thres = new Mat();

        Imgproc.adaptiveThreshold(medianBlur, thres, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV,
                85, 2);

        return thres;
    }

    public static Mat ResizeImage(Mat imgMat, int scaleFactor) {

        Mat imgMatCopy = new Mat();
        imgMat.copyTo(imgMatCopy);

        if (imgMatCopy.cols() > imgMatCopy.rows()) {
            Core.flip(imgMatCopy.t(), imgMatCopy, 1);
        }

        int heightScale = (imgMatCopy.rows() * scaleFactor) / imgMatCopy.cols();

        Size newSize = new Size(scaleFactor, heightScale);

        Imgproc.resize(imgMatCopy, imgMatCopy, newSize);

        return imgMatCopy;
    }

    public static Mat equalizeHistogram(Mat bgrMat) {
        double clipLimit = 2.0;
        Size tileGridSize = new Size(8, 8);

        Mat yuvSrc = new Mat();

        //BGR to YUV
        Imgproc.cvtColor(bgrMat, yuvSrc, Imgproc.COLOR_BGR2YUV);

        //Extract Y
        List<Mat> yuvDst = new ArrayList<Mat>();
        Core.split(yuvSrc, yuvDst);

        //Equalize Y Histogram
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(clipLimit);
        clahe.setTilesGridSize(tileGridSize);
        clahe.apply(yuvDst.get(0), yuvDst.get(0));

        //Join Y to UV
        Core.merge(yuvDst, yuvSrc);

        //Transform YUV to HSV
        Imgproc.cvtColor(yuvSrc, bgrMat, Imgproc.COLOR_YUV2BGR);
        return bgrMat;
    }
}
