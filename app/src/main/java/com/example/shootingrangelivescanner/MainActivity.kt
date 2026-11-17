package com.example.shootingrangelivescanner

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shootingrangelivescanner.databinding.ActivityMainBinding
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class MainActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraView : CustomCameraView;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (OpenCVLoader.initLocal()) {
            Log.i("opencv_init", "OpenCV loaded successfully");
        } else {
            Log.e("opencv_init", "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = ActivityMainBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v,insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cameraView = binding.cameraView

        setContentView(binding.root)
        binding.zoom.addOnChangeListener { slider, value, fromUser ->
            cameraView.setZoom(value)
        }

        cameraView.setCvCameraViewListener(this)


        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()

    }

    /**
     * A native method that is implemented by the 'shootingrangelivescanner' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat? {
        if(inputFrame != null) {
            val img = inputFrame.rgba()
            val mat1 = Mat(img.size(), img.type())
            val mat2 = Mat(img.size(), img.type())
            Imgproc.GaussianBlur(img, mat1, Size(5.0,5.0), 0.5)
            Imgproc.Canny(mat1, mat2, 60.0, 100.0)
            Imgproc.morphologyEx(mat2, mat1, Imgproc.MORPH_CLOSE ,Mat(), Point(-1.0,-1.0),2 )
            thinning(mat1)
            return mat1
        }
        else return null
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onResume() {
        super.onResume()
        cameraView.enableView()
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase?> {
        return listOf(cameraView)
    }

    companion object {
        // Used to load the 'shootingrangelivescanner' library on application startup.
        init {
            System.loadLibrary("shootingrangelivescanner")
        }
    }
}


fun thinning(binaryImg: Mat) {
    val skel = Mat.zeros(binaryImg.size(), binaryImg.type())
    val temp = Mat()
    val eroded = Mat()
    val img = binaryImg.clone()
    Log.e("mat_type",img.type().toString())
    Log.e("mat_type",skel.type().toString())

    // Element strukturalny w kształcie krzyża 3x3
    val element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, Size(3.0, 3.0))

    try {
        while (true) {
            Imgproc.erode(img, eroded, element)
            Imgproc.dilate(eroded, temp, element) // Otwarcie morfologiczne
            Core.subtract(img, temp, temp)        // Różnica między oryginałem a otwarciem
            Core.bitwise_or(skel, temp, skel)     // Dodanie do wynikowego szkieletu
            eroded.copyTo(img)

            // Zakończ, gdy wszystkie białe piksele zostaną wyerodowane
            if (Core.countNonZero(img) == 0) {
                break
            }
        }
    } finally {
        // Zwolnienie pamięci natywnej C++ nawet w przypadku wyjątku
        temp.release()
        eroded.release()
        img.release()
        element.release()
    }
    skel.copyTo(binaryImg)
}