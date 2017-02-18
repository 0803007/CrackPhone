package nchc.crackphone;

import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc; 
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends Activity {
	private static final int
			ALBUM_OK  = 0,
			CUT_OK  = 1,
			CAMERA_OK  = 2;

	private Button mBtnSelFile;
	private Button mBtnCropFile;
	private Button mBtnCapImage;
	private Button mBtnPreview;
	private Button mBtnClose;
	private File tempFile;

	private ImageView  mImageView ;
	//private Handler nHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//maxMemory是每個app最大可使用的bytes數量, 除以1024*1024轉換為MB
		Runtime rt = Runtime.getRuntime();
		long maxMemory = rt.maxMemory();
		
		//Log.i("onCreate", "maxMemory:" + Long.toString(maxMemory/(1024*1024)));
		
		//設置橫放  
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  
		setContentView(R.layout.activity_main);
		setupViewComponent();

	}
    private void setupViewComponent(){
    	mBtnSelFile = (Button)findViewById(R.id.buttonSelFile);
		mBtnCropFile = (Button)findViewById(R.id.buttonCropFile);
    	mBtnCapImage = (Button)findViewById(R.id.buttonCapImage);
    	mBtnPreview = (Button)findViewById(R.id.buttonPreview);
    	mBtnClose = (Button)findViewById(R.id.buttonClose);
    	mImageView = (ImageView)findViewById(R.id.imageView1);
    	mBtnSelFile.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO 自動產生的方法 Stub
				Intent intent = new Intent(Intent.ACTION_PICK); 
				intent.setType("image/*");

				startActivityForResult(intent, ALBUM_OK);

			}});
		mBtnCropFile.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO 自動產生的方法 Stub
				Intent intent = new Intent(Intent.ACTION_PICK);
				//intent.setType("image/*");
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, CUT_OK);

			}});
    	mBtnCapImage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO 自動產生的方法 Stub
				/*
				Mat img;
				img =  Highgui.imread( Environment.getExternalStorageDirectory()+"/test2.jpg",1);
				int a = img.cols();
				a = a+1;
			    Bitmap bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
				Utils.matToBitmap(img, bmp);

				mImageView.setImageBitmap(bmp);*/
				
				Intent intent = new Intent(); //調用照相機  
				//intent.setAction("android.media.action.STILL_IMAGE_CAMERA");

				intent.setAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivity(intent);
			}});
    	mBtnPreview.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				   Intent intent = new Intent();

				   intent.setClass(MainActivity.this, CameraPreviewActivity.class);  //setClass(來源, 目的地)

				   startActivity(intent);
			}});
    	mBtnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO 自動產生的方法 Stub
				
			}});
    	//final ProgressBar proBar =(ProgressBar)findViewById(R.id.progressBar2);
//    	DoLengthWork work = new DoLengthWork();
//    	work.setHandler(nHandler);
//    	work.setProgressBar(proBar);
//    	work.start();
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
/*
		// 有選擇檔案
		if (resultCode == RESULT_OK) {
			String str = data.getDataString();

			Intent it = new Intent();
			it.setClass(MainActivity.this, ImageShowActivity.class);

			Bundle bundle = new Bundle();
			bundle.putString("DATA_STRING", str);
			it.putExtras(bundle);

			startActivity(it);
		}*/

		switch (requestCode) {
			case ALBUM_OK:
				// 有選擇檔案
				if (resultCode == RESULT_OK) {
					String str = data.getDataString();

					Intent it = new Intent();
					it.setClass(MainActivity.this, ImageShowActivity.class);

					Bundle bundle = new Bundle();
					bundle.putString("DATA_STRING", str);
					it.putExtras(bundle);

					startActivity(it);
				}
				break;
			case CUT_OK:
				if (data != null) {
					clipPhoto(data.getData());
				}
				break;

		}
	}

	public void clipPhoto(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("scale", false);
		/*intent.putExtra("aspectX", 4);
		intent.putExtra("aspectY", 3);
		intent.putExtra("outputX", 4000);
		intent.putExtra("outputY", 3000);*/
		intent.putExtra("return-data", false);

		startActivityForResult(intent, ALBUM_OK);
	}

	private Uri getTmpUri() {
		String IMAGE_FILE_DIR = Environment.getExternalStorageDirectory() + "/" + "app_name";
		File dir = new File(IMAGE_FILE_DIR);
		File file = new File(IMAGE_FILE_DIR, Long.toString(System.currentTimeMillis()));

		if (!dir.exists()) {
			dir.mkdirs();
		}
		return Uri.fromFile(file);
	}
}
