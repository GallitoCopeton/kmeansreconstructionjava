import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Imgcodecs imageCodecs = new Imgcodecs();
        String inputFolder = "./inputImages/";
        File dir = new File(inputFolder);
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        File[] files = new File(inputFolder).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                System.out.println("file = " + file);
                Mat originalImage = imageCodecs.imread(file.getAbsolutePath());
                Mat biggestSquare = CropImage.findBiggestSquare(originalImage);
                int k = 3;
                List<Mat> xmarkersMats = CropImage.findIndividualTests(biggestSquare, 4);
                List<Mat> kMeansReconstructedMarkers = new ArrayList<>();
                Map<String, String> specificResults = new HashMap<>();
                for (int i = 0; i < xmarkersMats.size(); i++) {
                    Mat finalMat = Cluster.cluster(xmarkersMats.get(i), k);
                    String fileName = String.format("E:/Unima/Proyectos/kMeansReconstructionJava/testPictures/kMeans%d.png", i);
                    Imgcodecs.imwrite(fileName, finalMat);
                    kMeansReconstructedMarkers.add(finalMat);
                }
                specificResults = XMarkers.individualAnalysis(xmarkersMats);
            }
        }
        /*s
        String path = "E:/Unima/Proyectos/kMeansReconstructionJava/testPictures/test.jpg";
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat originalImage = imageCodecs.imread(path);
        System.out.println("originalImage = " + originalImage);
        */


    }


}
