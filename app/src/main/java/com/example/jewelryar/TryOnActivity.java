package com.example.jewelryar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TryOnActivity extends AppCompatActivity {

    private static final boolean EARRING_IS_PAIR = true;

    // Ring size: bigger = bigger ring (0.9 smaller, 1.5 bigger)
    private static final float RING_SIZE = 0.9f;
    // Position along the finger: 0.3 = near knuckle, 0.6 = near tip
    private static final float RING_POS = 0.45f;

    // 0 thumb, 1 index, 2 middle, 3 ring, 4 little
    private static final int[][] FINGERS = {{2, 3}, {5, 6}, {9, 10}, {13, 14}, {17, 18}};

    private volatile int selectedFinger = 3;
    private Button[] fingerButtons;

    // true = front camera, false = back camera
    private volatile boolean useFront = true;

    private PreviewView previewView;
    private OverlayView overlay;
    private Product product;

    private FaceLandmarker faceLandmarker;
    private HandLandmarker handLandmarker;
    private ExecutorService analysisExecutor;

    private volatile Bitmap earLeft, earRight;
    private volatile Bitmap necklaceBmp;
    private volatile Bitmap ringBmp;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission is needed for try-on", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tryon);

        previewView = findViewById(R.id.previewView);
        overlay = findViewById(R.id.overlay);
        TextView tvName = findViewById(R.id.tvProductName);

        product = (Product) getIntent().getSerializableExtra("product");
        if (product != null) tvName.setText(product.name);

        // Rings are easier to try with the back camera, so start there
        if (product != null && "ring".equals(product.category)) useFront = false;

        findViewById(R.id.btnFlip).setOnClickListener(v -> {
            useFront = !useFront;
            overlay.setSprites(new ArrayList<>());
            startCamera();
        });

        setupFingerButtons();

        analysisExecutor = Executors.newSingleThreadExecutor();
        setupLandmarkers();
        loadJewelryPicture();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void setupFingerButtons() {
        if (product == null || !"ring".equals(product.category)) return;

        findViewById(R.id.fingerBar).setVisibility(View.VISIBLE);

        fingerButtons = new Button[]{
                findViewById(R.id.btnThumb),
                findViewById(R.id.btnIndex),
                findViewById(R.id.btnMiddle),
                findViewById(R.id.btnRing),
                findViewById(R.id.btnLittle)
        };

        for (int i = 0; i < fingerButtons.length; i++) {
            final int index = i;
            fingerButtons[i].setOnClickListener(v -> {
                selectedFinger = index;
                highlightFinger();
            });
        }
        highlightFinger();
    }

    private void highlightFinger() {
        for (int i = 0; i < fingerButtons.length; i++) {
            fingerButtons[i].setAlpha(i == selectedFinger ? 1f : 0.45f);
        }
    }

    // Removes the empty transparent space around a picture
    private Bitmap cropTransparent(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);

        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int alpha = (px[y * w + x] >>> 24);
                if (alpha > 20) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < minX || maxY < minY) return src;
        return Bitmap.createBitmap(src, minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private void loadJewelryPicture() {
        if (product == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Bitmap full = Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(product.tryon_image)
                        .submit()
                        .get();

                int w = full.getWidth();
                int h = full.getHeight();

                if ("necklace".equals(product.category)) {
                    necklaceBmp = full;
                } else if ("ring".equals(product.category)) {
                    ringBmp = cropTransparent(full);
                } else if (EARRING_IS_PAIR) {
                    earLeft = Bitmap.createBitmap(full, 0, 0, w / 2, h);
                    earRight = Bitmap.createBitmap(full, w / 2, 0, w - w / 2, h);
                } else {
                    Matrix flip = new Matrix();
                    flip.preScale(-1f, 1f);
                    earLeft = full;
                    earRight = Bitmap.createBitmap(full, 0, 0, w, h, flip, true);
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Picture error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void setupLandmarkers() {
        try {
            if (product != null && "ring".equals(product.category)) {
                BaseOptions base = BaseOptions.builder()
                        .setModelAssetPath("hand_landmarker.task")
                        .build();
                HandLandmarker.HandLandmarkerOptions options =
                        HandLandmarker.HandLandmarkerOptions.builder()
                                .setBaseOptions(base)
                                .setRunningMode(RunningMode.VIDEO)
                                .setNumHands(2)
                                .build();
                handLandmarker = HandLandmarker.createFromOptions(this, options);
            } else {
                BaseOptions base = BaseOptions.builder()
                        .setModelAssetPath("face_landmarker.task")
                        .build();
                FaceLandmarker.FaceLandmarkerOptions options =
                        FaceLandmarker.FaceLandmarkerOptions.builder()
                                .setBaseOptions(base)
                                .setRunningMode(RunningMode.VIDEO)
                                .setNumFaces(1)
                                .build();
                faceLandmarker = FaceLandmarker.createFromOptions(this, options);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Model error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();
                analysis.setAnalyzer(analysisExecutor, this::analyzeFrame);

                CameraSelector selector = useFront
                        ? CameraSelector.DEFAULT_FRONT_CAMERA
                        : CameraSelector.DEFAULT_BACK_CAMERA;

                provider.unbindAll();
                provider.bindToLifecycle(this, selector, preview, analysis);
            } catch (Exception e) {
                Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeFrame(ImageProxy imageProxy) {
        try {
            if (product == null) return;

            Bitmap raw = imageProxy.toBitmap();
            Matrix m = new Matrix();
            m.postRotate(imageProxy.getImageInfo().getRotationDegrees());
            Bitmap upright = Bitmap.createBitmap(raw, 0, 0, raw.getWidth(), raw.getHeight(), m, true);
            MPImage mpImage = new BitmapImageBuilder(upright).build();

            float viewW = overlay.getWidth();
            float viewH = overlay.getHeight();
            float imgW = upright.getWidth();
            float imgH = upright.getHeight();
            float scale = Math.max(viewW / imgW, viewH / imgH);
            float offsetX = (viewW - imgW * scale) / 2f;
            float offsetY = (viewH - imgH * scale) / 2f;

            List<OverlayView.Sprite> sprites = new ArrayList<>();

            if ("ring".equals(product.category)) {
                if (handLandmarker == null) return;
                HandLandmarkerResult hr =
                        handLandmarker.detectForVideo(mpImage, SystemClock.uptimeMillis());

                Bitmap ring = ringBmp;
                if (!hr.landmarks().isEmpty() && ring != null) {
                    for (List<NormalizedLandmark> hand : hr.landmarks()) {
                        int[] f = FINGERS[selectedFinger];
                        float[] p1 = toScreen(hand.get(f[0]), imgW, imgH, scale, offsetX, offsetY);
                        float[] p2 = toScreen(hand.get(f[1]), imgW, imgH, scale, offsetX, offsetY);

                        float dx = p2[0] - p1[0];
                        float dy = p2[1] - p1[1];
                        float segLen = (float) Math.hypot(dx, dy);
                        float angle = (float) Math.toDegrees(Math.atan2(dx, -dy));

                        float cx = p1[0] + dx * RING_POS;
                        float cy = p1[1] + dy * RING_POS;
                        float ringWidth = segLen * RING_SIZE;
                        float ringHeight = ringWidth * ring.getHeight() / (float) ring.getWidth();

                        sprites.add(new OverlayView.Sprite(ring, cx, cy - ringHeight / 2f, ringWidth, angle));
                    }
                }
            } else {
                if (faceLandmarker == null) return;
                FaceLandmarkerResult result =
                        faceLandmarker.detectForVideo(mpImage, SystemClock.uptimeMillis());

                if (!result.faceLandmarks().isEmpty()) {
                    List<NormalizedLandmark> face = result.faceLandmarks().get(0);

                    float[] a = toScreen(face.get(234), imgW, imgH, scale, offsetX, offsetY);
                    float[] b = toScreen(face.get(454), imgW, imgH, scale, offsetX, offsetY);
                    float[] leftPt = a[0] < b[0] ? a : b;
                    float[] rightPt = a[0] < b[0] ? b : a;
                    float faceWidth = Math.abs(rightPt[0] - leftPt[0]);

                    if ("earring".equals(product.category) && earLeft != null && earRight != null) {
                        float earringWidth = faceWidth * 0.16f;
                        float dropY = faceWidth * 0.08f;
                        float outX = faceWidth * 0.02f;
                        sprites.add(new OverlayView.Sprite(earLeft,
                                leftPt[0] - outX, leftPt[1] + dropY, earringWidth));
                        sprites.add(new OverlayView.Sprite(earRight,
                                rightPt[0] + outX, rightPt[1] + dropY, earringWidth));
                    }

                    if ("necklace".equals(product.category) && necklaceBmp != null) {
                        float[] chin = toScreen(face.get(152), imgW, imgH, scale, offsetX, offsetY);
                        float necklaceWidth = faceWidth * 1.1f;
                        float top = chin[1] + faceWidth * 0.20f;
                        sprites.add(new OverlayView.Sprite(necklaceBmp, chin[0], top, necklaceWidth));
                    }
                }
            }

            overlay.post(() -> overlay.setSprites(sprites));

        } catch (Exception e) {
            // skip this frame
        } finally {
            imageProxy.close();
        }
    }

    private float[] toScreen(NormalizedLandmark lm, float imgW, float imgH,
                             float scale, float offsetX, float offsetY) {
        // only the front camera preview is mirrored
        float nx = useFront ? 1f - lm.x() : lm.x();
        return new float[]{
                nx * imgW * scale + offsetX,
                lm.y() * imgH * scale + offsetY
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (analysisExecutor != null) analysisExecutor.shutdown();
        if (faceLandmarker != null) faceLandmarker.close();
        if (handLandmarker != null) handLandmarker.close();
    }
}