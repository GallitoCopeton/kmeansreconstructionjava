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
        // Para utilización de openCv
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Imgcodecs imageCodecs = new Imgcodecs();
        String inputFolderName = "../Positives/";
        File[] listImages = new File(inputFolderName).listFiles();
        for (File image : listImages) {
            if (image.isFile()) {
                System.out.println("Name of image = " + image);
                // Leer imagen original
                Mat originalImage = imageCodecs.imread(image.getAbsolutePath());
                // Obtener cuadro de prueba
                Mat biggestSquare = CropImage.findBiggestSquare(originalImage);
                // Ecualizar histograma
                //biggestSquare = preProcessing.equalizeHistogram(biggestSquare);
                // Obtener cuadros de prueba individuales
                List<Mat> xmarkersMats = CropImage.findIndividualTests(biggestSquare, 4);
                // Declaración de marcadores reconstruidos
                List<Mat> kMeansReconstructedMarkers = new ArrayList<>();
                // Declaración hashmap de resultados
                Map<String, String> specificResults = new HashMap<>();
                int k = 3;
                for (int i = 0; i < xmarkersMats.size(); i++) {
                    // Reconstrucción d           e marcador con k centros/colores
                    Mat finalMat = Cluster.cluster(xmarkersMats.get(i), k);
                    String fileName = String.format("../testPictures/kMeans%d.png", i);
                    Imgcodecs.imwrite(fileName, finalMat);
                    kMeansReconstructedMarkers.add(finalMat);
                }
                specificResults = XMarkers.individualAnalysis(kMeansReconstructedMarkers);
            }
        }
    }
}
