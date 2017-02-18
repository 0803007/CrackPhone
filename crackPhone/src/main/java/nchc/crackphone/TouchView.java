package nchc.crackphone;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class TouchView extends ImageView {

    static final int NONE = 0;
    static final int DRAG = 1; //拖動中
    static final int ZOOM = 2; //縮放中
    static final int BIGGER = 3; //放大ing
    static final int SMALLER = 4; //縮小ing
    private int mode = NONE; //當前的事件
   
    private float beforeLenght; //兩觸點距離
    private float afterLenght; //兩觸點距離
    private float scale = 0.04f; //縮放的比例XY方向都是這個值越大縮放的越快
    
    private int screenW;
    private int screenH;
    public int mImageWidth = 0;
    public int mImageHeight = 0;

    /*處理拖動 變量 */
    private int start_x;
    private int start_y;
    private int stop_x ;
    private int stop_y ;
    
    Vibrator vibrator;
    public float crackstart_x = 0;
    public float crackstart_y = 0;
    public float crackend_x = 0;
    public float crackend_y = 0;
	public float mLastMotionX = 0f;
	public float mLastMotionY = 0f;
    
    /*長按
	private boolean mIsLongPressed = false;
	private float mLastMotionX = 0f;
	private float mLastMotionY = 0f;
	private long lastDownTime = 0;
	private long eventTime = 0;*/
    boolean isLongClickModule = false;
    boolean isTouchOne = false;
    boolean isStartPoint = true;
    
    //裂縫的結果
	public PointF mInterpPTLeft = new PointF(0.0f,0.0f);
	public PointF mInterpPTRight = new PointF(0.0f,0.0f);
	ArrayList <PointF> vecSemiAutoBounUpPoint;
	ArrayList <PointF> vecSemiAutoBounDownPoint;
	ArrayList <PointF> vecSemiAutoNodePoint;
	ArrayList <Double> vecSemiAutoNodeWidth;
    List<MatOfPoint> vecContoursList = null;
    ArrayList <RotatedRect> vecRotatedRectContours = null;
	
	//紅點偵測的點做標
	ArrayList <PointF> vecLaserPointList;
    boolean isLaserPointMode = false;
    int nLaserPointIndex = 0;
    boolean isDLTMode = false;
    boolean isGridMode = false;
    float fScale = 1;
    
	//其他雜物
	public TextView mPosText;
    Timer timer;
    private Paint paint;//聲明畫筆
    private TranslateAnimation trans; //處理超出邊界的動畫
    Matrix mNewTrx = new Matrix();
    
    /**
     * 默認構造函數
     * @param context
     */
	public TouchView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    /**
     * 該構造方法在靜態引入XML文件中是必須的
     * @param context
     * @param paramAttributeSet
     */
    public TouchView(Context context,AttributeSet paramAttributeSet){
        super(context, paramAttributeSet);
        /*super.setLongClickable(true);
        super.setClickable(true);
        super.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				vibrator.vibrate(1000);
				return false;
			}});*/
    }
    public void myInvalidate(){
    	this.invalidate();
    }
    @Override
	protected boolean setFrame(int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		return super.setFrame(l, t, r, b);
	}
	/**
     * 該構造函數在動態創建時，指定圖片的初始高寬
     * @param context
     * @param w
     * @param h
     */
    public TouchView(Context context,int w,int h) {
        super(context);
        
        this.setPadding(0, 0, 0, 0);
        screenW = w;
        screenH = h;
    }
    /**
     * 就算兩點間的距離
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    
    /**
     * 處理觸碰..
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
   
    	switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
        	
                mode = DRAG;

                stop_x = (int) event.getRawX();
                stop_y = (int) event.getRawY();
                start_x = (int) event.getX();
                //start_y = (int) event.getY();
                start_y = stop_y - this.getTop();
                if(event.getPointerCount()==2)
                    beforeLenght = spacing(event);
               
                mNewTrx.set(this.getImageMatrix());
                mNewTrx.invert(mNewTrx);
                
	            mLastMotionX = event.getX();
	            mLastMotionY = event.getY();
	            
                /*長按
	            mLastMotionX = event.getX();
	            mLastMotionY = event.getY();
	            lastDownTime = event.getEventTime() ;*/
                timer = new Timer();
                isLongClickModule = true;
                
                timer.schedule(new TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
			            if (isLongClickModule){
			            	//長按模式下做的事情
			            	vibrator.vibrate(100);
			            	
	            			float pts [] = {mLastMotionX, mLastMotionY};       			
	            			mNewTrx.mapPoints(pts);
	            			
			            	if (isLaserPointMode)
			            	{	            			
		            			vecLaserPointList.set(nLaserPointIndex, new PointF(pts[0],pts[1]));
			            	}
			            	else
			            	{
			            		if (isStartPoint){	
			            			crackstart_x = (int)pts[0];
			            			crackstart_y = (int)pts[1];

			            			isStartPoint = false;
			            		}
			            		else{
			            			crackend_x = (int)pts[0];
			            			crackend_y = (int)pts[1];

			            			isStartPoint = true;
			            		}
			            	}
			            	postInvalidate();
			            	isTouchOne = true;
			            }
					}}, 1000);
    			float pts [] = {event.getX(), event.getY()};
    			mNewTrx.mapPoints(pts);
    		
    			int pixel = 0;
    	        int redValue = 0;
    	        int blueValue = 0;
    	        int greenValue = 0;
    			Bitmap bitmap = ((BitmapDrawable)this.getDrawable()).getBitmap();
    			if (0<=pts[0]&&pts[0]<bitmap.getWidth()&&0<=pts[1]&&pts[1]<bitmap.getHeight())
    			{
    				pixel = bitmap.getPixel((int)pts[0],(int)pts[1]);
    				redValue = Color.red(pixel);
    				blueValue = Color.blue(pixel);
    				greenValue = Color.green(pixel);
    			}

    			String str = "(X,Y)=("+(int)pts[0] + ","+ (int)pts[1] + "),RGB=(" + redValue + ","+ blueValue +","+ greenValue + ")";
                //String str = (int)event.getX() + ","+ (int)event.getY();
                mPosText.setText(str);

                invalidate();
                break;
        case MotionEvent.ACTION_POINTER_DOWN:
                if (spacing(event) > 10f) {
                    mode = ZOOM;
                    beforeLenght = spacing(event);
                }
                break;
        case MotionEvent.ACTION_UP:
            /*判斷是否超出範圍 並處理*/
        	/*
                int disX = 0;
                int disY = 0;
                if(getHeight()<=screenH || this.getTop()<0)
                {
                    if(this.getTop()<0 )
                    {
                        int dis = getTop();
                        this.layout(this.getLeft(), 0, this.getRight(), 0 + this.getHeight());
                        disY = dis - getTop();
                    }
                    else if(this.getBottom()>screenH)
                    {
                        disY = getHeight()- screenH+getTop();
                        this.layout(this.getLeft(), screenH-getHeight(), this.getRight(), screenH);
                    }
                }
                if(getWidth()<=screenW)
                {
                    if(this.getLeft()<0)
                    {
                        disX = getLeft();
                        this.layout(0, this.getTop(), 0+getWidth(), this.getBottom());
                    }
                    else if(this.getRight()>screenW)
                    {
                        disX = getWidth()-screenW+getLeft();
                        this.layout(screenW-getWidth(), this.getTop(), screenW, this.getBottom());
                    }
                }
                if(disX!=0 || disY!=0)
                {
                    trans = new TranslateAnimation(disX, 0, disY, 0);
                    trans.setDuration(500);
                    this.startAnimation(trans);
                }
                mode = NONE;*/
             	/*長按
        	    mIsLongPressed = false;*/
        	    isLongClickModule = false;
                break;
        case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        case MotionEvent.ACTION_MOVE:
        	    /*長按
                float x = event.getX();
                float y = event.getY();
        	    eventTime = event.getEventTime();
        	    if (!mIsLongPressed){
        	         mIsLongPressed = isLongPressed(mLastMotionX, mLastMotionY, x, y, lastDownTime,eventTime,500);
              	}
        	    if (mIsLongPressed){
	        	    //長按模式下做的事情
	        	    vibrator.vibrate(1000);
        	    }
        	    else
        	    {}*/
        	    //double deltaX = Math.sqrt((event.getX() - start_x) * (event.getX() - start_x) + (event.getY() - start_y) * (event.getY() - start_y));
        	double deltaX = Math.sqrt((event.getX() - mLastMotionX) * (event.getX() - mLastMotionX) + (event.getY() - mLastMotionY) * (event.getY() - mLastMotionY));
        	    if (deltaX > 20 && timer != null) { // 移動大於5個pixel
            		timer.cancel();
            		timer = null;
            		isLongClickModule = false;
            	}
   	
                /*處理拖動*/
                if (mode == DRAG) {
                    if (Math.abs(stop_x-start_x-getLeft())<200 && Math.abs(stop_y - start_y-getTop())<200)//88,85
                    {
                        //this.setPosition(stop_x - start_x, stop_y - start_y, stop_x + this.getWidth() - start_x, stop_y - start_y + this.getHeight());
                        Matrix mtrx = this.getImageMatrix();
                        mtrx.postTranslate((int) event.getRawX()- stop_x , (int) event.getRawY() - stop_y);
                    	//mNewTrx.set(mtrx);
                    	
                        stop_x = (int) event.getRawX();
                        stop_y = (int) event.getRawY();
                    }
                }
                /*處理縮放*/
                else if (mode == ZOOM) {
                    if (spacing(event)>10f)
                    {
                        afterLenght = spacing(event);
                        float gapLenght = afterLenght - beforeLenght;
                        if(gapLenght == 0) {
                           break;
                        }
                        else if(Math.abs(gapLenght)>5f)
                        {
                            if(gapLenght>0) {  
                                this.setScale(scale,BIGGER);
 
                            }else {
                                this.setScale(scale,SMALLER);
                                
                            }
                            beforeLenght = afterLenght;
                        }
                    }
                }     
                /*
                String str2 = (int)event.getX() + ","+ (int)event.getX();
                mPosText.setText(str2);*/
                postInvalidate();
                break;
        }
        return true;
    }
    /**
     * 實現適合視窗大小,針對高即可
     */
    public void setScaleFitWin() {
        //縮小影像比例
        float yScale;
        float screenwidth = (float)this.getWidth();
        float screenheight = (float)this.getHeight();
        yScale = screenheight/(float)mImageHeight;

        //先縮小影像
        Matrix mtrx = this.getImageMatrix();
        mtrx.postScale(yScale, yScale, 0, 0);

        //平移影像
        float xShift = (screenwidth - (float)mImageWidth*yScale)/2;
        float yShift = (screenheight - (float)mImageHeight*yScale)/2;
        float []pts = new float[]{0,0};
        float []dst = new float[]{0,0};
        mtrx.mapPoints(dst, pts);//影像座標換算成螢幕座標
        mtrx.postTranslate(xShift - dst[0], yShift - dst[1]);
    }
    /**
     * 實現處理縮放
     */
    private void setScale(float temp,int flag) {
        /*    
        if (flag==BIGGER) {
            this.setFrame(this.getLeft()-(int)(temp*this.getWidth()),
                          this.getTop()-(int)(temp*this.getHeight()),
                          this.getRight()+(int)(temp*this.getWidth()),
                          this.getBottom()+(int)(temp*this.getHeight()));
        	
            scaler = scaler + 2*scale;
        }else if (flag==SMALLER){
            this.setFrame(this.getLeft()+(int)(temp*this.getWidth()),
                          this.getTop()+(int)(temp*this.getHeight()),
                          this.getRight()-(int)(temp*this.getWidth()),
                          this.getBottom()-(int)(temp*this.getHeight()));
            scaler = scaler - 2*scale; 
        }*/
    	Matrix mtrx = this.getImageMatrix(); 
    	if (flag==BIGGER) {
        	mtrx.postScale(1.05f,1.05f,stop_x,stop_y);
        	//mNewTrx.set(mtrx);
    	}else if (flag==SMALLER){
        	mtrx.postScale(0.95f,0.95f,stop_x,stop_y);	
        	//mNewTrx.set(mtrx);
    	}

    }
       
    /**
     * 實現處理拖動
     */
    private void setPosition(int left,int top,int right,int bottom) {
        this.layout(left,top,right,bottom);
    }
    /**
     *長按的函式
     */
    private boolean isLongPressed(float lastX, float lastY, float thisX,float thisY, long lastDownTime,long thisEventTime, long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <=10 && offsetY<=10 && intervalTime >= longPressTime){
            return true;
        }
        return false;
    }
    @Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
    	super.onDraw(canvas);
    	canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG, 0));

        if (isDLTMode == false)
        	drawLaserNode(vecLaserPointList, canvas, Color.BLUE);

        drawRotatedRect(canvas);
        //drawContours(canvas);
    	/*
    	if (isDLTMode)
    	    drawScaler(1000, 1,canvas);
    	*/
    	if (isGridMode)
    	    drawGrid(canvas);
    	
    	if (isTouchOne){
    		drawTouchRect(canvas);
    		drawInterpPT(mInterpPTLeft, mInterpPTRight, canvas);
    		drawNode(vecSemiAutoNodePoint, canvas);
    		//drawBoundary(vecSemiAutoBounUpPoint,vecSemiAutoBounDownPoint,canvas);
    	}
	}
	/**
     * 默認構造函數
     * @param
     * @return 
     */
	public void setVibrator(Vibrator vibrator) {
		this.vibrator = vibrator;
	}
	
	public void drawBoundary(ArrayList <PointF> up, ArrayList <PointF> down, Canvas canvas) {
		if (up == null || down == null)
			return;
		
		//設置畫筆
		paint = new Paint();
		paint.setStrokeWidth(5);//筆寬5圖元
		paint.setStyle(Paint.Style.STROKE);//空心  
		paint.setColor(Color.YELLOW);//設置為紅筆
		paint.setAntiAlias(true);//鋸齒不顯示
		//座標轉換+畫上邊界
		Matrix mtrx = this.getImageMatrix();
		float[] pts = new float[] {0, 0};
		float[] dst = new float[] {0, 0};
		for (int i=0;i<up.size();i++)
		{
			 pts[0] = up.get(i).x;
			 pts[1] = up.get(i).y;
			 //座標轉換
			 mtrx.mapPoints(dst, pts);
			 //畫圖
			 canvas.drawLine(dst[0]-5, dst[1]-5, dst[0]+5, dst[1]+5, paint);
			 canvas.drawLine(dst[0]+5, dst[1]-5, dst[0]-5, dst[1]+5, paint);
		}		
		//座標轉換+畫下邊界
		mtrx = this.getImageMatrix();
		paint.setColor(Color.RED);//設置為紅筆
		float[] pts2 = new float[] {0, 0};
		float[] dst2 = new float[] {0, 0};
		for (int i=0;i<down.size();i++)
		{
			 pts2[0] = down.get(i).x;
			 pts2[1] = down.get(i).y;
			 //座標轉換
			 mtrx.mapPoints(dst2, pts2);
			 //畫圖
			 canvas.drawLine(dst2[0] - 5, dst2[1] - 5, dst2[0] + 5, dst2[1] + 5, paint);
			 canvas.drawLine(dst2[0] + 5, dst2[1] - 5, dst2[0] - 5, dst2[1] + 5, paint);
		}	
	}
    /**
     * 繪製有角度的Rect
     * @param
     * @return
     */
    public void drawRotatedRect(Canvas canvas) {
        if (vecRotatedRectContours == null)
            return;
        //設置畫筆
        paint = new Paint();
        paint.setStrokeWidth(2);//筆寬5圖元
        paint.setStyle(Paint.Style.STROKE);//空心
        paint.setColor(Color.RED);//設置為紅筆
        paint.setAntiAlias(true);//鋸齒不顯示

        for (int i=0;i<vecRotatedRectContours.size();i++)
        {
            org.opencv.core.Point [] rect_points = new org.opencv.core.Point[4];
            vecRotatedRectContours.get(i).points(rect_points);

            //座標轉換
            Matrix mtrx = this.getImageMatrix();
            float[] pts = new float[]{(float)rect_points[0].x, (float)rect_points[0].y,
                                      (float)rect_points[1].x, (float)rect_points[1].y,
                                      (float)rect_points[1].x, (float)rect_points[1].y,
                                      (float)rect_points[2].x, (float)rect_points[2].y,
                                      (float)rect_points[2].x, (float)rect_points[2].y,
                                      (float)rect_points[3].x, (float)rect_points[3].y,
                                      (float)rect_points[3].x, (float)rect_points[3].y,
                                      (float)rect_points[0].x, (float)rect_points[0].y};
            float[] dst = new float[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mtrx.mapPoints(dst, pts);
            canvas.drawLines(dst, paint);
        }

    }
	public void drawTouchRect(Canvas canvas) {
		//設置畫筆
		paint = new Paint();
		paint.setStrokeWidth(5);//筆寬5圖元
		paint.setStyle(Paint.Style.STROKE);//空心  
		paint.setColor(Color.RED);//設置為紅筆
		paint.setAntiAlias(true);//鋸齒不顯示
        //座標轉換
		Matrix mtrx = this.getImageMatrix(); 
		float[] pts = new float[]{crackstart_x, crackstart_y, crackend_x, crackend_y};
		float[] dst = new float[] {0, 0, 0, 0};
		mtrx.mapPoints(dst, pts);
		//畫上框
		if (crackend_x!=0 || crackend_y!=0)  //End點等於0時不畫
			canvas.drawLine(dst[0], dst[1], dst[2], dst[3], paint);

		if (dst[0] > 7 && dst[1] > 7)
			canvas.drawRect(dst[0]-7, dst[1]-7, dst[0]+7, dst[1]+7, paint);

		if (dst[2] > 7 && dst[3] > 7)
			canvas.drawRect(dst[2]-7, dst[3]-7, dst[2]+7, dst[3]+7, paint);
	}
	public void drawInterpPT(PointF left,PointF right, Canvas canvas) {
		//設置畫筆
		paint = new Paint();
		paint.setStrokeWidth(5);//筆寬5圖元
		paint.setStyle(Paint.Style.STROKE);//空心  
		paint.setColor(Color.YELLOW);//設置為紅筆
		paint.setAntiAlias(true);//鋸齒不顯示
		//座標轉換
		Matrix mtrx = this.getImageMatrix(); 
		float[] pts =new float[]{left.x+0.5f, left.y+0.5f, right.x+0.5f, right.y+0.5f};  //校正回pixel的正中央,不影響寬度值
		float[] dst = new float[] {0, 0, 0, 0};
		mtrx.mapPoints(dst, pts);
		//畫上XX
		canvas.drawLine(dst[0]+5, dst[1]-5, dst[0]-5, dst[1]+5, paint);
		canvas.drawLine(dst[0]-5, dst[1]-5, dst[0]+5, dst[1]+5, paint);
		canvas.drawLine(dst[2]-5, dst[3]-5, dst[2]+5, dst[3]+5, paint);
		canvas.drawLine(dst[2]+5, dst[3]-5, dst[2]-5, dst[3]+5, paint);
	}
	public void drawNode(ArrayList <PointF> nodePoint, Canvas canvas) {
		if (nodePoint == null)
			return;
		
		//設置畫筆
		paint = new Paint();
		paint.setStrokeWidth(5);//筆寬5圖元
		paint.setStyle(Paint.Style.STROKE);//空心  
		paint.setColor(Color.GREEN);//設置為紅筆
		paint.setAntiAlias(true);//鋸齒不顯示
		//座標轉換+畫圖
		Matrix mtrx = this.getImageMatrix();
		float[] pts = new float[] {0, 0};
		float[] dst = new float[] {0, 0};
		for (int i=0;i<nodePoint.size();i++)
		{
			 pts[0] = nodePoint.get(i).x;
			 pts[1] = nodePoint.get(i).y;
			 //座標轉換
			 mtrx.mapPoints(dst, pts);
			 //畫圖
			 canvas.drawLine(dst[0]-5, dst[1]-5, dst[0]+5, dst[1]+5, paint);
			 canvas.drawLine(dst[0]+5, dst[1]-5, dst[0]-5, dst[1]+5, paint);
		}
	}
	public void drawLaserNode(ArrayList <PointF> nodePoint, Canvas canvas, int color) {	
		if (nodePoint == null)
			return;
	
		//設置畫筆
		paint = new Paint();
		paint.setStrokeWidth(3);//筆寬5圖元
		paint.setStyle(Paint.Style.STROKE);//空心  
		paint.setColor(Color.BLUE);//設置為紅筆
		paint.setAntiAlias(true);//鋸齒不顯示
		
		//設置字
		Paint paintWord = new Paint();
		paintWord.setTextSize(54);//設定字體大小
		paintWord.setStrokeWidth(3);//筆寬5圖元
		paintWord.setStrokeWidth(24);//筆寬24圖元
		paintWord.setColor(color);//設置為紅筆

		//座標轉換+畫圖
		Matrix mtrx = this.getImageMatrix();
		float[] pts = new float[] {0, 0};
		float[] dst = new float[] {0, 0};
		for (int i=0;i<nodePoint.size();i++)
		{
			pts[0] = nodePoint.get(i).x;
			pts[1] = nodePoint.get(i).y;
			//座標轉換
			mtrx.mapPoints(dst, pts);
			//畫圖
			canvas.drawLine(dst[0]-8, dst[1]-8, dst[0]+8, dst[1]+8, paint);
			canvas.drawLine(dst[0]+8, dst[1]-8, dst[0]-8, dst[1]+8, paint);
			//如果有四個 畫上ABCD
			if (nodePoint.size() == 4)
			{
				if (i==0)
				    canvas.drawText("A", dst[0]-20, dst[1]-20, paintWord); 	
				if (i==1)
				    canvas.drawText("B", dst[0]-20, dst[1]-20, paintWord); 
				if (i==2)
				    canvas.drawText("C", dst[0]-20, dst[1]-20, paintWord); 
				if (i==3)
				    canvas.drawText("D", dst[0]-20, dst[1]-20, paintWord); 
				
			}
		}
	}
	public void drawScaler(int length, float scale, Canvas canvas) {
		//畫一個10cm*10cm的比例尺
		//設置畫筆
		paint = new Paint();
		paint.setStrokeWidth(5);//筆寬5圖元
		paint.setStyle(Paint.Style.STROKE);//空心  
		paint.setColor(Color.RED);//設置為紅筆
		paint.setAntiAlias(true);//鋸齒不顯示
		//設置字
		Paint paintWord = new Paint();
		paintWord.setTextSize(36);//設定字體大小
		paintWord.setStrokeWidth(3);//筆寬5圖元
		paintWord.setStrokeWidth(24);//筆寬24圖元
		paintWord.setColor(Color.BLUE);//設置為紅筆
		//設置轉換矩陣
		Matrix mtrx = this.getImageMatrix();
		PointF xstart = new PointF(mImageWidth/7,mImageHeight/7*6);
		PointF xend = new PointF(mImageWidth/7 + length,mImageHeight/7*6);
		float[] pts = new float[] {xstart.x, xstart.y, xend.x, xend.y};
		float[] dst = new float[] {0, 0, 0, 0};
		mtrx.mapPoints(dst, pts);
		//畫圖
		canvas.drawLine(dst[0], dst[1], dst[2], dst[3], paint);//主體
		canvas.drawLine(dst[0], dst[1]-10, dst[0], dst[1]+10, paint);//頭端
		canvas.drawLine(dst[2], dst[3]-10, dst[2], dst[3]+10, paint);//尾端
		//寫字
		canvas.drawText("100mm", dst[0], dst[1]-10, paintWord); 	
		
	}
    public void drawContours(Canvas canvas) {
        if (vecContoursList == null)
            return;
        //設置畫筆
        paint = new Paint();
        paint.setStrokeWidth(1);//筆寬5圖元
        paint.setStyle(Paint.Style.STROKE);//空心
        paint.setColor(Color.RED);//設置為紅筆
        paint.setAntiAlias(true);//鋸齒不顯示

        //設置轉換矩陣
        Matrix mtrx = this.getImageMatrix();

        //trace every contour
        for (int i = 0; i < vecContoursList.size(); i++)
        {
            MatOfPoint contours =  vecContoursList.get(i);
            org.opencv.core.Point[] points_contour = contours.toArray();

            float[] pts = new float[points_contour.length*2];
            float[] dst = new float[points_contour.length*2];

            for (int j =0; j < points_contour.length; j++)
            {
                pts[j*2] = (float)points_contour[j].x;
                pts[j*2+1] = (float)points_contour[j].y;
            }
            //pts[(points_contour.length)*2] = (float)points_contour[0].x;
            //pts[(points_contour.length)*2+1] = (float)points_contour[0].y;
            //座標轉換
            mtrx.mapPoints(dst, pts);
            //畫圖
            if (points_contour.length>2)
               canvas.drawLines(dst,paint);

            //release
            pts = null;
            dst = null;
        }
    }
	public void drawGrid(Canvas canvas) {
		//設置畫筆
		paint = new Paint();
		paint.setStrokeWidth(1);//筆寬5圖元
		paint.setStyle(Paint.Style.STROKE);//空心  
		paint.setColor(Color.RED);//設置為紅筆
		paint.setAntiAlias(true);//鋸齒不顯示
		//設置轉換矩陣
		Matrix mtrx = this.getImageMatrix();
		
		//畫直線
		for (int i=0;i<mImageWidth;i++)
		{
			PointF xstart = new PointF(i,0);
			PointF xend = new PointF(i,mImageHeight);
			float[] pts = new float[] {xstart.x, xstart.y, xend.x, xend.y};
			float[] dst = new float[] {0, 0, 0, 0};
			
			mtrx.mapPoints(dst, pts);
			canvas.drawLine(dst[0], dst[1], dst[2], dst[3], paint);//主體
		}
		//畫橫線
		for (int j=0;j<mImageHeight;j++)
		{
			PointF xstart = new PointF(0,j);
			PointF xend = new PointF(mImageWidth,j);
			float[] pts = new float[] {xstart.x, xstart.y, xend.x, xend.y};
			float[] dst = new float[] {0, 0, 0, 0};
			
			mtrx.mapPoints(dst, pts);
			canvas.drawLine(dst[0], dst[1], dst[2], dst[3], paint);//主體
		}
		
	}

}
