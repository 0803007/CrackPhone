package nchc.crackphone;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore.MediaColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;

import nchc.crackphone.SemiAutoExtraction;


public class CopyOfImageShowActivity extends Activity implements SensorEventListener {
	private static final int 
            MENU_AUTO_LASER = Menu.FIRST,
           	MENU_MANU_LASER = Menu.FIRST + 1,
           	MENU_GET_DLTIMG = Menu.FIRST + 2,
           	MENU_PROFILE = Menu.FIRST + 3,
           	MENU_SEMIAUTO = Menu.FIRST + 4,
         	MENU_OPENCLOSE_GRID = Menu.FIRST + 5,
         	MENU_ABOUT = Menu.FIRST + 6,
           	MENU_EXIT = Menu.FIRST + 7,
        	MENU_OPENCLOSE_SENSOR = Menu.FIRST + 8,
            MENU_INIT_AB = Menu.FIRST + 9,
            MENU_GET_AB = Menu.FIRST + 10,
            MENU_GET_THRESH = Menu.FIRST + 11;
           			
	//主畫面
	private TouchView iv;
	private Vibrator mVibrator;
	private TextView mLogText;	
	private TextView mPosText;
	private TextView mRotText;
	private TextView mAccText;
	private TextView mAngText;
	private SlidingDrawer mSlidingDrawer;
	private ProgressDialog mProgressDialog;
	private ImageButton mROILButton;
	private ImageButton mROIRButton;
	private ProgressBar mProgressBar;
	
	//隱藏的button
	private Button mTopButton;
	private Button mDownButton;
	private Button mRightButton;
	private Button mLeftButton;
	private Button mSaveButton;
	private Button mCenterButton;
	
	private Bitmap mBitmap = null;
	private Bitmap mBinaryBitmap = null;
	ArrayList <PointF> mLaserPointList = null;
	
	//右邊抽屜
	private EditText mAlphaText;
	private EditText mBetaText;
	private Button mSetAngleButton;
	private EditText mWText;
	private EditText mHText;
	private EditText mScaleText;
	private Button mSetWHScaleButton;
	
	//右邊抽屜
	private EditText mStdText;
	private EditText mNodeText;
	private EditText mNormalText;

	//上面抽屜
	private SeekBar mRSeekbar;
	private SeekBar mRRGBSeekbar;
	private SeekBar mGRGBSeekbar;
	private SeekBar mBRGBSeekbar;
	private SeekBar mAreaSeekbar;
	private EditText mRText;
	private EditText mRRGBText;
	private EditText mGRGBText;
	private EditText mBRGBText;
	private EditText mAreaText;
	
	//Dialog
	//private Spinner mAdaptiveSpinner;
	private Spinner mParamSpinner;
	private Spinner mBlockSizeSpinner;
	private Button mAdaptiveButton;
	private EditText mManualText;
	private SeekBar mManualSeekBar;
	private Button mManualButton;
	
	//Angle Data
    private int 				   count1 = 0;
    private int 				   count2 = 0;
    private int 				   count3 = 0;
    private double 				   tmp_aAngle = 0.0;  //wyc1,20140622
    private double 				   tmp_bAngle = 0.0;  //wyc1,20140622
    private TextView output;
    private boolean key1 = false; //完成getAngle後，key = true，並顯示"Alpha,Beta 寫入檔案成功"到TextView
    private boolean key2 = false;
    private boolean key3 = false;
    private boolean key4 = false;
    private boolean key5 = false;
    
    private SensorManager sensorManager;
    private String posA;  //wyc1,20140622
    private String posB;  //wyc1,20140622
    private String posC;  //wyc1,20140622
    private String posD;  //wyc1,20140622
    private String tmpString;  //wyc1,20140622
    private double 				   aAngle;  //wyc1,20140622
    private double 				   bAngle;  //wyc1,20140622
    private double 				   aInitAngle = 0.0;  
    private double 				   bInitAngle = 0.0;      
    private double 				   aDisplayAngle;  //wyc1,20140622
    private double 				   bDisplayAngle;  //wyc1,20140622
    private double 				   W = 139.0;  //mm
    private double 				   H = 82.0;  //mm
     
    private float 				   pointA_x = (float)0.0;
    private float 				   pointA_y = (float)0.0;
    private float 				   pointB_x = (float)0.0;
    private float 				   pointB_y = (float)0.0;
    private float 				   pointC_x = (float)0.0;
    private float 				   pointC_y = (float)0.0;
    private float 				   pointD_x = (float)0.0;
    private float 				   pointD_y = (float)0.0;
    private float                  pointA_u = (float)0.0;
    private float                  pointA_v = (float)0.0;
    private float                  pointB_u = (float)0.0;
    private float                  pointB_v = (float)0.0;
    private float                  pointC_u = (float)0.0;
    private float                  pointC_v = (float)0.0;
    private float                  pointD_u = (float)0.0;
    private float                  pointD_v = (float)0.0;
    
    //ortho-rectification
    private Mat                    mLaser;
    private Mat                    mLasertmp;
    private Mat                    imgR;
    private Mat                    imgG;
    private Mat                    imgB;
    private Mat                    mR;
    private Mat                    mG;
    private Mat                    mB;
    private Mat                    temp;
    private Mat                    temp1;
    private Mat                    temp2;
    private Mat                    temp3;
    private Mat                    temp4;
    private Mat                    epsilon;
    private Mat                    zero;
    private int 				   fMinArea;
    private Mat                    mAA;
    private Mat                    mXX;
    private Mat                    mBB;
    private Mat                    ortho_bf;
    private Mat                    ortho_af; 
    
    //NativePIV
    private int 				   PIVFile_count = 0;
    float[] LL={0,0,0,0,0,0,0,0};
	float mScale = (float)10.0;;
	
	//Sensor values
	float[] magneticFieldValues = new float[3]; 
	float[] accelerometerValues = new float[3]; 
	float[] gyroscopeValues = new float[3];
	float[] gyroscopeAngles = new float[3];
	
	//Update Rotation 資訊
	boolean bShowSensor = false;
    Handler mHandler;
    Timer updateTimer;
    TimerTask mClock;
	
    //other
	private SemiAutoExtraction mSemiAuto;
	private int nProgressStatus = 0;
	private float timestamp;
	private static final float NS2S = 1.0f / 1000000000.0f;
	private boolean bManualLaserPoint = false;
	private int nLaserPointIndex = 0;
	private Uri uri;
	private String ImagePath;
	private Dialog mThresholdDlg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		//設置橫放  
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.imageshow_activity);
		
		mThresholdDlg = new Dialog(this);
		//mThresholdDlg.setTitle("門檻值");
		mThresholdDlg.setContentView(R.layout.threshold_dlg);
		
		setupViewComponent();
	          
	    Bundle bundle = getIntent().getExtras();    
	    ImagePath = bundle.getString("DATA_STRING");
	    uri = Uri.parse(ImagePath);
	    //iv.setImageURI(uri);
	    ContentResolver cr = this.getContentResolver(); 

		//方法1
		BitmapFactory.Options opt16 = new BitmapFactory.Options();
		opt16.inPreferredConfig = Bitmap.Config.RGB_565;
		mBitmap = BitmapFactory.decodeFile(getFilePathFromContentUri(uri,cr),opt16).copy(Bitmap.Config.RGB_565, true);
		//mBitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));

		//方法2
		/*
		Mat m = Highgui.imread(getFilePathFromContentUri(uri,cr));
		mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(m, mBitmap);
		m.release();*/
		
		/*
		//消除內插
		Canvas canvas = new Canvas(mBitmap);
		Paint paint = new Paint();
		int w = mBitmap.getWidth();
		int h = mBitmap.getHeight();
		//  int w_2 = bitmap2.getWidth();
		//  int h_2 = bitmap2.getHeight();

		paint.setAntiAlias(false);
		paint.setDither(false);  
		paint.setFilterBitmap(false); 
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		*/
		
		//resize bitmap
		if (mBitmap.getWidth()>4096||mBitmap.getHeight()>4096)
		    mBitmap = reSizeBitmap(mBitmap);
		
		iv.mPosText = mPosText;
		iv.mImageWidth = mBitmap.getWidth();
		iv.mImageHeight = mBitmap.getHeight();
		//iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		iv.setImageBitmap(mBitmap);
		iv.setVibrator(mVibrator);	
		
		//Sensor part
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);  //wyc1,20140622
		//開啟Sensors
		List<Sensor> sensors_acc = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors_acc.size() > 0) 
			sensorManager.registerListener(this, sensors_acc.get(0), SensorManager.SENSOR_DELAY_NORMAL);	
		else
			mLogText.setText("No ACCELEROMETER");//呼叫SensorManager   //wyc1,20140622
		
		List<Sensor> sensors_mag = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if (sensors_mag.size() > 0)
			sensorManager.registerListener(this, sensors_mag.get(0), SensorManager.SENSOR_DELAY_NORMAL);
		else
			mLogText.setText("No MAGNETIC");//呼叫SensorManager   //wyc1,20140622
				
		List<Sensor> sensors_gyr = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
		if (sensors_gyr.size() > 0) 
			sensorManager.registerListener(this, sensors_gyr.get(0), SensorManager.SENSOR_DELAY_GAME);
		else
			mLogText.setText("No ORIENTATION");//呼叫SensorManager   //wyc1,20140622
		
        //ortho-rectification
        mAA = new Mat(8, 8, CvType.CV_32FC1, new Scalar(0));
        mXX = new Mat(8, 1, CvType.CV_32FC1, new Scalar(0));
        mBB = new Mat(8, 1, CvType.CV_32FC1, new Scalar(0));
        ortho_bf = new Mat(mBitmap.getHeight(), mBitmap.getWidth(), CvType.CV_8UC1, new Scalar(0));
        ortho_af = new Mat(mBitmap.getHeight(), mBitmap.getWidth(), CvType.CV_8UC1, new Scalar(0));
        
      /*
			try {
				saveExif(getFilePathFromContentUri(uri,cr));
			} catch (ImageReadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ImageWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       */
        //saveExif(ImagePath);
        
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (mBitmap!=null)
			mBitmap.recycle();
		
		if (mAA!=null)
			mAA.release();
		
		if (mXX!=null)
			mXX.release();
		
		if (mBB!=null)
			mBB.release();
		
		if (ortho_bf!=null)
			ortho_bf.release();
		
		if (ortho_af!=null)
			ortho_af.release();
		
		sensorManager.unregisterListener(this);
			
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//unregisterReceiver(updateUIReceive);
	}
	private void setupViewComponent(){
		//從資源類別R中取得介面元件
	    //ImageView iv = (ImageView)this.findViewById(R.id.imageView1);
	    iv = (TouchView)this.findViewById(R.id.touchView1);
	    mLogText = (TextView) findViewById(R.id.textView1);
	    mPosText = (TextView) findViewById(R.id.textView2);
	    mRotText = (TextView) findViewById(R.id.textView5);
	    mAccText = (TextView) findViewById(R.id.textView3);
	    mAngText  = (TextView) findViewById(R.id.textView6); 
	    //mSlidingDrawer = (SlidingDrawer) this.findViewById(R.id.slidingDrawer1);
	    mAlphaText = (EditText)findViewById(R.id.alphaEditText); 
	    mBetaText = (EditText)findViewById(R.id.betaEditText); 
	    mWText = (EditText)findViewById(R.id.WEditText); 
	    mHText = (EditText)findViewById(R.id.HEditText); 
	    mScaleText = (EditText)findViewById(R.id.ScaleEditText); 
	    
	    mStdText = (EditText)findViewById(R.id.stdEditText); 
	    mNodeText = (EditText)findViewById(R.id.nodeEditText); 
	    mNormalText = (EditText)findViewById(R.id.normalEditText); 	   
		mRSeekbar = (SeekBar)findViewById(R.id.RSeekBar);
		mRRGBSeekbar = (SeekBar)findViewById(R.id.RRGBSeekBar);
		mGRGBSeekbar = (SeekBar)findViewById(R.id.GRGBSeekBar);
		mBRGBSeekbar = (SeekBar)findViewById(R.id.BRGBseekBar);
		mAreaSeekbar = (SeekBar)findViewById(R.id.AREAseekBar); 
		mRText = (EditText)findViewById(R.id.REditText);
		mRRGBText = (EditText)findViewById(R.id.RRGBEditText);
		mGRGBText = (EditText)findViewById(R.id.GRGBEditText);
		mBRGBText = (EditText)findViewById(R.id.BRGBEditText);
		mAreaText = (EditText)findViewById(R.id.AreaEditText);
        mTopButton = (Button)findViewById(R.id.buttonPreview);
	    mDownButton = (Button)findViewById(R.id.button2);
		mRightButton = (Button)findViewById(R.id.button4);
		mLeftButton = (Button)findViewById(R.id.button3);
		mSaveButton = (Button)findViewById(R.id.button5);
		mCenterButton = (Button)findViewById(R.id.button6);
		mSetAngleButton = (Button)findViewById(R.id.button7);
		mSetWHScaleButton = (Button)findViewById(R.id.button8);
		mROILButton = (ImageButton)findViewById(R.id.imageButton1);
		mROIRButton = (ImageButton)findViewById(R.id.imageButton2);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
		
		//Dialog
		//mAdaptiveSpinner = (Spinner)mThresholdDlg.findViewById(R.id.spinnerAdaptive);
		mParamSpinner = (Spinner)mThresholdDlg.findViewById(R.id.spinnerParam);
		mBlockSizeSpinner = (Spinner)mThresholdDlg.findViewById(R.id.spinnerBlockSize);
		mAdaptiveButton = (Button)mThresholdDlg.findViewById(R.id.buttonAdaptive);
		mManualText = (EditText)mThresholdDlg.findViewById(R.id.editTextManual);
		mManualSeekBar = (SeekBar)mThresholdDlg.findViewById(R.id.seekBarManual);
		mManualButton = (Button)mThresholdDlg.findViewById(R.id.buttonManual);
		
		mVibrator = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);

		mRSeekbar.setOnSeekBarChangeListener(rseekBarOnChangeLis);
		mRRGBSeekbar.setOnSeekBarChangeListener(rrgbseekBarOnChangeLis);
		mGRGBSeekbar.setOnSeekBarChangeListener(grgbseekBarOnChangeLis);
		mBRGBSeekbar.setOnSeekBarChangeListener(brgbseekBarOnChangeLis);
		mAreaSeekbar.setOnSeekBarChangeListener(areaseekBarOnChangeLis);
		mManualSeekBar.setOnSeekBarChangeListener(manualseekBarOnChangeLis);
		

		mTopButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;
				
	       		int i = (int) mLaserPointList.get(nLaserPointIndex).x;
	       		int j = (int) mLaserPointList.get(nLaserPointIndex).y;
	       		mLaserPointList.set(nLaserPointIndex, new PointF(i,j-3));
	       		iv.postInvalidate();	
			}});
		mDownButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;
				
				int i = (int) mLaserPointList.get(nLaserPointIndex).x;
	       		int j = (int) mLaserPointList.get(nLaserPointIndex).y;
	       		mLaserPointList.set(nLaserPointIndex, new PointF(i,j+3));
	       		iv.postInvalidate();
			}});
		mRightButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;
				
				int i = (int) mLaserPointList.get(nLaserPointIndex).x;
	       		int j = (int) mLaserPointList.get(nLaserPointIndex).y;
	       		mLaserPointList.set(nLaserPointIndex, new PointF(i+3,j));
	       		iv.postInvalidate();
			}});
		mLeftButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;
				
				int i = (int) mLaserPointList.get(nLaserPointIndex).x;
	       		int j = (int) mLaserPointList.get(nLaserPointIndex).y;
	       		mLaserPointList.set(nLaserPointIndex, new PointF(i-3,j));
	       		iv.invalidate(); 
			}});
		mSaveButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;
				
				//得到DLT參數
		        getDLTParameter();
				//寫入紅點雷射影像位置
				saveLaserImagePos(mLaserPointList);
			}});
		mCenterButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLaserPointList==null)
					return;
				
				nLaserPointIndex = nLaserPointIndex + 1;
				if (nLaserPointIndex==4)
					nLaserPointIndex = 0;
				
				iv.nLaserPointIndex = nLaserPointIndex;
				mCenterButton.setText(Integer.toString(nLaserPointIndex+1));			
			}});
		mSetAngleButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				aAngle = Float.parseFloat(mAlphaText.getText().toString());
				bAngle = Float.parseFloat(mBetaText.getText().toString());
				
				//寫入角度標至檔案
				saveLaserAngle(aAngle,bAngle);				
			}});
		mSetWHScaleButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				W = Float.parseFloat(mWText.getText().toString());
				H = Float.parseFloat(mHText.getText().toString());
				mScale = Float.parseFloat(mScaleText.getText().toString());
				
				Toast.makeText(getApplicationContext(), "雷射寬高度比例修改成功", 1000).show();  
			}});
		mROILButton.setOnTouchListener(new OnTouchListener(){
                int[] temp = new int[]{0,0};

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
                    int eventAction = event.getAction();
                    //Log.i("", "OnTouchEvent"+eventAction);

                    int x = (int)event.getRawX();
                    int y = (int)event.getRawY();
                    int id_x = v.getLeft();
                    int id_y = v.getTop();
                    switch(eventAction){
                    case MotionEvent.ACTION_DOWN:
                            temp[0] = (int)event.getX();
                            temp[1] = y - v.getTop();
                            id_x = v.getLeft();
                            id_y = v.getTop();
                            if(y > v.getTop()&& y < v.getTop()+100&&x>v.getLeft()&&x<v.getLeft()+100)
                            {
                            	mROILButton.setVisibility(View.INVISIBLE); 
                            }
                            break;

                    case MotionEvent.ACTION_MOVE:
                            id_x = v.getLeft();
                            id_y = v.getTop();
                            v.layout(x - temp[0], y - temp[1], x + mROILButton.getWidth()- temp[0], y - temp[1] +  mROILButton.getHeight());

                            v.postInvalidate();
                    case MotionEvent.ACTION_UP:
                            break;
                    }
					return false;
				}
        });
		mROIRButton.setOnTouchListener(new OnTouchListener(){
            int[] temp = new int[]{0,0};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
                int eventAction = event.getAction();
                //Log.i("", "OnTouchEvent"+eventAction);

                int x = (int)event.getRawX();
                int y = (int)event.getRawY();
                int id_x = v.getLeft();
                int id_y = v.getTop();
                switch(eventAction){
                case MotionEvent.ACTION_DOWN:
                        temp[0] = (int)event.getX();
                        temp[1] = y - v.getTop();
                        id_x = v.getLeft();
                        id_y = v.getTop();
                        if(y > v.getTop()&& y < v.getTop()+100&&x>v.getLeft()&&x<v.getLeft()+100)
                        {
                        	mROIRButton.setVisibility(View.INVISIBLE); 
                        }
                        break;

                case MotionEvent.ACTION_MOVE:
                        id_x = v.getLeft();
                        id_y = v.getTop();
                        v.layout(x - temp[0], y - temp[1], x + mROIRButton.getWidth()- temp[0], y - temp[1] +  mROIRButton.getHeight());

                        v.postInvalidate();
                case MotionEvent.ACTION_UP:
                        break;
                }
				return false;
			}

		});
		mManualButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		    	//顯示進度bar
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.VISIBLE);
				mThresholdDlg.cancel(); 
				
				new Thread() { 
					@Override  
					public void run() { 
						int width = mBitmap.getWidth();
				    	int height = mBitmap.getHeight();
				    	int threshold = Integer.parseInt(mRText.getText().toString());
				    	if(mBinaryBitmap!=null)
				    		mBinaryBitmap.recycle();
				    	mBinaryBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
					    for(int x = 0; x < width; ++x) {
					        for(int y = 0; y < height; ++y) {
					        	int pixel = mBitmap.getPixel(x, y);
					            int red = Color.red(pixel);

					            //get binary value
					            if(red < threshold){
					            	mBinaryBitmap.setPixel(x, y, 0xFF000000);
					            } else{
					            	mBinaryBitmap.setPixel(x, y, 0xFFFFFFFF);
					            }
					        }
					    }
					    runOnUiThread(new Runnable() {
                            public void run() {
                        		iv.setImageBitmap(mBinaryBitmap);
                        		iv.invalidate();
                            	mProgressBar.setVisibility(View.GONE);      	
                            }
                       });
					} 
				}.start();  

			}});
		mAdaptiveButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}});
	}
	SeekBar.OnSeekBarChangeListener manualseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			mManualText.setText(Integer.toString(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}
		
	};
	SeekBar.OnSeekBarChangeListener rseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			mRText.setText(Integer.toString(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}
		
	};

	SeekBar.OnSeekBarChangeListener rrgbseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			double pos = (double)progress / 100;
			DecimalFormat df = new DecimalFormat("#.##");
			mRRGBText.setText(df.format(pos));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}
		
	};
	SeekBar.OnSeekBarChangeListener grgbseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			double pos = (double)progress / 100;
			DecimalFormat df = new DecimalFormat("#.##");
			mGRGBText.setText(df.format(pos));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub	
		}
		
	};
	SeekBar.OnSeekBarChangeListener brgbseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			double pos = (double)progress / 100;
			DecimalFormat df = new DecimalFormat("#.##");
			mBRGBText.setText(df.format(pos));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub	
		}
		
	};
	SeekBar.OnSeekBarChangeListener areaseekBarOnChangeLis = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			//int pos = progress
			mAreaText.setText(Integer.toString(progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
	};

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		//張博的方法
		/*
		//aAngle = -(Math.toDegrees(Math.acos(arg0.values[0]/Math.pow(Math.pow(arg0.values[0],2)+Math.pow(arg0.values[1],2)+Math.pow(arg0.values[2],2), 0.5))) - 90);
	    //bAngle = -(Math.toDegrees(Math.acos(arg0.values[1]/Math.pow(Math.pow(arg0.values[0],2)+Math.pow(arg0.values[1],2)+Math.pow(arg0.values[2],2), 0.5))) - 90);
		bAngle = (Math.toDegrees(Math.asin(arg0.values[1]/Math.pow(Math.pow(arg0.values[0],2)+Math.pow(arg0.values[1],2)+Math.pow(arg0.values[2],2), 0.5))));
		aAngle = -(Math.toDegrees(Math.asin(arg0.values[0]/Math.cos(bAngle/180*3.1415926)/Math.pow(Math.pow(arg0.values[0],2)+Math.pow(arg0.values[1],2)+Math.pow(arg0.values[2],2), 0.5))));
		
		//水平放手機與雷射投射器時(寬矩形)，角度需修正為
		//double tmp=0.0;
		//tmp=bAngle;
		//bAngle=aAngle;
		//aAngle=-tmp;	
		//****修正流速角度至裂縫的角度(直放)*****
	    //bAngle = 90 - bAngle;
		//aAngle = -aAngle;
		//****修正流速角度至裂縫的角度(橫放)*****
		double tmp=0.0;
		tmp=bAngle;
		bAngle=aAngle;
		aAngle=-tmp;
		bAngle = 90 + bAngle;
			
		aDisplayAngle = (Math.round(aAngle * 10000)) / 10000.0;
		bDisplayAngle = (Math.round(bAngle * 10000)) / 10000.0;*/
		
		//網路找到的方法
		/*
		float x = arg0.values[0];
		float y = arg0.values[1];
		float z = arg0.values[2];
		
		float dz = (float) (Math.atan(Math.sqrt(x*x+y*y)/z) *180 /3.1415926);
		float dy = (float) (Math.atan(x/Math.sqrt(y*y+z*z)) *180 /3.1415926);
		float dx = (float) (Math.atan(y/Math.sqrt(x*x+z*z)) *180 /3.1415926);
		
		bAngle = dy;
		aAngle = dx;*/
		
        //android內建的方法	
		float[] R = new float[9]; 
		float[] values = new float[3]; 
		
	    if (arg0.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 
	    	magneticFieldValues = arg0.values;     
	    if (arg0.sensor.getType() == Sensor.TYPE_ACCELEROMETER)       
	    	accelerometerValues = arg0.values;  
		if (arg0.sensor.getType() == Sensor.TYPE_GYROSCOPE )   
		{
			//gyroscopeValues = arg0.values;
			if (timestamp != 0) {  
				final float dT = (arg0.timestamp - timestamp) * NS2S;  
				gyroscopeValues[0] += arg0.values[0] * dT;  
				gyroscopeValues[1] += arg0.values[1] * dT;  
				gyroscopeValues[2] += arg0.values[2] * dT;  
				gyroscopeAngles[0] = (float) Math.toDegrees(gyroscopeValues[0]);
				gyroscopeAngles[1] = (float) Math.toDegrees(gyroscopeValues[1]);
				gyroscopeAngles[2] = (float) Math.toDegrees(gyroscopeValues[2]);
			}  
			timestamp = arg0.timestamp;  

		}
		/*SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues); 
		SensorManager.getOrientation(R, values); 
		values[0] = (float) Math.toDegrees(values[0]);  
		values[1] = (float) Math.toDegrees(values[1]);  
		values[2] = (float) Math.toDegrees(values[2]);*/
		values[0] = gyroscopeAngles[0];
		values[1] = gyroscopeAngles[1];
		values[2] = gyroscopeAngles[2];
			
		aAngle = -values[0];
		bAngle = -values[1];
		
		/*if (accelerometerValues[1] < 0)        //手機面朝下  
		    bAngle = 180 - Math.abs(values[2]); //手機面朝下無法偵測,須靠加速度器的z軸判斷 
		else
			bAngle = Math.abs(values[2]);  */    
	}
	private void getAngle(){
		//mRgba = inputFrame.rgba();

		// Log.i("!!!!!!!!!!!!!!!!", Double.toString(aDisplayAngle));

		if ( count1 > 10){   //累加
			tmp_aAngle=tmp_aAngle+aAngle;
			tmp_bAngle=tmp_bAngle+bAngle; 
		}

		if ( count1 == 30){
			tmp_aAngle=tmp_aAngle/20.;    //取30-10筆之平均
			tmp_bAngle=tmp_bAngle/20.;
			aAngle=tmp_aAngle;
			bAngle=tmp_bAngle;			
			posA = "A(0,0)"+"\r\n"; //換行;
			posB = "B(" + Double.toString(Math.round(W/Math.cos(Math.toRadians(aAngle))*10000)/10000.0) + "," + Double.toString(Math.round                                        ( W * Math.tan(Math.toRadians(bAngle)) * Math.tan(Math.toRadians(aAngle))*10000)/10000.0) + ")"+"\r\n"; //換行;
			posC = "C(" + Double.toString(Math.round(W/Math.cos(Math.toRadians(aAngle))*10000)/10000.0) + "," + Double.toString(Math.round(   (H/Math.cos(Math.toRadians(bAngle)) + W * Math.tan(Math.toRadians(bAngle)) * Math.tan(Math.toRadians(aAngle)))*10000  )/10000.0)+")"+"\r\n"; //換行;
			posD = "D(0," + Double.toString(Math.round(H/Math.cos(Math.toRadians(bAngle))*10000)/10000.0) + ")"+"\r\n"; //換行;
			
			key1=false;
		} //if ( count1 == 30)

		count1=count1+1; 
	}
	private void getDLTParameter() {
		//	讀取雷射投射角度alpha,beta
		//一行一行讀取檔案
	    double read_aAngle = 0.0;  
	    double read_bAngle = 0.0; 
	    /*
	    //掃描檔案  得到alpha beta
		try {
			String path = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";
			File InFile1 = new File(path,"LaserANGLE.dat");
			Scanner sc = new Scanner(InFile1);
			String input = sc.nextLine();
			read_aAngle=Double.parseDouble(input);
			//System.out.println(input);
			String input1 = sc.nextLine();
			read_bAngle=Double.parseDouble(input1);
			//System.out.println(input1);
			sc.close();	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    //掃描exif 得到alpha beta
	    /*
		try {
		    TiffImageMetadata exif;
		    IImageMetadata meta = Sanselan.getMetadata(new File((getFilePathFromContentUri(uri,this.getContentResolver()))));
		    if (meta instanceof JpegImageMetadata) {
		        exif = ((JpegImageMetadata)meta).getExif();
		    } else if (meta instanceof TiffImageMetadata) {
		        exif = (TiffImageMetadata)meta;
		    } else {
		        return;
		    }
		    TiffOutputSet outputSet = exif.getOutputSet();
		    TiffOutputDirectory exifDir = outputSet.getOrCreateExifDirectory();
		    exifDir.getFields()
		} catch (ImageReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImageWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    try {				
	    	File file = new File(getFilePathFromContentUri(uri,this.getContentResolver()));
	    	boolean abc = file.exists();
	    	IImageMetadata metadata;
	    	metadata = Sanselan.getMetadata(file);
	    	JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
	    	TiffField field = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_USER_COMMENT);
	    	if (field != null){
	        	String str = field.getValueDescription();
	        	str = str.replaceAll("'", "");    //去除單引號
	        	String[] split = str.split(";");  //alpha beta分離
	        	if (split.length == 2)
	        	{
	        		read_aAngle = Double.parseDouble(split[0]);
	        		read_bAngle = Double.parseDouble(split[1]);
	    		}
	    	}
	    } catch (ImageReadException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    }  
	    Toast.makeText(this, "Alpha=" +read_aAngle+",Beta=" +read_bAngle , 1000).show(); 
	    

		//計算ABCD四點在空間上的平面座標
		pointA_x =(float) 0.0;
		pointA_y =(float) 0.0;
		pointB_x =(float) (W/Math.cos(Math.toRadians(read_aAngle)));
		pointB_y =(float) (W * Math.tan(Math.toRadians(read_aAngle)) * Math.tan(Math.toRadians(read_bAngle)));
		pointC_x =(float) (W/Math.cos(Math.toRadians(read_aAngle)));
		pointC_y =(float) (H/Math.cos(Math.toRadians(read_bAngle))+W * Math.tan(Math.toRadians(read_aAngle)) * Math.tan(Math.toRadians(read_bAngle)));
		pointD_x =(float) 0.0;
		pointD_y =(float) (H/Math.cos(Math.toRadians(read_bAngle)));
		
		//如果四點都找到
        //ABCD在實體影像的座標  
		if (mLaserPointList.size()==4)
		{
			pointA_u = mLaserPointList.get(0).x;
			pointA_v = mLaserPointList.get(0).y;
			pointB_u = mLaserPointList.get(1).x;
			pointB_v = mLaserPointList.get(1).y;
			pointC_u = mLaserPointList.get(2).x;
			pointC_v = mLaserPointList.get(2).y;
			pointD_u = mLaserPointList.get(3).x;
			pointD_v = mLaserPointList.get(3).y;
		

			//求解2D-DLT的係數
			float[] b={pointA_u,pointB_u,pointC_u,pointD_u,pointA_v,pointB_v,pointC_v,pointD_v};
			mBB.put(0, 0, b);
			float[] a={pointA_x,pointA_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointA_u*pointA_x,-pointA_u*pointA_y,
					pointB_x,pointB_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointB_u*pointB_x,-pointB_u*pointB_y,
					pointC_x,pointC_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointC_u*pointC_x,-pointC_u*pointC_y,
					pointD_x,pointD_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointD_u*pointD_x,-pointD_u*pointD_y,
					(float)0.0,(float)0.0,(float)0.0,pointA_x,pointA_y,(float)1.0,-pointA_v*pointA_x,-pointA_v*pointA_y,
				(float)0.0,(float)0.0,(float)0.0,pointB_x,pointB_y,(float)1.0,-pointB_v*pointB_x,-pointB_v*pointB_y,
				(float)0.0,(float)0.0,(float)0.0,pointC_x,pointC_y,(float)1.0,-pointC_v*pointC_x,-pointC_v*pointC_y,
				(float)0.0,(float)0.0,(float)0.0,pointD_x,pointD_y,(float)1.0,-pointD_v*pointD_x,-pointD_v*pointD_y};

			mAA.put(0, 0, a);
			Core.solve(mAA, mBB, mXX);
			float[] x={0,0,0,0,0,0,0,0};
			mXX.get(0, 0, x);
			System.out.println(Double.toString(x[0]));
			System.out.println(Double.toString(x[1]));
			System.out.println(Double.toString(x[2]));
			System.out.println(Double.toString(x[3]));
			System.out.println(Double.toString(x[4]));
			System.out.println(Double.toString(x[5]));
			System.out.println(Double.toString(x[6]));
			System.out.println(Double.toString(x[7]));

			//DLT寫入檔案
			saveDLTParameter(x);
			
			key2=false;    
		}else
		{
			Toast.makeText(this, "DLT參數沒有成功", 1000).show(); 			
		}
		
	}
	private void getDLTImage(){           
       	//讀取DLT係數，進行正交轉換
		//讀取原始影像
		Utils.bitmapToMat(mBitmap, ortho_bf);
            //一行一行讀取檔案
			try {
				String path = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";
       		    File InFile1 = new File(path,"DLT_Coefficients.dat");
       		   // Log.i("!!!!!!!!!!!!!!!!!!!", path);
       		    Scanner sc = new Scanner(InFile1);
				String input0 = sc.nextLine();
				LL[0]=(float)Double.parseDouble(input0);
				String input1 = sc.nextLine();
				LL[1]=(float)Double.parseDouble(input1);
				String input2 = sc.nextLine();
				LL[2]=(float)Double.parseDouble(input2);
				String input3 = sc.nextLine();
				LL[3]=(float)Double.parseDouble(input3);
				String input4 = sc.nextLine();
				LL[4]=(float)Double.parseDouble(input4);
				String input5 = sc.nextLine();
				LL[5]=(float)Double.parseDouble(input5);
				String input6 = sc.nextLine();
				LL[6]=(float)Double.parseDouble(input6);
				String input7 = sc.nextLine();
				LL[7]=(float)Double.parseDouble(input7);
				sc.close();	
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			//顯示進度bar
			nProgressStatus = 0;                                        //進度bar的狀態
			mProgressDialog = new ProgressDialog(CopyOfImageShowActivity.this);                    //產生新的bar  
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//進度調的風格  
			mProgressDialog.setTitle("處理中");
			mProgressDialog.setProgress(0);
			mProgressDialog.setMax(100);
			mProgressDialog.show();  
			new Thread() { 
				@Override  
				public void run() {  
					//mapping 實際的座標到i=ortho_bf.cols(),j=ortho_bf.rows(),即1mm=1pixel=1個i或j
					//但這樣影像太小，所以令1個i或j=1/scale mm (影像放大三倍)，且平移(400,200)pixel
					//這樣就能將轉換後的影像容納於(1280*760)影像中
					//long startTime = System.currentTimeMillis();
					Mat bf_gray = new Mat();
					Imgproc.cvtColor(ortho_bf,bf_gray,Imgproc.COLOR_BGRA2GRAY, 1);
					//int bf_widthstep = (bf_gray.width()+3)/4*4;
					//int af_widthstep = (ortho_af.width()+3)/4*4;
					int bf_widthstep = bf_gray.width()* bf_gray.channels();
					int af_widthstep = ortho_af.width()* ortho_af.channels();
					float xx,yy;
					int uu,vv;
					float shift_x=1000, shift_y=1000;
					//float scale=(float)0.5;
			        int bf_size = (int) (bf_gray.total() * bf_gray.channels());
			        int af_size = (int) (ortho_af.total() * ortho_af.channels());
			        byte[] af_buff = new byte[af_size];
			        byte[] bf_buff = new byte[bf_size];
			        bf_gray.get(0, 0, bf_buff);
			        
		 	        for (int j=0;j<ortho_bf.rows();j++) { 

		 	        	for (int i=0;i<ortho_bf.cols();i++) {
			        		xx = ((float)i-shift_x)/mScale;
		 	        		yy = ((float)(ortho_bf.rows()-j)-shift_y)/mScale;
		 	        	    uu = (int) ((LL[0]*xx+LL[1]*yy+LL[2])/(LL[6]*xx+LL[7]*yy+1));
		           		    vv = (int) ((LL[3]*xx+LL[4]*yy+LL[5])/(LL[6]*xx+LL[7]*yy+1));
		          	        if (uu>=0 && uu<ortho_bf.cols() && vv>=0 && vv<ortho_bf.rows()){
		          	        	//double[] data = ortho_bf.get(Math.round(vv), Math.round(uu));	
		          	        	//ortho_af.put(j, i, data);
			            		af_buff[i + j*af_widthstep] = (byte) bf_buff[uu + vv*bf_widthstep];
		          		    }
			        	}
		 	        	//---回傳進度條----
		 	        	nProgressStatus = (int) (((j+1)*100)/(double)ortho_bf.rows());//處理進度bar
		 	        	Message msg = new Message();
		 	        	msg.what = nProgressStatus;
		 	        	handler.sendMessage(msg);
		 	        	//-------------
			        }
		 	        /*
			        //改變紅點的位置 
			        for (int k = 0;k<mLaserPointList.size();k++)
			        {
			       		int i = (int) mLaserPointList.get(k).x;
			       		int j = (int) mLaserPointList.get(k).y;
			        	xx = ((float)i-shift_x)/mScale;
	 	        		yy = ((float)(ortho_bf.rows()-j)-shift_y)/mScale;
	 	        	    uu = (int) ((LL[0]*xx+LL[1]*yy+LL[2])/(LL[6]*xx+LL[7]*yy+1));
	           		    vv = (int) ((LL[3]*xx+LL[4]*yy+LL[5])/(LL[6]*xx+LL[7]*yy+1));
	          	        if (uu>=0 && uu<ortho_bf.cols() && vv>=0 && vv<ortho_bf.rows())
	          	        	mDLTLaserPointList.add(new PointF(uu,vv));
	          	        else
	          	        	mDLTLaserPointList.add(new PointF(0,0));	
	          	      
			        }
			        iv.vecDLTLaserPointList = mDLTLaserPointList;*/
			        
			        ortho_af.put(0, 0, af_buff); 			             	       			        
			        bf_gray.release();  
			        mLaserPointList.clear();
			        
			        //long endTime = System.currentTimeMillis();
			        //long totTime = endTime - startTime;
			        //mLogText.setText("time:"+Long.toString(totTime));
			        
				    //Mat轉換成Bitmap到iv中,須放入handler中
					Utils.matToBitmap(ortho_af, mBitmap);
					Message msg = new Message();
                    msg.what = -1;
	 	        	handler.sendMessage(msg);
				} 
			}.start();  
			
	   //iv.setImageBitmap(mBitmap);	
		   
		   
	}
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
            //處理thread中,會碰到UI的部分 
            if (msg.what == -1)
            {
            	iv.setImageBitmap(mBitmap);	
            	mProgressDialog.cancel();    
            }
            else
				mProgressDialog.setProgress(msg.what);	
		}
	};
	
	private void getLaserDetectionPoint() {
		//讀照片
		Mat img = new Mat();
		Utils.bitmapToMat(mBitmap, img);
		mLaserPointList = new ArrayList ();
		//宣告影像變數
		int w,h;
		w = img.width();
		h = img.height();
		Mat imgR8u = null;
		Mat imgG8u = null;
		Mat imgB8u = null;	
		Mat temp8u =  new Mat(h, w, CvType.CV_8UC1);
		
		Mat imgR =  new Mat(h, w, CvType.CV_32FC1);
		Mat imgG =  new Mat(h, w, CvType.CV_32FC1);
		Mat imgB =  new Mat(h, w, CvType.CV_32FC1);
		Mat temp =  new Mat(h, w, CvType.CV_32FC1);
		Mat temp1 =  new Mat(h, w, CvType.CV_32FC1);	
		Mat temp2 =  new Mat(h, w, CvType.CV_32FC1);
		Mat temp3 =  new Mat(h, w, CvType.CV_32FC1);
		Mat temp4 =  new Mat(h, w, CvType.CV_32FC1);

		//通道分離
		List<Mat> listImg = new ArrayList<Mat>(3);
		Core.split(img, listImg); 
		imgR8u = listImg.get(1);  //R->0 G->1 G ->2,R跟G對調為綠點偵測
		imgG8u = listImg.get(0); 
		imgB8u = listImg.get(2);
    	//8U->32F
    	//Core.convertScaleAbs(imgR8u, imgR);
    	//Core.convertScaleAbs(imgG8u, imgG);
    	//Core.convertScaleAbs(imgB8u, imgB);
		imgR8u.convertTo(imgR, CvType.CV_32FC1);
		imgG8u.convertTo(imgG, CvType.CV_32FC1);
		imgB8u.convertTo(imgB, CvType.CV_32FC1);
		
    	//取介面上的參數
        float fR, fRRGB, fGRGB, fBRGB, fMinArea;
        fR = Float.parseFloat(mRText.getText().toString()); 
        fRRGB = Float.parseFloat(mRRGBText.getText().toString());
        fGRGB = Float.parseFloat(mGRGBText.getText().toString());
        fBRGB = Float.parseFloat(mBRGBText.getText().toString());
        fMinArea = Float.parseFloat(mAreaText.getText().toString());
                
        //取門檻1
        //Imgproc.threshold(imgR,temp1,fR,255,Imgproc.THRESH_BINARY_INV); //方法1
        int size = (int) (imgR.total() * imgR.channels());                //方法2
        float[] buff = new float[size];
        imgR.get(0, 0, buff);
        for (int i = 0; i < size; i++)
        	if (buff[i] > fR)
        		buff[i] = (float) 255;
        	else
        		buff[i] = (float) 0;
        	//buff[i] = (byte) (255 - buff[i]);
        temp1.put(0, 0, buff);
        //取門檻2
        Core.add(imgR,imgG,temp);
        Core.add(temp,imgB,temp);
        Core.add(temp, new Scalar(1,1,1), temp);
        Core.divide(imgR,temp,temp2);
        Imgproc.threshold(temp2, temp2,fRRGB,255,Imgproc.THRESH_BINARY);
        //取門檻3
        Core.divide(imgG,temp,temp3);
        Imgproc.threshold(temp3,temp3,fGRGB,255,Imgproc.THRESH_BINARY_INV);
        //取門檻4
        Core.divide(imgB,temp,temp4);
        Imgproc.threshold(temp4,temp4,fBRGB,255,Imgproc.THRESH_BINARY_INV);
        //四個門檻做and交集
        Core.bitwise_and(temp1,temp2,temp2);
        Core.bitwise_and(temp2,temp3,temp3);
        Core.bitwise_and(temp3,temp4,temp);
        //轉換影像至8u
        temp.convertTo(temp8u, CvType.CV_8UC1);
        
        /*
        //取灰階門檻
        Mat gray = new Mat();
		Imgproc.cvtColor(img,gray,Imgproc.COLOR_BGRA2GRAY, 1);
        Imgproc.threshold(gray,temp8u,230,255,Imgproc.THRESH_BINARY); //方法1,針對綠點
        */
        
        /*
        //   debug用
        Bitmap bitmap = Bitmap.createBitmap(temp8u.cols(), temp8u.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(temp8u, bitmap);
        iv.setImageBitmap(bitmap);
        */ 
        
        /*
        //Image inverse 超慢
        for (int i=0;i<temp.rows();i++) { 
        	for (int j=0;j<temp.cols();j++) { 
        		double[] data = temp.get(i, j); 
        		data[0] = (255 - data[0]);
        		temp.put(i, j, data); 
        	} 
        }*/
        /*
        //Image inverse 比較快的方法
        int size = (int) (temp.total() * temp.channels());
        byte[] buff = new byte[size];
        temp.get(0, 0, buff);
        for (int i = 0; i < size; i++)
        	buff[i] = (byte) (255- buff[i]);
        temp.put(0, 0, buff); */    	
            
        //找出所有輪廓中心
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();    
        Imgproc.findContours(temp8u, contours, new Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
        for (int i=0; i< contours.size();i++)
        	if (Imgproc.contourArea(contours.get(i)) > fMinArea )
        	{
        		Rect rect = Imgproc.boundingRect(contours.get(i));
        		PointF point = new PointF(rect.x + rect.width/2, rect.y + rect.height/2);
        		mLaserPointList.add(point);    
        	}
        //release記憶體 
        contours.clear();
        imgR8u.release();
        imgG8u.release();
        imgB8u.release();
        temp8u.release();
        imgR.release();
        imgG.release();
        imgB.release();
        temp.release();
        temp1.release();
        temp2.release();
        temp3.release();
        temp4.release();
               
        //排列ABCD四點位置
        //getSortLaserPostion(mLaserPointList);
        getSortLaserPostionForSort(mLaserPointList);
        
        //得到DLT參數
        getDLTParameter();
        
        //座標丟到iv上
        iv.vecLaserPointList = mLaserPointList;
        
        //更新畫面
        iv.invalidate();    
	}
	private void getSortLaserPostionForSort(ArrayList<PointF> list){
        if (list.size() != 4)
            return;
        
        PointF pointA = new PointF();
        PointF pointB = new PointF();
        PointF pointC = new PointF();
        PointF pointD = new PointF();
        //最左下角的是A點
        //先sort x的到AD兩點,在判斷Y取得AD,另外的兩點BC透過Y取得
        Collections.sort(list, new SortByX());
        if (list.get(0).y > list.get(1).y)
        {
        	pointA = list.get(0);
        	pointD = list.get(1);
        }
        else
        {
        	pointA = list.get(1);
        	pointD = list.get(0);
        }
        if (list.get(2).y > list.get(3).y)
        {
        	pointB = list.get(2);
        	pointC = list.get(3);
        }
        else
        {
        	pointB = list.get(3);
        	pointC = list.get(2);
        }
		list.clear();
		list.add(pointA);
		list.add(pointB);
		list.add(pointC);
		list.add(pointD);

	}
	class SortByX  implements Comparator<PointF> {

		public int compare(final PointF a, final PointF b) {
			if (a.x < b.x) {
				return -1;
			}
			else if (a.x > b.x) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}
	class SortByY  implements Comparator<PointF> {

		public int compare(final PointF a, final PointF b) {
			if (a.y < b.y) {
				return -1;
			}
			else if (a.y > b.y) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}
	
	private void getSortLaserPostion(ArrayList<PointF> list){
        if (list.size() != 4)
            return;
        
		//計算A B C D四點的位置
		PointF pointA = new PointF(); 
		float tmp_u=0;
		float tmp_v=0;
		float tmp1_u=0;
		float tmp1_v=0;
		float tmp2_u=0;
		float tmp2_v=0;
		float tmp3_u=0;
		float tmp3_v=0;
		float tmp4_u=0;
		float tmp4_v=0;
/*
		float pointA_u=0;
		float pointA_v=0;
		float pointB_u=0;
		float pointB_v=0;
		float pointC_u=0;
		float pointC_v=0;
		float pointD_u=0;
		float pointD_v=0;*/

		float length_12=0;
		float length_13=0;
		float length_14=0;

		float x1=0;
		float x2=0;
		float y1=0;
		float y2=0;

		pointA=(PointF) mLaserPointList.get(0); //要用(PointF)將object轉為point的格式
		tmp1_u=pointA.x;
		tmp1_v=pointA.y;
		pointA=(PointF) mLaserPointList.get(1);
		tmp2_u=pointA.x;
		tmp2_v=pointA.y;
		pointA=(PointF) mLaserPointList.get(2);
		tmp3_u=pointA.x;
		tmp3_v=pointA.y;
		pointA=(PointF) mLaserPointList.get(3);
		tmp4_u=pointA.x;
		tmp4_v=pointA.y;


		//先將最左邊的點存到tmp1_u,tmp1_v
		if (tmp2_u <= tmp1_u) {
			tmp_u=tmp1_u;
			tmp_v=tmp1_v;
			tmp1_u=tmp2_u;
			tmp1_v=tmp2_v;
			tmp2_u=tmp_u;
			tmp2_v=tmp_v;
		}
		if (tmp3_u <= tmp1_u) {
			tmp_u=tmp1_u;
			tmp_v=tmp1_v;
			tmp1_u=tmp3_u;
			tmp1_v=tmp3_v;
			tmp3_u=tmp_u;
			tmp3_v=tmp_v;
		}
		if (tmp4_u <= tmp1_u) {
			tmp_u=tmp1_u;
			tmp_v=tmp1_v;
			tmp1_u=tmp4_u;
			tmp1_v=tmp4_v;
			tmp4_u=tmp_u;
			tmp4_v=tmp_v;
		}


		//tmp1為最左邊的點
		x1=tmp1_u; //對角線的左邊點
		y1=tmp1_v;
		length_12=(tmp1_u-tmp2_u)*(tmp1_u-tmp2_u); //1-2線段
		length_13=(tmp1_u-tmp3_u)*(tmp1_u-tmp3_u); //1-3線段
		length_14=(tmp1_u-tmp4_u)*(tmp1_u-tmp4_u); //1-4線段
		if (length_12 >= length_13 && length_12 >= length_14) {  //1-2線段最長
			x2=tmp2_u; //對角線的右邊點
			y2=tmp2_v;
			if (tmp3_v < (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_13 > length_14){  //3在對角線上面    且    1-3線段>1-4線段
				pointD_u=tmp1_u; //最左點
				pointD_v=tmp1_v;
				pointB_u=tmp2_u; //對角點
				pointB_v=tmp2_v;
				pointC_u=tmp3_u; //對角線上點
				pointC_v=tmp3_v;
				pointA_u=tmp4_u; //對角線下點
				pointA_v=tmp4_v;
			}
			else if(tmp3_v < (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_13 < length_14){  //3在對角線上面    且     1-3線段<1-4線段
				pointA_u=tmp1_u; //最左點
				pointA_v=tmp1_v;
				pointC_u=tmp2_u; //對角點
				pointC_v=tmp2_v;
				pointD_u=tmp3_u; //對角線上點
				pointD_v=tmp3_v;
				pointB_u=tmp4_u; //對角線下點
				pointB_v=tmp4_v;	
			}
			else if(tmp3_v > (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_13 > length_14){  //3在對角線下面    且    1-3線段>1-4線段
				pointA_u=tmp1_u; //最左點
				pointA_v=tmp1_v;
				pointC_u=tmp2_u; //對角點
				pointC_v=tmp2_v;
				pointB_u=tmp3_u; //對角線下點
				pointB_v=tmp3_v;
				pointD_u=tmp4_u; //對角線上點
				pointD_v=tmp4_v;
			}
			else if(tmp3_v > (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_13 < length_14){  //3在對角線下面    且    1-3線段<1-4線段
				pointD_u=tmp1_u; //最左點
				pointD_v=tmp1_v;
				pointB_u=tmp2_u; //對角點
				pointB_v=tmp2_v;
				pointA_u=tmp3_u; //對角線下點
				pointA_v=tmp3_v;
				pointC_u=tmp4_u; //對角線上點
				pointC_v=tmp4_v;
			}
		}
		if (length_13 >= length_12 && length_13 >= length_14) {  //1-3線段最長
			x2=tmp3_u; //對角線的右邊點
			y2=tmp3_v;
			if (tmp2_v < (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 > length_14){  //2在對角線上面    且    1-2線段>1-4線段
				pointD_u=tmp1_u; //最左點
				pointD_v=tmp1_v;
				pointB_u=tmp3_u; //對角點
				pointB_v=tmp3_v;
				pointC_u=tmp2_u; //對角線上點
				pointC_v=tmp2_v;
				pointA_u=tmp4_u; //對角線下點
				pointA_v=tmp4_v;
			}
			else if(tmp2_v < (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 < length_14){  //2在對角線上面    且     1-2線段<1-4線段
				pointA_u=tmp1_u; //最左點
				pointA_v=tmp1_v;
				pointC_u=tmp3_u; //對角點
				pointC_v=tmp3_v;
				pointD_u=tmp2_u; //對角線上點
				pointD_v=tmp2_v;
				pointB_u=tmp4_u; //對角線下點
				pointB_v=tmp4_v;	
			}
			else if(tmp2_v > (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 > length_14){  //2在對角線下面    且    1-2線段>1-4線段
				pointA_u=tmp1_u; //最左點
				pointA_v=tmp1_v;
				pointC_u=tmp3_u; //對角點
				pointC_v=tmp3_v;
				pointB_u=tmp2_u; //對角線下點
				pointB_v=tmp2_v;
				pointD_u=tmp4_u; //對角線上點
				pointD_v=tmp4_v;
			}
			else if(tmp2_v > (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_12 < length_14){  //2在對角線下面    且    1-2線段<1-4線段
				pointD_u=tmp1_u; //最左點
				pointD_v=tmp1_v;
				pointB_u=tmp3_u; //對角點
				pointB_v=tmp3_v;
				pointA_u=tmp2_u; //對角線下點
				pointA_v=tmp2_v;
				pointC_u=tmp4_u; //對角線上點
				pointC_v=tmp4_v;
			} 
		}
		if (length_14 >= length_12 && length_14 >= length_13) {  //1-4線段最長
			x2=tmp4_u; //對角線的右邊點
			y2=tmp4_v;
			if (tmp2_v < (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 > length_13){  //2在對角線上面    且    1-2線段>1-3線段
				pointD_u=tmp1_u; //最左點
				pointD_v=tmp1_v;
				pointB_u=tmp4_u; //對角點
				pointB_v=tmp4_v;
				pointC_u=tmp2_u; //對角線上點
				pointC_v=tmp2_v;
				pointA_u=tmp3_u; //對角線下點
				pointA_v=tmp3_v;
			}
			else if(tmp2_v < (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 < length_13){  //2在對角線上面    且     1-2線段<1-3線段
				pointA_u=tmp1_u; //最左點
				pointA_v=tmp1_v;
				pointC_u=tmp4_u; //對角點
				pointC_v=tmp4_v;
				pointD_u=tmp2_u; //對角線上點
				pointD_v=tmp2_v;
				pointB_u=tmp3_u; //對角線下點
				pointB_v=tmp3_v;	
			}
			else if(tmp2_v > (y2-y1)/(x2-x1)*tmp2_u+(y1*x2-y2*x1)/(x2-x1) && length_12 > length_13){  //2在對角線下面    且    1-2線段>1-3線段
				pointA_u=tmp1_u; //最左點
				pointA_v=tmp1_v;
				pointC_u=tmp4_u; //對角點
				pointC_v=tmp4_v;
				pointB_u=tmp2_u; //對角線下點
				pointB_v=tmp2_v;
				pointD_u=tmp3_u; //對角線上點
				pointD_v=tmp3_v;
			}
			else if(tmp2_v > (y2-y1)/(x2-x1)*tmp3_u+(y1*x2-y2*x1)/(x2-x1) && length_12 < length_13){  //2在對角線下面    且    1-2線段<1-3線段
				pointD_u=tmp1_u; //最左點
				pointD_v=tmp1_v;
				pointB_u=tmp4_u; //對角點
				pointB_v=tmp4_v;
				pointA_u=tmp2_u; //對角線下點
				pointA_v=tmp2_v;
				pointC_u=tmp3_u; //對角線上點
				pointC_v=tmp3_v;
			} 
		}
		//ABCDList
		list.clear();
		list.add(new PointF(pointA_u, pointA_v));
		list.add(new PointF(pointB_u, pointB_v));
		list.add(new PointF(pointC_u, pointC_v));
		list.add(new PointF(pointD_u, pointD_v));
		
		//******************************************************************************
		// putText   //還無法在圖上標示A  B  C  D四點
		// Point point9 = new Point(); 
		// point9.x=pointA_u;
		// point9.y=pointA_v;
		// Core.putText(mLaser, "A", point9, 8, 3.0, new Scalar(0, 0, 255), 8);
		//********************************************************************************
/*
		//	讀取雷射投射角度alpha,beta
		//一行一行讀取檔案
		try {
			String path = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";
			File InFile1 = new File(path,"LaserANGLE.dat");
			Scanner sc = new Scanner(InFile1);
			String input = sc.nextLine();
			read_aAngle=Double.parseDouble(input);
			//System.out.println(input);
			String input1 = sc.nextLine();
			read_bAngle=Double.parseDouble(input1);
			//System.out.println(input1);
			sc.close();	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//計算ABCD四點在空間上的平面座標
		pointA_x =(float) 0.0;
		pointA_y =(float) 0.0;
		pointB_x =(float) (W/Math.cos(Math.toRadians(aAngle)));
		pointB_y =(float) (W * Math.tan(Math.toRadians(aAngle)) * Math.tan(Math.toRadians(bAngle)));
		pointC_x =(float) (W/Math.cos(Math.toRadians(aAngle)));
		pointC_y =(float) (H/Math.cos(Math.toRadians(bAngle))+W * Math.tan(Math.toRadians(aAngle)) * Math.tan(Math.toRadians(bAngle)));
		pointD_x =(float) 0.0;
		pointD_y =(float) (H/Math.cos(Math.toRadians(bAngle)));  	

		//求解2D-DLT的係數
		float[] b={pointA_u,pointB_u,pointC_u,pointD_u,pointA_v,pointB_v,pointC_v,pointD_v};
		mBB.put(0, 0, b);
		float[] a={pointA_x,pointA_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointA_u*pointA_x,-pointA_u*pointA_y,
				pointB_x,pointB_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointB_u*pointB_x,-pointB_u*pointB_y,
				pointC_x,pointC_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointC_u*pointC_x,-pointC_u*pointC_y,
				pointD_x,pointD_y,(float)1.0,(float)0.0,(float)0.0,(float)0.0,-pointD_u*pointD_x,-pointD_u*pointD_y,
				(float)0.0,(float)0.0,(float)0.0,pointA_x,pointA_y,(float)1.0,-pointA_v*pointA_x,-pointA_v*pointA_y,
				(float)0.0,(float)0.0,(float)0.0,pointB_x,pointB_y,(float)1.0,-pointB_v*pointB_x,-pointB_v*pointB_y,
				(float)0.0,(float)0.0,(float)0.0,pointC_x,pointC_y,(float)1.0,-pointC_v*pointC_x,-pointC_v*pointC_y,
				(float)0.0,(float)0.0,(float)0.0,pointD_x,pointD_y,(float)1.0,-pointD_v*pointD_x,-pointD_v*pointD_y};
		mAA.put(0, 0, a);
		Core.solve(mAA, mBB, mXX);
		float[] x={0,0,0,0,0,0,0,0};
		mXX.get(0, 0, x);
		System.out.println(Double.toString(x[0]));
		System.out.println(Double.toString(x[1]));
		System.out.println(Double.toString(x[2]));
		System.out.println(Double.toString(x[3]));
		System.out.println(Double.toString(x[4]));
		System.out.println(Double.toString(x[5]));
		System.out.println(Double.toString(x[6]));
		System.out.println(Double.toString(x[7]));



		//output 2D-DLT係數
		try {				
			String path = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";
			File outFile1 = new File(path,"DLT_Coefficients.dat"); 
			FileOutputStream fileos1 = new FileOutputStream(outFile1);

			tmpString=Double.toString(x[0])+"\r\n"; 
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[1])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[2])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[3])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[4])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[5])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[6])+"\r\n";  
			fileos1.write(tmpString.getBytes());
			tmpString=Double.toString(x[7])+"\r\n"; 
			fileos1.write(tmpString.getBytes());


//			//驗算係數	  
//	          double tmptmp;    		  
//     		  //pointA_u, pointA_v
//     		  tmptmp=(x[0]*pointA_x+x[1]*pointA_y+x[2])/(x[6]*pointA_x+x[7]*pointA_y+1);
//     		  tmpString=Double.toString(tmptmp)+"\r\n";  
//     		  fileos1.write(tmpString.getBytes());
//     		  tmptmp=(x[3]*pointA_x+x[4]*pointA_y+x[5])/(x[6]*pointA_x+x[7]*pointA_y+1);
//     		  tmpString=Double.toString(tmptmp)+"\r\n"; 
//     		  fileos1.write(tmpString.getBytes());
//
//     		  //pointB_u, pointB_v
//     		  tmptmp=(x[0]*pointB_x+x[1]*pointB_y+x[2])/(x[6]*pointB_x+x[7]*pointB_y+1);
//     		  tmpString=Double.toString(tmptmp)+"\r\n";  
//     		  fileos1.write(tmpString.getBytes());
//     		  tmptmp=(x[3]*pointB_x+x[4]*pointB_y+x[5])/(x[6]*pointB_x+x[7]*pointB_y+1);
//     		  tmpString=Double.toString(tmptmp)+"\r\n"; 
//     		  fileos1.write(tmpString.getBytes());
			   
			fileos1.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		key2=false;    	*/

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub	
		menu.add(0, MENU_AUTO_LASER, 0, "Auto Laser Detection");
		menu.add(0, MENU_MANU_LASER, 0, "Manual LaserPoint");
		menu.add(0, MENU_GET_DLTIMG, 0, "Get DLTImage");
		menu.add(0, MENU_PROFILE, 0, "ProFile");
		menu.add(0, MENU_SEMIAUTO, 0, "SemiAuto");
		//menu.add(0, MENU_OPENCLOSE_SENSOR, 0, "Open/Close Sensor");
		//menu.add(0, MENU_INIT_AB, 0, "Get InitAlphaBeta");
		//menu.add(0, MENU_GET_AB, 0, "Get AlphaBeta");
		menu.add(0, MENU_OPENCLOSE_GRID, 0, "Open/Close Grid");
		menu.add(0, MENU_GET_THRESH, 0, "Get ThresholdImage");
		menu.add(0, MENU_ABOUT, 0, "About");
		menu.add(0, MENU_EXIT, 0, "Exit");
			
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case MENU_OPENCLOSE_SENSOR:	
			//結束此程式
			//Rotation資訊
			if (bShowSensor == false)
			{
				mHandler = new Handler();
				updateTimer = new Timer();
				mClock = new TimerTask() {
					public void run()
					{          
						mHandler.post(new Runnable() {
							@Override
							public void run()
							{
								DecimalFormat df4 = new DecimalFormat("#.##");
								String str4 = "Gyroscope: " + df4.format(gyroscopeAngles[0]) + " , " + df4.format(gyroscopeAngles[1]) + " , " + df4.format(gyroscopeAngles[2]); 
								String str5 = "Accelerometer:" + df4.format(accelerometerValues[0]) + " , "+ df4.format(accelerometerValues[1]) + " , "+ df4.format(accelerometerValues[2]);
								String str6 = "Angle: " + df4.format(aAngle) + " , " + df4.format(bAngle);
								mRotText.setText(str4);
								mAccText.setText(str5);
								mAngText.setText(str6);
								
							}
						});
					}
				};
				updateTimer.schedule(mClock, 0, 100);
				bShowSensor = true;
	        }
			else
			{
				updateTimer.cancel();
				mRotText.setText("");
				mAccText.setText("");
				bShowSensor = false;
			}
			break;
		case MENU_INIT_AB:	
			//取得 initial Alpah Beta	
			//取得Alpah Beta
			tmp_aAngle = 0;
			tmp_bAngle = 0;
			for (int i=0;i<31;i++)
			{
			    getAngle();
			}		
			count1 = 0;
			
			//取得Initial Alpha Beta
			aInitAngle = 0;
			bInitAngle = 0;
			gyroscopeValues[0] = 0; 
			gyroscopeValues[1] = 0; 
			gyroscopeValues[2] = 0; 
			
			//顯示
			DecimalFormat df2 = new DecimalFormat("#.##");
			String str2 = "Initial Alpha = " + df2.format(aInitAngle) + " ,Beta =" + df2.format(bInitAngle);
			mLogText.setText(str2);
			break;
		case MENU_GET_AB:
			//取得Alpah Beta
			tmp_aAngle = 0;
			tmp_bAngle = 0;
			for (int i=0;i<31;i++)
			{
			    getAngle();
			}		
			count1 = 0;
			
			//取得Alpha與Beta的差
			//aAngle = aAngle - aInitAngle;
			//bAngle = bAngle - bInitAngle;

			//顯示
			DecimalFormat df3 = new DecimalFormat("#.##");
			String str3 = "Alpha = " + df3.format(aAngle) + " ,Beta =" + df3.format(bAngle);
			mLogText.setText(str3);
			
			//寫入角度標至檔案
			saveLaserAngle(aAngle,bAngle);

			break;
		case MENU_AUTO_LASER:
			//關避顯示sensor
			if (bShowSensor)
			{
				updateTimer.cancel();
				mRotText.setText("");
				mAccText.setText("");
				mAngText.setText("");
				bShowSensor = true;
			}
			//紅點偵測程式
			DecimalFormat df6 = new DecimalFormat("#.##");
			//sensorManager.unregisterListener(this);
			/*
			//float[] buff5566 = new float[5947392];

			System.out.println( "totalmem = " + df6.format(Runtime.getRuntime().totalMemory()));
			System.out.println( "freemem = " + df6.format(Runtime.getRuntime().freeMemory()));
			
			Mat abcd1 =  new Mat(2000, 2000, CvType.CV_32FC1);
			Mat abcd2 =  new Mat(2000, 2000, CvType.CV_32FC1);
			Mat abcd3 =  new Mat(2000, 2000, CvType.CV_32FC1);
			Mat abcd4 =  new Mat(2000, 2000, CvType.CV_32FC1);
			Mat abcd5 =  new Mat(2000, 2000, CvType.CV_32FC1);
			Mat abcd6 =  new Mat(2000, 2000, CvType.CV_32FC1);
			Mat abcd7 =  new Mat(2000, 2000, CvType.CV_32FC1);
			Mat abcd8 =  new Mat(2000, 2000, CvType.CV_32FC1);
			Mat abcd9 =  new Mat(2000, 2000, CvType.CV_32FC1);
			System.out.println( "mem = " + df6.format(Runtime.getRuntime().totalMemory()));
			System.out.println( "freemem = " + df6.format(Runtime.getRuntime().freeMemory()));
			System.out.println( "maxmem = " + df6.format(Runtime.getRuntime().maxMemory()));
			float[] cdef1 = new float[594739];
			System.out.println( "mem = " + df6.format(Runtime.getRuntime().totalMemory()));
			System.out.println( "freemem = " + df6.format(Runtime.getRuntime().freeMemory()));
			System.out.println( "maxmem = " + df6.format(Runtime.getRuntime().maxMemory()));
			float[] cdef2 = new float[594739];
			float[] cdef3 = new float[594739];
			System.out.println( "mem = " + df6.format(Runtime.getRuntime().totalMemory()));
			System.out.println( "freemem = " + df6.format(Runtime.getRuntime().freeMemory()));
			System.out.println( "maxmem = " + df6.format(Runtime.getRuntime().maxMemory()));
			float[] cdef4 = new float[594739];
			float[] cdef5 = new float[594739];
			float[] cdef6 = new float[594739];
			System.out.println( "mem = " + df6.format(Runtime.getRuntime().totalMemory()));
			System.out.println( "freemem = " + df6.format(Runtime.getRuntime().freeMemory()));
			System.out.println( "maxmem = " + df6.format(Runtime.getRuntime().maxMemory()));
			float[] cdef7 = new float[594739];
			float[] cdef8 = new float[594739];
			float[] cdef9 = new float[594739];
			System.out.println( "mem = " + df6.format(Runtime.getRuntime().totalMemory()));
			System.out.println( "freemem = " + df6.format(Runtime.getRuntime().freeMemory()));
			System.out.println( "maxmem = " + df6.format(Runtime.getRuntime().maxMemory()));
			float[] cdef10 = new float[594739];
			float[] cdef11 = new float[594739];
			float[] cdef12 = new float[594739];
			float[] cdef13 = new float[594739];
			float[] cdef14 = new float[594739];
			System.out.println( "mem = " + df6.format(Runtime.getRuntime().totalMemory()));
			System.out.println( "freemem = " + df6.format(Runtime.getRuntime().freeMemory()));
			float[] cdef15 = new float[5947392];
			System.out.println( "mem = " + df6.format(Runtime.getRuntime().totalMemory()));
			System.out.println( "freemem = " + df6.format(Runtime.getRuntime().freeMemory()));
			*/
			/*
		    long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		    //String s1= "12345";
		    Mat mmm = new Mat(10000,10000,CvType.CV_32FC1);
		    long endMem = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
		    System.out.println("s1: "+ (startMem - endMem));
		    
		    
		    startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		    //String s2= new String("12345");
		    float a2 = 1.0f;
		    endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ;
		    System.out.println("s2: "+ (startMem - endMem));
		    
		    startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ;
		   // String s3= new String("123456789012345678901234567890");
		    double a3 = 1.0;
		    endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ;
		    System.out.println("s3: "+ (startMem - endMem));
		    
		    
		    startMem = Runtime.getRuntime().maxMemory();
		    //String s5= new String();
		    endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ;
		    System.out.println("max: "+ (startMem ));
		    
		    startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		    //String s4= new String("");
		    int a4[] = new int[500000000];
		    endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ;
		    System.out.println("s4: "+ (startMem - endMem));
		    
		    //int a = s1.length() + s2.length() +s3.length()+ +s4.length() +s5.length();
	        //a = a+1;
		    int b = (int) ( mmm.cols()+a2+a3);
		    */
			getLaserDetectionPoint();
			//寫入紅點雷射影像位置
			saveLaserImagePos(mLaserPointList);
			break;
		case MENU_MANU_LASER:
			if (bManualLaserPoint == false)
			{
				//建立新的4點(0,0)的array
				mLaserPointList = new ArrayList ();
				mLaserPointList.add(new PointF(0,0));
				mLaserPointList.add(new PointF(0,0));
				mLaserPointList.add(new PointF(0,0));
				mLaserPointList.add(new PointF(0,0));
				iv.vecLaserPointList = mLaserPointList;
				iv.nLaserPointIndex = nLaserPointIndex;
				
				mTopButton.setVisibility(View.VISIBLE);  
				mDownButton.setVisibility(View.VISIBLE);  
				mRightButton.setVisibility(View.VISIBLE);  
				mLeftButton.setVisibility(View.VISIBLE);  
				mSaveButton.setVisibility(View.VISIBLE);  
				mCenterButton.setVisibility(View.VISIBLE);  
								
				iv.isLaserPointMode = true;
				bManualLaserPoint = true;
			}else
			{
				mTopButton.setVisibility(View.GONE);  
				mDownButton.setVisibility(View.GONE);  
				mRightButton.setVisibility(View.GONE);  
				mLeftButton.setVisibility(View.GONE);  
				mSaveButton.setVisibility(View.GONE);  
				mCenterButton.setVisibility(View.GONE);  			
				
				iv.isLaserPointMode = false;
				bManualLaserPoint = false;
			}
			//更新畫面 
			iv.postInvalidate();
			break;
		case MENU_GET_DLTIMG:
			//結束此程式	
			getDLTImage();
	        
			//修改狀態
			mTopButton.setVisibility(View.GONE);  
			mDownButton.setVisibility(View.GONE);  
			mRightButton.setVisibility(View.GONE);  
			mLeftButton.setVisibility(View.GONE);  
			mSaveButton.setVisibility(View.GONE);  
			mCenterButton.setVisibility(View.GONE); 	
			
			iv.isLaserPointMode = false;
			bManualLaserPoint = false;
			iv.isDLTMode = true;		
			//mLogText.setText("DLT");
			break;
		case MENU_PROFILE:
			/*
			bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();   
			int  colorValue = bitmap.getPixel((int)iv.crackstart_x, (int)iv.crackend_y); 
			int r = Color.red(colorValue);
			int g = Color.green(colorValue);
			int b = Color.blue(colorValue);
			String str = r + "," + g + "," + b;
			mText.setText(str);
			//mText.setText("裂縫好長............");*/
			lineCalculate();
            iv.invalidate();
			break;
		case MENU_SEMIAUTO:
			mSemiAuto = new SemiAutoExtraction();
			Mat img = new Mat();
			Mat temp = new Mat();
			Utils.bitmapToMat(mBitmap, img);
			Imgproc.cvtColor(img,temp,Imgproc.COLOR_BGRA2GRAY, 4);
			mSemiAuto.image = temp;
			mSemiAuto.startPoint.x = (float)iv.crackstart_x;
			mSemiAuto.startPoint.y = (float)iv.crackstart_y;
			mSemiAuto.endPoint.x = (float)iv.crackend_x;
			mSemiAuto.endPoint.y = (float)iv.crackend_y;
			mSemiAuto.m_fpNodeLen = Double.parseDouble(mNodeText.getText().toString());   //從UI擷取值
			mSemiAuto.m_fpNormalLen = Double.parseDouble(mNormalText.getText().toString());   //從UI擷取值
			//mSemiAuto.m_fpNodeLen = 20.0f;
			//mSemiAuto.m_fpNormalLen = 20.0f;
			mSemiAuto.m_nSmoothPara = 1;
			mSemiAuto.m_nProfileHeight = 30;
			mSemiAuto.run();
			
			//接收資料
			iv.vecSemiAutoBounUpPoint = mSemiAuto.vecBounUpPoint; 
			iv.vecSemiAutoBounDownPoint = mSemiAuto.vecBounDownPoint; 
			iv.vecSemiAutoNodePoint = mSemiAuto.vecNodePoint;
			iv.vecSemiAutoNodeWidth = mSemiAuto.vecNodeWidth;
					
			//傳送長度與寬度
			double wid_crack = 0.0f;
			double len_crack = 0.0f;
			
			for (int i=0;i<iv.vecSemiAutoNodeWidth.size();i++)
				wid_crack = wid_crack + iv.vecSemiAutoNodeWidth.get(i);
            
			if (iv.vecSemiAutoNodeWidth.size()!=0)
				wid_crack = wid_crack/iv.vecSemiAutoNodeWidth.size();
			
			len_crack =  mSemiAuto.getCrackLength(mSemiAuto.vecNodePoint);
			DecimalFormat df = new DecimalFormat("#.##");
			String str = "Length = " + df.format(len_crack / mScale) + "mm   Width = " + df.format(wid_crack / mScale) + "mm";   
			mLogText.setText(str);	
			
			iv.invalidate();
			break;
		case MENU_OPENCLOSE_GRID:	
			if (iv.isGridMode)
				iv.isGridMode = false;
			else
				iv.isGridMode = true;
			
			iv.invalidate();
			break;
		case MENU_ABOUT:
			//結束此程式
			mLogText.setText("國家高速網路與計算中心............");
			break;
		case MENU_EXIT:
			//結束此程式
			finish();
			break;
		case MENU_GET_THRESH:
		   
		    
			mThresholdDlg.show();
		    
		    break;
		default:

		}
		return super.onOptionsItemSelected(item);
		
	}
    /**
     * 
     */
	public void lineCalculate() {
		if (iv.crackstart_x==0 && iv.crackstart_y==0 && iv.crackend_x==0 && iv.crackend_y==0 )
		{
			Toast.makeText(this, "無設定起終點座標", 1000).show();
			return;
		}
		ArrayList <PointF> vecLinePos = new ArrayList ();
		ArrayList <Integer> vecLineValue = new ArrayList ();
		Mat gray = new Mat();
		Mat img = new Mat();
		Utils.bitmapToMat(mBitmap, img);
		Imgproc.cvtColor(img,gray,Imgproc.COLOR_BGRA2GRAY, 4);
		PointF s = new PointF(iv.crackstart_x, iv.crackstart_y);
		PointF e = new PointF(iv.crackend_x, iv.crackend_y);
		//整條線段長度
		double len = Math.sqrt((s.x - e.x)*(s.x - e.x) + (s.y - e.y)*(s.y - e.y));
		//從起始點開始內插找點 1cm 2cm 3cm.....
		PointF pt;
		double []v;
		double total = 0.0f;
		double avg = 0.0f;
		//double si = 0.0f;
		double si = Double.parseDouble(mStdText.getText().toString());   //從UI擷取值
		//Imgproc.medianBlur(gray, gray, 5);
		ArrayList<Integer> vecTotalValue = new ArrayList(); 
		
		for (int i = 0;i<len;i++){
			pt = lineInterp(s,e,i);
			pt.x = Math.round(pt.x);
			pt.y = Math.round(pt.y);
			v = gray.get(Math.round(pt.y), Math.round(pt.x));
			//v = CV_IMAGE_ELEM(gray,uchar,ROUND(pt.y),ROUND(pt.x));
			vecLinePos.add(pt);
			vecLineValue.add((int)v[0]);
			//total = total + vecLineValue.get(i);
			
			//多取樣附近八點做平均
			v = gray.get(Math.round(pt.y-1), Math.round(pt.x-1));
			vecTotalValue.add((int)v[0]);
			total = total + v[0];
			
			v = gray.get(Math.round(pt.y-1), Math.round(pt.x));
			vecTotalValue.add((int)v[0]);
			total = total + v[0];
			
			v = gray.get(Math.round(pt.y-1), Math.round(pt.x+1));
			vecTotalValue.add((int)v[0]);
			total = total + v[0];
			
			v = gray.get(Math.round(pt.y), Math.round(pt.x-1));
			vecTotalValue.add((int)v[0]);
			total = total + v[0];
			
			v = gray.get(Math.round(pt.y), Math.round(pt.x));
			vecTotalValue.add((int)v[0]);
			total = total + v[0];
			
			
			v = gray.get(Math.round(pt.y), Math.round(pt.x+1));
			vecTotalValue.add((int)v[0]);
			total = total + v[0];
			
			
			v = gray.get(Math.round(pt.y+1), Math.round(pt.x-1));
			vecTotalValue.add((int)v[0]);
			total = total + v[0];
			
			v = gray.get(Math.round(pt.y+1), Math.round(pt.x));
			vecTotalValue.add((int)v[0]);
			total = total + v[0];
			
			v = gray.get(Math.round(pt.y+1), Math.round(pt.x+1));
			vecTotalValue.add((int)v[0]);
			total = total + v[0];
	
		}	
		avg = total / vecTotalValue.size();                 //平均數
		total = 0;
		for (int i = 0; i < vecTotalValue.size(); i++)
			total = total + (avg - vecTotalValue.get(i))*(avg - vecTotalValue.get(i));

		si = Math.sqrt(total / vecTotalValue.size());    //標準差
		//計算裂縫的範圍,兩種方法找到邊界點
		int num = vecLineValue.size();
		int posL = 0;
		int posR = 0;
		//double fSTD = 1.0f;         //預設一個標準差
		double fSTD = Double.parseDouble(mStdText.getText().toString());
		boolean isMinToBorder = true;
		

		if (isMinToBorder == true)
		{   //第一種方法:由最低點向外搜尋
			int posMin = 0;
			double fValuse = 255.0;
			for (int i = 1; i < num-1; i++)
			{
				if (vecLineValue.get(i) < fValuse)                  //計算像亮中的最小值
				{
					fValuse = vecLineValue.get(i);
					posMin = i;
				}
			}
			for (int i = posMin; i > 1; i--)
			{
				if (vecLineValue.get(i) > avg - si*fSTD )     //判斷是否大於N個標準差
				{
					posL = i+1;                                 //要加回來
					//posL = i;
					break;
				}
			}
			for (int i = posMin; i < num; i++)
			{
				if (vecLineValue.get(i) > avg - si*fSTD )     //判斷是否大於N個標準差
				{
					posR = i;                                //要減回來
					//posR = i;                                    //一邊撿回來一邊就不用加
					break;
				}
			}
		}else
		{    //第二種方法:由頭尾點向內搜尋
			for (int i = 3; i < num-3; i++)
			{
				if (vecLineValue.get(i) < avg - si*fSTD )     //判斷是否大於N個標準差
				{
					//posL = i-1;                                 //要減回來
					posL = i;                                     //一邊撿回來一邊就不用加
					break;
				}
			}
			for (int i = num-3; i > 3; i--)
			{
				if (vecLineValue.get(i) < avg - si*fSTD )     //判斷是否大於N個標準差
				{
					posR = i+1;                                 //要加回來
					break;
				}
			}
		}
		//資料傳出至介面
		/*MainlForm->m_tpXCrackStart.x =  vecLinePos[posL].x;
		MainlForm->m_tpXCrackStart.y =  vecLinePos[posL].y;
		MainlForm->m_tpXCrackEnd.x =  vecLinePos[posR].x;
		MainlForm->m_tpXCrackEnd.y =  vecLinePos[posR].y;*/
		//裂縫的長度
		double len_crack = 0.0f;
		PointF L = new PointF(); 
		PointF R = new PointF(); 
		boolean isSubpixel = false;
		if (isSubpixel == true)    //判斷是否需要內差至subpixel
		{
			if (vecLineValue.get(posL-1) == vecLineValue.get(posL) || vecLineValue.get(posR+1) == vecLineValue.get(posR) )   //避免分母為0
			{
				L.x =  vecLinePos.get(posL).x;                                                                                                                                           //無內插
				L.y =  vecLinePos.get(posL).y;
				R.x =  vecLinePos.get(posR).x;
				R.y =  vecLinePos.get(posR).y;
			}else
			{
				double tempLX = (vecLineValue.get(posL-1) - (avg-si*fSTD))/(vecLineValue.get(posL-1) - vecLineValue.get(posL) *(vecLinePos.get(posL).x - vecLinePos.get(posL-1).x));
				double tempLY = (vecLineValue.get(posL-1) - (avg-si*fSTD))/(vecLineValue.get(posL-1) - vecLineValue.get(posL) *(vecLinePos.get(posL).y - vecLinePos.get(posL-1).y));
				double tempRX =  ((avg-si*fSTD) - vecLineValue.get(posR))/(vecLineValue.get(posR+1) - vecLineValue.get(posR)) *(vecLinePos.get(posR+1).x - vecLinePos.get(posR).x);
				double tempRY =  ((avg-si*fSTD) - vecLineValue.get(posR))/(vecLineValue.get(posR+1) - vecLineValue.get(posR)) *(vecLinePos.get(posR+1).y - vecLinePos.get(posR).y);
				L.x = (float) (vecLinePos.get(posL-1).x + tempLX);       //內插到subpixel
				L.y = (float) (vecLinePos.get(posL-1).y + tempLY);
				R.x = (float) (vecLinePos.get(posR).x + tempRX);
				R.y = (float) (vecLinePos.get(posR).y + tempRY);
			}
		}
		else
		{
			L.x =  vecLinePos.get(posL).x;                                                                                                                                           //無內插
			L.y =  vecLinePos.get(posL).y;
			R.x =  vecLinePos.get(posR).x;
			R.y =  vecLinePos.get(posR).y;
		}
		len_crack = Math.sqrt((L.x-R.x)*(L.x-R.x) + (L.y-R.y)*(L.y-R.y));
		iv.mInterpPTLeft.x = (int)L.x;
		iv.mInterpPTLeft.y = (int)L.y;
		iv.mInterpPTRight.x = (int)R.x;
		iv.mInterpPTRight.y = (int)R.y;
		//顯示所有的長度資訊
		/*STDWidthLabel->Caption = FloatToStrF(si,ffFixed,12,2);
		MeanValueLabel->Caption = FloatToStrF(avg,ffFixed,12,2);
		TotalValueLabel->Caption = FloatToStrF(len,ffFixed,12,2) / pow(10,MainlForm->m_DataSet->nCMUnit);           //換算單位cm
		CrackWidthLabel->Caption = FloatToStrF(len_crack,ffFixed,12,2) / pow(10,MainlForm->m_DataSet->nCMUnit-1);   //換算單位mm*/
		//傳送長度
		DecimalFormat df = new DecimalFormat("#.##");
		String str = "Width = " +  df.format(len_crack / mScale) + "mm"; 
		mLogText.setText(str);
		
		//清除記憶體
		/*vecLinePos.clear();
		vecLineValue.clear();
		cvReleaseImage(&gray);*/
		gray.release();
		img.release();
		vecTotalValue.clear();

	}
    /**
     * 線性內差法
     * @param start 起始點
     * @param end 終點 
     * @param length 內差的長度
     * @return 回傳內差的點
     */
	public PointF lineInterp(PointF start, PointF end, double length) {
		if (length == 0)
			return start;
		
		double dist = 0.0f;
		double x = 0.0f;
		double y = 0.0f;
		
		//四種有可能的情形都要考慮
		if (Math.abs(start.x - end.x) > Math.abs(start.y - end.y))
		{
			x = start.x;
			while (dist < length)
			{
				if (start.x < end.x)
					x = x+1;
				else
					x = x-1;
				//避免分母為0
				if(end.x == start.x)
					end.x += 1;

				y = ((end.y - start.y) / (end.x - start.x)) * (x - start.x) + start.y;
				dist = calDist(start, new PointF((float)x,(float)y));
			}
		}
		else
		{
				y = start.y;
				while (dist < length)
				{
					if (start.y < end.y)
						y = y+1;
					else
						y = y-1;
	                //避免分母為0
					if(end.y == start.y)	
						end.y += 1;

					x = ((end.x - start.x) / (end.y - start.y))  * (y - start.y) + start.x;
					dist = calDist(start, new PointF((float)x,(float)y));
				}
		}
		return new PointF((float)x,(float)y);		
	 	
	}
    /**
     * 直線距離
     * @param start 起始點
     * @param end 終點 
     * @return 距離
     */
	public double calDist(PointF start,PointF end){
		return Math.sqrt((start.x - end.x)*(start.x - end.x) + (start.y - end.y)*(start.y - end.y));
		
	}
	private void saveDLTParameter(float[] x){
		//output 2D-DLT係數
		try {				
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				String path = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";			
				File outFile1 = new File(path); 
				if( !outFile1.exists() )
					outFile1.mkdirs();

				FileWriter fileos1 = new FileWriter(path + "/DLT_Coefficients.dat");
				fileos1.write(Double.toString(x[0])+"\r\n");
				fileos1.write(Double.toString(x[1])+"\r\n");
				fileos1.write(Double.toString(x[2])+"\r\n");
				fileos1.write(Double.toString(x[3])+"\r\n");
				fileos1.write(Double.toString(x[4])+"\r\n");
				fileos1.write(Double.toString(x[5])+"\r\n");
				fileos1.write(Double.toString(x[6])+"\r\n");
				fileos1.write(Double.toString(x[7])+"\r\n");

				//驗算係數
				/*	  
	              double tmptmp;    		  
     		  //pointA_u, pointA_v
     		  tmptmp=(x[0]*pointA_x+x[1]*pointA_y+x[2])/(x[6]*pointA_x+x[7]*pointA_y+1);
     		  tmpString=Double.toString(tmptmp)+"\r\n";  
     		  fileos1.write(tmpString.getBytes());
     		  tmptmp=(x[3]*pointA_x+x[4]*pointA_y+x[5])/(x[6]*pointA_x+x[7]*pointA_y+1);
     		  tmpString=Double.toString(tmptmp)+"\r\n"; 
     		  fileos1.write(tmpString.getBytes());

     		  //pointB_u, pointB_v
     		  tmptmp=(x[0]*pointB_x+x[1]*pointB_y+x[2])/(x[6]*pointB_x+x[7]*pointB_y+1);
     		  tmpString=Double.toString(tmptmp)+"\r\n";  
     		  fileos1.write(tmpString.getBytes());
     		  tmptmp=(x[3]*pointB_x+x[4]*pointB_y+x[5])/(x[6]*pointB_x+x[7]*pointB_y+1);
     		  tmpString=Double.toString(tmptmp)+"\r\n"; 
     		  fileos1.write(tmpString.getBytes());
				 */	  
				fileos1.close();
				Toast.makeText(this, "DLT參數檔案寫入成功", 1000).show();  
			}
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	private void saveLaserAngle(double aAng, double bAng){
		//儲慛雷射角度與座標至檔案
		pointA_x =(float) 0.0;
		pointA_y =(float) 0.0;
		pointB_x =(float) (W/Math.cos(Math.toRadians(aAng)));
		pointB_y =(float) (W * Math.tan(Math.toRadians(aAng)) * Math.tan(Math.toRadians(bAng)));
		pointC_x =(float) (W/Math.cos(Math.toRadians(aAng)));
		pointC_y =(float) (H/Math.cos(Math.toRadians(bAng))+W * Math.tan(Math.toRadians(aAng)) * Math.tan(Math.toRadians(bAng)));
		pointD_x =(float) 0.0;
		pointD_y =(float) (H/Math.cos(Math.toRadians(bAng)));    		

		//output data
		try {		
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				String path = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";
				File outFile1 = new File(path); 
				if( !outFile1.exists() )
					outFile1.mkdirs();

				FileWriter fileos1 = new FileWriter(path + "/LaserANGLE.dat");
				fileos1.write(Double.toString(aAng)+"\r\n");
				fileos1.write(Double.toString(bAng)+"\r\n");

				fileos1.write(Double.toString(pointA_x)+"\r\n");
				fileos1.write(Double.toString(pointA_y)+"\r\n");
				fileos1.write(Double.toString(pointB_x)+"\r\n");
				fileos1.write(Double.toString(pointB_y)+"\r\n");
				fileos1.write(Double.toString(pointC_x)+"\r\n");
				fileos1.write(Double.toString(pointC_y)+"\r\n");
				fileos1.write(Double.toString(pointD_x)+"\r\n");
				fileos1.write(Double.toString(pointD_y)+"\r\n");

				fileos1.close();
				Toast.makeText(this, "雷射角度檔案寫入成功", 1000).show();  
				/*
			String path = Environment.getExternalStorageDirectory().toString ()+"/";
			FileWriter fw = new FileWriter(path + "/LaserANGLE.txt", true);
		    BufferedWriter fileos1 = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結

		    tmpString=Double.toString(aAngle)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(bAngle)+"\r\n";
			fileos1.write(tmpString);

			tmpString=Double.toString(pointA_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointA_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointB_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointB_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointC_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointC_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointD_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointD_y)+"\r\n";
			fileos1.write(tmpString);        

			fileos1.close();*/
			}else
				Toast.makeText(this, "雷射角度檔案寫入失敗", 1000).show(); 
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			Toast.makeText(this, "雷射角度檔案寫入失敗", 1000).show(); 
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "雷射角度檔案寫入失敗", 1000).show(); 
		}		
	}
	private void saveLaserImagePos(ArrayList <PointF> laserpoint){
   	
		if (laserpoint.size()!=4)
		{
			Toast.makeText(this, "偵測出非四個紅點!不寫入影像位置檔案!!", 2000).show(); 
			return;
		}
		//output data
		try {		
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				String path = Environment.getExternalStorageDirectory().toString ()+"/PIVdata";
				File outFile1 = new File(path); 
				if( !outFile1.exists() )
					outFile1.mkdirs();

				FileWriter fileos1 = new FileWriter(path + "/LaserImagePos.dat");
				for (int i=0;i<laserpoint.size();i++)
				{
					fileos1.write(Double.toString(laserpoint.get(i).x)+"\r\n");
					fileos1.write(Double.toString(laserpoint.get(i).y)+"\r\n");
				}

				fileos1.close();
				Toast.makeText(this, "紅點影像位置檔案寫入成功", 2000).show();  
				/*
			String path = Environment.getExternalStorageDirectory().toString ()+"/";
			FileWriter fw = new FileWriter(path + "/LaserANGLE.txt", true);
		    BufferedWriter fileos1 = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結

		    tmpString=Double.toString(aAngle)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(bAngle)+"\r\n";
			fileos1.write(tmpString);

			tmpString=Double.toString(pointA_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointA_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointB_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointB_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointC_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointC_y)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointD_x)+"\r\n";
			fileos1.write(tmpString);
			tmpString=Double.toString(pointD_y)+"\r\n";
			fileos1.write(tmpString);        

			fileos1.close();*/
			}else
				Toast.makeText(this, "紅點影像位置檔案寫入失敗", 2000).show(); 
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			Toast.makeText(this, "紅點影像位置檔案寫入失敗", 2000).show(); 
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "紅點影像位置檔案寫入失敗", 2000).show(); 
		}		
	}
	private void saveExif(String filename) throws ImageReadException, ImageWriteException, IOException 
	{
        /*
		try {
			ExifInterface exif = new ExifInterface(filename);
			exif.setAttribute("UserComment", "儲存exit:alpha = 60, beta = 60");
			exif.saveAttributes();
	        String str = exif.getAttribute(ExifInterface.TAG_MAKE);
	        
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "exif寫入失敗", 1000).show(); 
			e1.printStackTrace();
		}*/

		OutputStream os = null;
		try
		{
			TiffOutputSet outputSet = null;

			// note that metadata might be null if no metadata is found.
			IImageMetadata metadata = Sanselan.getMetadata(new File(filename));
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata)
			{
				// note that exif might be null if no Exif metadata is found.
				TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif)
				{
					// TiffImageMetadata class is immutable (read-only).
					// TiffOutputSet class represents the Exif data to write.
					//
					// Usually, we want to update existing Exif metadata by
					// changing
					// the values of a few fields, or adding a field.
					// In these cases, it is easiest to use getOutputSet() to
					// start with a "copy" of the fields read from the image.
					outputSet = exif.getOutputSet();
				}
			}

			// if file does not contain any exif metadata, we create an empty
			// set of exif metadata. Otherwise, we keep all of the other
			// existing tags.
			if (null == outputSet)
				outputSet = new TiffOutputSet();

			{
				// Example of how to add a field/tag to the output set.
				//
				// Note that you should first remove the field/tag if it already
				// exists in this directory, or you may end up with duplicate
				// tags. See above.
				//
				// Certain fields/tags are expected in certain Exif directories;
				// Others can occur in more than one directory (and often have a
				// different meaning in different directories).
				//
				// TagInfo constants often contain a description of what
				// directories are associated with a given tag.
				//
				// see
				// org.apache.sanselan.formats.tiff.constants.AllTagConstants
				//
				byte[] bytesUserComment = ExifTagConstants.EXIF_TAG_USER_COMMENT.encodeValue(TiffFieldTypeConstants.FIELD_TYPE_ASCII,"i Love 5566", outputSet.byteOrder);

				TiffOutputField aperture = new TiffOutputField(ExifTagConstants.EXIF_TAG_USER_COMMENT
						                                     ,ExifTagConstants.EXIF_TAG_USER_COMMENT.dataTypes[0]
						                                     ,bytesUserComment.length
						                                     ,bytesUserComment);

				TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
				// make sure to remove old value if present (this method will
				// not fail if the tag does not exist).
				exifDirectory.removeField(TiffConstants.EXIF_TAG_USER_COMMENT);
				exifDirectory.add(aperture);
			}


			// printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);
			File tempFile = new File(Environment.getExternalStorageDirectory()+ "/tempImage.jpg");
			os = new FileOutputStream(tempFile);
			os = new BufferedOutputStream(os);

			try {
				new ExifRewriter().updateExifMetadataLossless(new File(filename), os, outputSet);//無法寫在同一個檔案上面!
			} finally {
				os.close();
				//將暫存檔名Rename成原檔名!
				tempFile.renameTo(new File(filename));
			}
			Toast.makeText(this, "exif寫入成功", 1000).show(); 
			os.close();
			os = null;
		} finally
		{
			if (os != null)
				try
				{
					os.close();
				} catch (IOException e)
				{

				}
		}
		
	}
    public static String getFilePathFromContentUri(Uri selectedVideoUri, ContentResolver contentResolver) {  
        String filePath;  
        String[] filePathColumn = {MediaColumns.DATA};  

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);  
//      也可用下面的方法拿到cursor  
//      Cursor cursor = this.context.managedQuery(selectedVideoUri, filePathColumn, null, null, null);  
          
        cursor.moveToFirst();  
  
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);  
        filePath = cursor.getString(columnIndex);  
        cursor.close();  
        return filePath;  
    }  
    private static Bitmap reSizeBitmap(Bitmap bmp) {
    	//超過4096要縮小
    	int width = bmp.getWidth();
    	int height = bmp.getHeight();
    	Matrix matrix = new Matrix();
    	Bitmap resizedBitmap;
    	if (width>height)
    	{
    		matrix.postScale(3900/(float)width, 3900/(float)width);
    		resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

    	}else
    	{
    		matrix.postScale(3900/(float)height, 3900/(float)height);
    		resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
    	}
    	bmp.recycle();
    	System.gc();
    	
    	return resizedBitmap;    	
    }


}
