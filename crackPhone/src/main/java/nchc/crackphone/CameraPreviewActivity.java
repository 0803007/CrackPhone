package nchc.crackphone;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import java.io.IOException;
import java.util.List;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera.Size; 

public class CameraPreviewActivity extends Activity implements SurfaceHolder.Callback{
  
 Camera myCamera;
 SurfaceView previewSurfaceView;
 SurfaceHolder previewSurfaceHolder;
 boolean previewing = false;
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        
        //設定橫向
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 

         
        getWindow().setFormat(PixelFormat.UNKNOWN);      
        previewSurfaceView = (SurfaceView)findViewById(R.id.previewsurface);
        previewSurfaceHolder = previewSurfaceView.getHolder();
        previewSurfaceHolder.addCallback(this);
        previewSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
 
 @Override
 public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
  // TODO Auto-generated method stub
   
  if(previewing){
   myCamera.stopPreview();
   previewing = false;
  }
   
   
  // 取得相機參數 
  Camera.Parameters parameters = myCamera.getParameters(); 
  //自動對焦
  parameters.setFocusMode("auto");
  // 取得照片尺寸 
  List<Size> supportedPictureSizes = parameters.getSupportedPictureSizes(); 
  int sptw = supportedPictureSizes.get(supportedPictureSizes.size() - 1).width; 
  int spth = supportedPictureSizes.get(supportedPictureSizes.size() - 1).height; 
  // 取得預覽尺寸 
  List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes(); 
  int prvw = supportedPreviewSizes.get(0).width;  
  int prvh = supportedPreviewSizes.get(0).height; 
  parameters.setPictureFormat(PixelFormat.JPEG); 
  parameters.setPreviewSize(1280, 960); 
  myCamera.setParameters(parameters); 
  //myCamera.startPreview(); 

try {
	myCamera.setPreviewDisplay(arg0);
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

  myCamera.startPreview();
  previewing = true;
   
   
 }
 
 @Override
 public void surfaceCreated(SurfaceHolder arg0) {
  // TODO Auto-generated method stub
  myCamera = Camera.open();
 

//  if (Build.VERSION.SDK_INT >= 8) 
//
//	  myCamera.setDisplayOrientation(90); 
//
//  try { 
//
//	  myCamera.setPreviewDisplay(arg0); 
//  } catch (IOException exception) { 
//	  myCamera.release(); 
//
//	  myCamera = null; 
//  } 

 }
 
 @Override
 public void surfaceDestroyed(SurfaceHolder arg0) {
  // TODO Auto-generated method stub
  myCamera.stopPreview();
  myCamera.release();
  myCamera = null;
  previewing = false;
 }
}
