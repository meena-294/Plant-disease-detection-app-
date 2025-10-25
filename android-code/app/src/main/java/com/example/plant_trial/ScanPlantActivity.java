package com.example.plant_trial;
import com.example.plant_trial.utils.FileUtil;

import com.example.plant_trial.ResultActivity;
import com.example.plant_trial.CameraActivity;


import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.ULocale;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ScanPlantActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_CAMERA = 101;
    private static final String TAG = "ScanPlantActivity";
    private ImageView scanImagePreview;
    private Button takePhotoButton;
    private Button browseFileButton;
    private Uri imageUri = null;
    private Map<String, String[]> diseaseData = new HashMap<>();

    // --- TFLite Variables ---
    private Interpreter tflite;
    private int IMAGE_SIZE = 224; // Default value, will be updated by loadTFLiteModel
    private int NUM_CLASSES = 0; // Will be updated by loadTFLiteModel
    private TreeMap<Integer, String> classIndicesMap = new TreeMap<>(); // Index -> Label (using TreeMap to ensure sorted labels)
    private String[] labels; // Array of labels for easy indexing

    // Activity Launchers (kept for context)
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_plant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar_scan_plant);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scan Plant");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        scanImagePreview = findViewById(R.id.scan_image_preview);
        takePhotoButton = findViewById(R.id.button_take_photo);
        browseFileButton = findViewById(R.id.button_browse_file);

        // --- Model Initialization ---
        loadClassIndices();
        loadTFLiteModel();

        // Ensure diseaseData map is populated for mock results
        populateDiseaseData();

        // Initialize Launchers and Listeners
        initializeLaunchers();
        takePhotoButton.setOnClickListener(v -> launchCamera());
        browseFileButton.setOnClickListener(v -> launchGallery());

        // Add listener to the image preview (for testing if an image is loaded)
        findViewById(R.id.button_analyze).setOnClickListener(v -> {
            if (tflite == null) {
                // This toast will appear if the model load failed in onCreate
                Toast.makeText(this, "Model is not ready. Cannot analyze image.", Toast.LENGTH_LONG).show();
            } else if (imageUri == null) {
                Toast.makeText(this, "Please select or take an image first.", Toast.LENGTH_SHORT).show();
            } else {
                analyzeImage(imageUri);
            }
        });
    }

    // region TFLITE MODEL LOADING METHODS

    /**
     * Loads the class indices (index-to-label mapping) from the class_indices.json file.
     * Note: Assumes class_indices.json uses string keys (labels) and integer values (indices), e.g., {"Tomato___healthy": 0, ...}
     */
    private void loadClassIndices() {
        classIndicesMap.clear();
        try {
            InputStream is = getAssets().open("class_indices.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonString);

            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next(); // The index as a String (e.g., "0", "1", "2")
                String value = jsonObject.getString(key); // The disease label (e.g., "Apple___Apple_scab")

                int index = Integer.parseInt(key); // Convert the String key to an integer
                classIndicesMap.put(index, value); // Store as (Integer Index -> String Label)
            }

            // Populate the labels array from the sorted map values
            labels = classIndicesMap.values().toArray(new String[0]);
            NUM_CLASSES = labels.length;

            Log.d(TAG, "Class Indices loaded successfully. Total classes: " + classIndicesMap.size());
        } catch (IOException | JSONException e) {
            Log.e(TAG, "ERROR: Failed to load class indices. Check class_indices.json in assets.", e);
            Toast.makeText(this, "ERROR: Failed to load class indices.", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Loads the TFLite model from the assets folder.
     */
    private void loadTFLiteModel() {
        // First check if indices were loaded successfully
        if (classIndicesMap.isEmpty() || NUM_CLASSES == 0) {
            Log.e(TAG, "Class indices not loaded. Aborting model load.");
            Toast.makeText(this, "ERROR: Class indices missing. Cannot load model.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // This is the line that will throw IOException if the file is missing/corrupt
            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, "plant_model.tflite");

            // Initialize the interpreter
            tflite = new Interpreter(tfliteModel);

            // --- MODEL DEBUG: Get Input and Output Shapes/Types ---
            int[] inputShape = tflite.getInputTensor(0).shape();
            DataType inputType = tflite.getInputTensor(0).dataType();
            int[] outputShape = tflite.getOutputTensor(0).shape();

            // Set global constants based on model signature
            if (inputShape.length == 4) {
                // Assuming format [1, HEIGHT, WIDTH, CHANNELS]
                IMAGE_SIZE = inputShape[1];
            }
            // Verify output shape matches the number of labels loaded
            if (outputShape.length == 2 && outputShape[1] != NUM_CLASSES) {
                Log.e(TAG, "MISMATCH: Model output size (" + outputShape[1] + ") != Label count (" + NUM_CLASSES + ")");
                Toast.makeText(this, "WARNING: Model size vs. Label size mismatch!", Toast.LENGTH_LONG).show();
            } else if (outputShape.length == 1 && outputShape[0] != NUM_CLASSES) {
                Log.e(TAG, "MISMATCH: Model output size (" + outputShape[0] + ") != Label count (" + NUM_CLASSES + ")");
                Toast.makeText(this, "WARNING: Model size vs. Label size mismatch!", Toast.LENGTH_LONG).show();
            }


            Log.d(TAG, "Model loaded successfully.");
            Log.d("MODEL DEBUG", "Input Shape: " + Arrays.toString(inputShape) + ", Type: " + inputType);
            Log.d("MODEL DEBUG", "Output Shape: " + Arrays.toString(outputShape) + ", Calculated Image Size: " + IMAGE_SIZE + ", Calculated Num Classes: " + NUM_CLASSES);

        } catch (IOException e) {
            // This CATCH block is why you are getting the error pop-up
            Log.e(TAG, "ERROR: Failed to load TFLite model.", e);
            e.printStackTrace();
            Toast.makeText(this, "ERROR: Failed to load TFLite model: " + e.getMessage(), Toast.LENGTH_LONG).show();
            tflite = null; // Ensure tflite is null on failure
        }
    }

    // endregion TFLITE MODEL LOADING METHODS

    // region IMAGE HANDLING AND ANALYSIS

    private void analyzeImage(Uri imageUri) {
        if (tflite == null) {
            Toast.makeText(this, "Model not initialized. Check logs for error.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // 1. Image Pre-processing
            TensorImage tImage = new TensorImage(tflite.getInputTensor(0).dataType());
            tImage.load(bitmap);

            // Assuming standard model input: float32, normalized to 0-1 or -1-1
            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(IMAGE_SIZE, IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                    // Normalize the pixel values from 0-255 to 0-1.0
                    .add(new NormalizeOp(0.0f, 255.0f))
                    .build();

            tImage = imageProcessor.process(tImage);

            // 2. Prepare Output Buffer (assuming float32 output)
            TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, NUM_CLASSES}, DataType.FLOAT32);

            // 3. Run Inference (Input: TensorImage buffer, Output: OutputBuffer)
            tflite.run(tImage.getBuffer(), outputBuffer.getBuffer().rewind());

            // 4. Post-processing and Result
            float[] probabilities = outputBuffer.getFloatArray();

            // Get the highest confidence prediction
            String detectedDisease = "Unknown";
            float highestConfidence = -1.0f;
            int maxIndex = -1;

            for (int i = 0; i < probabilities.length; i++) {
                if (probabilities[i] > highestConfidence) {
                    highestConfidence = probabilities[i];
                    maxIndex = i;
                }
            }

            if (maxIndex != -1 && maxIndex < labels.length) {
                detectedDisease = labels[maxIndex];
                Log.d(TAG, "Detected Disease: " + detectedDisease + " with confidence: " + highestConfidence);
            } else {
                Log.e(TAG, "Error: Max index " + maxIndex + " out of bounds for labels array length " + labels.length);
            }

            // Get treatment and suggestions based on detected disease
            String[] details = diseaseData.getOrDefault(detectedDisease, new String[]{
                    "No specific treatment found.",
                    "No specific suggestions available."
            });
            String treatment = details[0];
            String suggestions = details[1];

            // Launch the ResultActivity with the analysis results
            Intent resultIntent = new Intent(this, ResultActivity.class);
            resultIntent.putExtra("imageUri", imageUri.toString());
            resultIntent.putExtra("disease", detectedDisease + " (Confidence: " + String.format("%.2f%%", highestConfidence * 100) + ")");
            resultIntent.putExtra("treatment", treatment);
            resultIntent.putExtra("suggestions", suggestions);
            startActivity(resultIntent);

        } catch (IOException e) {
            Toast.makeText(this, "Failed to analyze image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Image analysis error: ", e);
        }
    }

    // endregion IMAGE HANDLING AND ANALYSIS


    // region LAUNCHERS AND UTILS (Keep your existing methods)

    // Initialize the ActivityResultLaunchers
    private void initializeLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String uriString = result.getData().getStringExtra("image_uri");
                        if (uriString != null) {
                            imageUri = Uri.parse(uriString);
                            scanImagePreview.setImageURI(imageUri);
                            Toast.makeText(this, "Image Ready for Analysis.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        scanImagePreview.setImageURI(imageUri);
                        Toast.makeText(this, "Image Ready for Analysis.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // Start CameraActivity
    private void launchCamera() {
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        cameraLauncher.launch(cameraIntent);
    }

    // Launch gallery intent
    private void launchGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    // Simple mock data for demo
    private void populateDiseaseData() {
        diseaseData.put("Tomato___Bacterial_spot", new String[]{
                "Apply copper-based fungicides weekly.",
                "Ensure good air circulation and avoid overhead watering."
        });
        diseaseData.put("Tomato___Early_blight", new String[]{
                "Use fungicides containing maneb or chlorothalonil. Remove infected leaves.",
                "Rotate crops yearly and mulch heavily to reduce splash transmission."
        });
        diseaseData.put("Tomato___healthy", new String[]{
                "Keep up the good work! No treatment needed.",
                "Continue monitoring, fertilize appropriately, and ensure consistent watering."
        });
        // Add all other diseases from your class_indices.json here for proper results
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

    // endregion LAUNCHERS AND UTILS

//    private static final int REQUEST_CODE_CAMERA = 101;
//    private static final String TAG = "ScanPlantActivity";
//    private ImageView scanImagePreview;
//    private Button takePhotoButton;
//    private Button browseFileButton;
//    private Button buttonAnalyze; // Added button variable
//
//    private Uri imageUri ;
//    private Map<String, String[]> diseaseData = new HashMap<>();
//
//    // --- TFLite Variables ---
//    private Interpreter tflite;
//    private int IMAGE_SIZE = 224; // Default value, will be updated by loadTFLiteModel
//    private int NUM_CLASSES = 0; // Will be updated by loadTFLiteModel
//    private TreeMap<Integer, String> classIndicesMap = new TreeMap<>(); // Index -> Label (using TreeMap to ensure sorted labels)
//    private String[] labels; // Array of labels for easy indexing
//
//    // Activity Launchers (kept for context)
//    private ActivityResultLauncher<Intent> cameraLauncher;
//    private ActivityResultLauncher<Intent> galleryLauncher;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_scan_plant);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        Toolbar toolbar = findViewById(R.id.toolbar_scan_plant);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setTitle("Scan Plant");
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//
//        scanImagePreview = findViewById(R.id.scan_image_preview);
//        takePhotoButton = findViewById(R.id.button_take_photo);
//        browseFileButton = findViewById(R.id.button_browse_file);
//        buttonAnalyze = findViewById(R.id.button_analyze); // Initialize the analyze button
//
//
//        // --- Model Initialization ---
//        loadClassIndices();
//        loadTFLiteModel();
//
//        // Ensure diseaseData map is populated for mock results
//        //populateDiseaseData();
//
//        // Initialize Launchers and Listeners
//        initializeLaunchers();
//        takePhotoButton.setOnClickListener(v -> launchCamera());
//        browseFileButton.setOnClickListener(v -> launchGallery());
//
//        // Add listener to the image preview (for testing if an image is loaded)
//        buttonAnalyze.setOnClickListener(v -> {
//            if (tflite == null) {
//                // This toast will appear if the model load failed in onCreate
//                Toast.makeText(this, "Model is not ready. Cannot analyze image.", Toast.LENGTH_LONG).show();
//            } else if (imageUri == null) {
//                Toast.makeText(this, "Please select or take an image first.", Toast.LENGTH_SHORT).show();
//            } else {
//                analyzeImage(imageUri);
//            }
//        });
//    }
//
//    // region TFLITE MODEL LOADING METHODS
//
//    /**
//     * Loads the class indices (index-to-label mapping) from the class_indices.json file.
//     * Note: Assumes class_indices.json uses string keys (labels) and integer values (indices), e.g., {"Tomato___healthy": 0, ...}
//     */
//    private void loadClassIndices() {
//        classIndicesMap.clear();
//        try {
//            InputStream is = getAssets().open("class_indices.json");
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//            String jsonString = new String(buffer, StandardCharsets.UTF_8);
//            JSONObject jsonObject = new JSONObject(jsonString);
//
//            Iterator<String> keys = jsonObject.keys();
//            while (keys.hasNext()) {
//                String key = keys.next(); // The index as a String (e.g., "0", "1", "2")
//                String value = jsonObject.getString(key); // The disease label (e.g., "Apple___Apple_scab")
//
//                int index = Integer.parseInt(key); // Convert the String key to an integer
//                classIndicesMap.put(index, value); // Store as (Integer Index -> String Label)
//            }
//
//            // Populate the labels array from the sorted map values
//            labels = classIndicesMap.values().toArray(new String[0]);
//            NUM_CLASSES = labels.length;
//
//            Log.d(TAG, "Class Indices loaded successfully. Total classes: " + classIndicesMap.size());
//        } catch (IOException | JSONException e) {
//            Log.e(TAG, "ERROR: Failed to load class indices. Check class_indices.json in assets.", e);
//            Toast.makeText(this, "ERROR: Failed to load class indices.", Toast.LENGTH_LONG).show();
//        }
//    }
//
//
//
//
//    /**
//     * Loads the TFLite model from the assets folder.
//     */
//    private void loadTFLiteModel() {
//        // First check if indices were loaded successfully
//        if (classIndicesMap.isEmpty() || NUM_CLASSES == 0) {
//            Log.e(TAG, "Class indices not loaded. Aborting model load.");
//            Toast.makeText(this, "ERROR: Class indices missing. Cannot load model.", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        try {
//            // This is the line that will throw IOException if the file is missing/corrupt
//            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, "plant_model.tflite");
//
//            // Initialize the interpreter
//            tflite = new Interpreter(tfliteModel);
//
//            // --- MODEL DEBUG: Get Input and Output Shapes/Types ---
//            int[] inputShape = tflite.getInputTensor(0).shape();
//            DataType inputType = tflite.getInputTensor(0).dataType();
//            int[] outputShape = tflite.getOutputTensor(0).shape();
//
//            // Set global constants based on model signature
//            if (inputShape.length == 4) {
//                // Assuming format [1, HEIGHT, WIDTH, CHANNELS]
//                IMAGE_SIZE = inputShape[1];
//            }
//            // Verify output shape matches the number of labels loaded
//            if (outputShape.length == 2 && outputShape[1] != NUM_CLASSES) {
//                Log.e(TAG, "MISMATCH: Model output size (" + outputShape[1] + ") != Label count (" + NUM_CLASSES + ")");
//                Toast.makeText(this, "WARNING: Model size vs. Label size mismatch!", Toast.LENGTH_LONG).show();
//            } else if (outputShape.length == 1 && outputShape[0] != NUM_CLASSES) {
//                Log.e(TAG, "MISMATCH: Model output size (" + outputShape[0] + ") != Label count (" + NUM_CLASSES + ")");
//                Toast.makeText(this, "WARNING: Model size vs. Label size mismatch!", Toast.LENGTH_LONG).show();
//            }
//
//
//            Log.d(TAG, "Model loaded successfully.");
//            Log.d("MODEL DEBUG", "Input Shape: " + Arrays.toString(inputShape) + ", Type: " + inputType);
//            Log.d("MODEL DEBUG", "Output Shape: " + Arrays.toString(outputShape) + ", Calculated Image Size: " + IMAGE_SIZE + ", Calculated Num Classes: " + NUM_CLASSES);
//
//        } catch (IOException e) {
//            // This CATCH block is why you are getting the error pop-up
//            Log.e(TAG, "ERROR: Failed to load TFLite model.", e);
//            e.printStackTrace();
//            Toast.makeText(this, "ERROR: Failed to load TFLite model: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            tflite = null; // Ensure tflite is null on failure
//        }
//    }
//
//    // endregion TFLITE MODEL LOADING METHODS
//
//    // region IMAGE HANDLING AND ANALYSIS
//
//    private void analyzeImage(Uri imageUri) {
//        if (tflite == null) {
//            Toast.makeText(this, "Model not initialized.", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//
//            TensorImage tImage = new TensorImage(tflite.getInputTensor(0).dataType());
//            tImage.load(bitmap);
//
//            ImageProcessor imageProcessor = new ImageProcessor.Builder()
//                    .add(new ResizeOp(IMAGE_SIZE, IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
//                    .add(new NormalizeOp(0.0f, 255.0f))
//                    .build();
//            tImage = imageProcessor.process(tImage);
//
//            TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, NUM_CLASSES}, DataType.FLOAT32);
//            tflite.run(tImage.getBuffer(), outputBuffer.getBuffer().rewind());
//
//            float[] probabilities = outputBuffer.getFloatArray();
//            int maxIndex = -1;
//            float highestConfidence = -1.0f;
//            for (int i = 0; i < probabilities.length; i++) {
//                if (probabilities[i] > highestConfidence) {
//                    highestConfidence = probabilities[i];
//                    maxIndex = i;
//                }
//            }
//
//            String detectedDisease = "Unknown";
//            if (maxIndex != -1) {
//                detectedDisease = labels[maxIndex];
//            }
//
//            // --- CHANGED: This section is now simplified as per our previous discussion ---
//            // This activity's only job is to get the disease name and pass it to ResultActivity.
//            // ResultActivity will then handle the logic for treatments and suggestions.
//            Intent resultIntent = new Intent(this, ResultActivity.class);
//            resultIntent.putExtra("IMAGE_URI", imageUri); // Pass the Uri object directly
//            resultIntent.putExtra("DISEASE_NAME", detectedDisease); // Use the key we defined
//            startActivity(resultIntent);
//
//        } catch (IOException e) {
//            Toast.makeText(this, "Failed to analyze image: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            Log.e(TAG, "Image analysis error: ", e);
//        }
//
//
//
//
//
//
//
//
//    }
//
//    // endregion IMAGE HANDLING AND ANALYSIS
//
//
//    // region LAUNCHERS AND UTILS (Keep your existing methods)
//
//    // Initialize the ActivityResultLaunchers
//    private void initializeLaunchers() {
//        cameraLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                        String uriString = result.getData().getStringExtra("image_uri");
//                        if (uriString != null) {
//                            imageUri = Uri.parse(uriString);
//                            scanImagePreview.setImageURI(imageUri);
//                            Toast.makeText(this, "Image Ready for Analysis.", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//        );
//
//        galleryLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                        imageUri = result.getData().getData();
//                        scanImagePreview.setImageURI(imageUri);
//                        Toast.makeText(this, "Image Ready for Analysis.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//    }
//
//    // Start CameraActivity
//    private void launchCamera() {
//        Intent cameraIntent = new Intent(this, CameraActivity.class);
//        cameraLauncher.launch(cameraIntent);
//    }
//
//    // Launch gallery intent
//    private void launchGallery() {
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
//        galleryIntent.setType("image/*");
//        galleryLauncher.launch(galleryIntent);
//    }
//
//    // Simple mock data for demo
//    private void populateDiseaseData() {
//        diseaseData.put("Tomato___Bacterial_spot", new String[]{
//                "Apply copper-based fungicides weekly.",
//                "Ensure good air circulation and avoid overhead watering."
//        });
//        diseaseData.put("Tomato___Early_blight", new String[]{
//                "Use fungicides containing maneb or chlorothalonil. Remove infected leaves.",
//                "Rotate crops yearly and mulch heavily to reduce splash transmission."
//        });
//        diseaseData.put("Tomato___healthy", new String[]{
//                "Keep up the good work! No treatment needed.",
//                "Continue monitoring, fertilize appropriately, and ensure consistent watering."
//        });
//        // Add all other diseases from your class_indices.json here for proper results
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
//
//    // endregion LAUNCHERS AND UTILS









