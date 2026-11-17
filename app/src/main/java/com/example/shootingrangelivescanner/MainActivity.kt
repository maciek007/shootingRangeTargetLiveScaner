package com.example.shootingrangelivescanner

import android.hardware.camera2.CameraAccessException
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
import org.opencv.core.Mat
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
            val cannyImg : Mat = Mat.zeros(img.size(), img.type())
            Imgproc.Canny(img, cannyImg, 80.0, 90.0)

            return cannyImg
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