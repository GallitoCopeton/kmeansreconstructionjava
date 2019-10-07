import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;

public class Cluster {

    public static Mat cluster(Mat cutout, int k) {

        Mat samples = cutout.reshape(1, cutout.cols() * cutout.rows());
        Mat samples32f = new Mat();
        samples.convertTo(samples32f, CvType.CV_32F, 1.0 / 255.0);
        Mat labels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10, 1);

        Mat centers = new Mat();
        Core.kmeans(samples32f, k, labels, criteria, 10, Core.KMEANS_RANDOM_CENTERS, centers);
        return showClusters(cutout, labels, centers);
    }

    private static Mat showClusters(Mat cutout, Mat labels, Mat centers) {
        centers.convertTo(centers, CvType.CV_8UC1, 255.0);
        centers.reshape(3);
        List<Mat> clusters = new ArrayList<Mat>();
        for (int i = 0; i < centers.rows(); i++) {
            clusters.add(Mat.zeros(cutout.size(), cutout.type()));
        }
        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
        for (int i = 0; i < centers.rows(); i++) counts.put(i, 0);
        int rows = 0;
        for (int y = 0; y < cutout.rows(); y++) {
            for (int x = 0; x < cutout.cols(); x++) {
                int label = (int) labels.get(rows, 0)[0];
                int r = (int) centers.get(label, 2)[0];
                int g = (int) centers.get(label, 1)[0];
                int b = (int) centers.get(label, 0)[0];
                clusters.get(label).put(y, x, b, g, r);
                rows++;
            }
        }

        if (clusters.size() == 1) return clusters.get(0);
        else {
            Mat finalMat = new Mat();
            for (int i = 0; i < clusters.size(); i++) {
                if (i == 0) {
                    Core.addWeighted(clusters.get(i), 1, clusters.get(i + 1), 1, 0, finalMat);
                } else {
                    try {
                        Core.addWeighted(finalMat, 1, clusters.get(i + 1), 1, 0, finalMat);
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                }
            }
            return finalMat;
        }

    }
}
