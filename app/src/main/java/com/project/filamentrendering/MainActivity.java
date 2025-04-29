package com.project.filamentrendering;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Choreographer;
import android.view.SurfaceView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.filament.Engine;
import com.google.android.filament.Filament;
import com.google.android.filament.IndirectLight;
import com.google.android.filament.Skybox;
import com.google.android.filament.android.UiHelper;
import com.google.android.filament.utils.Float3;
import com.google.android.filament.utils.Manipulator;
import com.google.android.filament.utils.ModelViewer;
import com.google.android.filament.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "MainActivity";
    SurfaceView surfaceView;
    Choreographer choreographer;
    ModelViewer modelViewer;
    Engine engine;
    UiHelper uiHelper;
    Manipulator manipulator;
    Choreographer.FrameCallback callback;
    private String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Enables edge to edge display of UI, hides system bars
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        surfaceView = findViewById(R.id.surfaceView);
        initFilament();
        initFields();
        initCallback();
        loadGlb("BusterDrone");
    }

    private void loadGlb(String name) {
        ByteBuffer buffer = readAsset("models/"+name+".gltf");
        modelViewer.loadModelGltf(buffer, s -> {
            uri = s;
            readAsset("models/" + s);
            return null;
        });
        Log.d(TAG,"::::"+"models/"+uri);
        modelViewer.transformToUnitCube(new Float3(1f,1f,1f));
    }

    private ByteBuffer readAsset(String assetName) {
        try {
            InputStream inputStream = getAssets().open(assetName);
            byte[] byteArr = new byte[inputStream.available()];
            inputStream.read(byteArr);
            Log.e(TAG,"::::"+byteArr.length);
            return ByteBuffer.wrap(byteArr);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initCallback() {
        callback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long l) {
                modelViewer.render(l);
                choreographer.postFrameCallback(this);
            }
        };
    }

    private void initFields() {
        engine = Engine.create();
        uiHelper = new UiHelper();
        manipulator = new Manipulator.Builder().viewport(surfaceView.getWidth(),surfaceView.getHeight()).build(Manipulator.Mode.ORBIT);
        modelViewer = new ModelViewer(surfaceView, engine, uiHelper, manipulator);
        choreographer = Choreographer.getInstance();
        surfaceView.setOnTouchListener(modelViewer);
        uiHelper.setOpaque(false);
        uiHelper.attachTo(surfaceView);
    }

    private void initFilament() {
        Filament.init();
        System.loadLibrary("filament-utils-jni");
        Log.d(TAG, "Filament Loaded");
    }

    @Override
    protected void onResume() {
        super.onResume();
        choreographer.postFrameCallback(callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        choreographer.removeFrameCallback(callback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        choreographer.removeFrameCallback(callback);
    }
}