/**
 * Created by miloshzelembaba on 2017-10-16.
 */
// Imports the Google Cloud client library
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Vision {
    ImageAnnotatorClient vision;

    public Vision() {
        try {
            vision = ImageAnnotatorClient.create();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public String getTextFromImage(ByteString imgBytes){
        // Builds the image annotation request
        List<AnnotateImageRequest> requests = new ArrayList<>();
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        requests.add(request);

        // Performs label detection on the image file
        BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();
        String pictureText = "";

        for (AnnotateImageResponse res : responses) {
            if (res.hasError()) {
                System.out.printf("Error: %s\n", res.getError().getMessage());
                return "ERROR";
            }

            pictureText = res.getTextAnnotationsList().get(0).getAllFields().values().toArray()[1].toString();
            break;
        }

        return  pictureText;
    }

    public static void main(String... args) throws Exception {
        // Instantiates a client
        Vision vision = new Vision();

        // The path to the image file to annotate
        String fileName = "imgs/challenge_2.jpeg";
        // Reads the image file into memory
        Path path = Paths.get(fileName);
        byte[] data = Files.readAllBytes(path);
        ByteString imgBytes = ByteString.copyFrom(data);

        System.out.println(vision.getTextFromImage(imgBytes));
    }
}
