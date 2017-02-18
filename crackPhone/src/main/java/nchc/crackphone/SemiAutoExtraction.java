package nchc.crackphone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.graphics.Point;
import android.graphics.PointF;

public class SemiAutoExtraction {
	public double m_fpNodeLen;
	public double m_fpNormalLen;
	public int m_nSmoothPara;
	public int m_nProfileHeight;
	public Mat image;
	
	public PointF startPoint = new PointF(0,0);
	public PointF endPoint = new PointF(0,0);
	public PointF interpPoint = new PointF(0,0);
	
	private ArrayList <PointF> vecNormalPoint = new ArrayList ();
	
	public ArrayList <PointF> vecBounUpPoint = new ArrayList ();
	public ArrayList <PointF> vecBounDownPoint = new ArrayList ();
	public ArrayList <PointF> vecNodePoint = new ArrayList ();	
	public ArrayList <Double> vecNodeWidth = new ArrayList ();

	public double M;

	public SemiAutoExtraction() {

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
    /**
     * 線性內差法
     * @param start 起始點
     * @param end 終點 
     * @param length 內差的長度
     * @return 回傳內差的點
     */
	public PointF lineInterp(PointF start, PointF end, double length) {
		/*
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
		return new PointF((float)x,(float)y);*/
		if (length == 0)
			return start;

		double dist = calDist(start, end);
		if (dist==0)
			return start;
			//dist=length;

		double x = 0.0f;
		double y = 0.0f;

		x = start.x + (end.x -start.x)*length/dist;
		y = start.y + (end.y -start.y)*length/dist;

		return new PointF((float)x,(float)y);
	}
	/**
     * 用點協式找法向量 y-b = m(x-a)
     * [fx,fy] 找到的法向量點
     * @param point 點
     * @param M slope 斜率 
     * @param length 所要的法向量長度
     * @return 回傳點
     */
	public ArrayList <PointF> getNormal(PointF point, double M, double length) {
		double dist = 0;
		ArrayList  <PointF> f = new ArrayList ();;

		double a = point.x;
		double b = point.y;
		double d = 0;

		//四種有可能的情形都要考慮
		if (0 <= Math.abs(M)&& Math.abs(M)<=1)
		{
			while (dist < length)
			{
				//第一種
				double x = a - d;
				double y = M * (x - a) + b;
				dist = calDist(new PointF((float)a,(float)b),new PointF((float)x,(float)y));
				f.add(0,new PointF((float)x,(float)y));//插入到第一排
				//fx = [x;fx];
				//fy = [y;fy];
				//第二種
				x = a + d;
				y = M * (x - a) + b;
				dist = calDist(new PointF((float)a,(float)b),new PointF((float)x,(float)y));
				f.add(new PointF((float)x,(float)y));
				//fx = [fx;x];
				//fy = [fy;y];

				d = d + 1;
			}
		}
		else
		{
			while (dist < length)
			{
				//第三種
				double y = b - d;
				double x = (y - b)/M + a;
				dist = calDist(new PointF((float)a,(float)b),new PointF((float)x,(float)y));
				f.add(0,new PointF((float)x,(float)y));
				//fx = [x;fx];
				//fy = [y;fy];
				//第四種
				y = b + d;
				x = (y - b)/M + a;
				dist = calDist(new PointF((float)a,(float)b),new PointF((float)x,(float)y));
				f.add(new PointF((float)x,(float)y));
				//fx = [fx;x];
				//fy = [fy;y];

				d = d + 1;
			}
		}
		return f;
	}
	/**
	 * 內差至子pixel
	 * @param arr 園史影像
	 * @param loc pixel位置
	 * @return pixel位置
	 */
	public PointF getSubpixel(Mat arr, Point loc){
	    //x方向的subpixel
		double []if1;
		double []f0;
		double []f1;
		double dx,dy;
	    PointF cp =new PointF(0.0f,0.0f);
		if(loc.x-1 < 0 || loc.x+1>image.width())                  //判斷超出邊界
		{
			cp.x =  loc.x;
		}else
		{
			if1 = arr.get(loc.y, loc.x-1);
			f0 = arr.get(loc.y, loc.x);
			f1 = arr.get(loc.y, loc.x+1);
				
		    dx = (f1[0]-if1[0])/(2*(2*f0[0]-if1[0]-f1[0]));
	        if(dx < -1 || dx > 1)                                                   //超出一個pixel歸0
	            dx = 0;

	        cp.x = (float) (loc.x + dx);
		}

		//y方向的subpixel
		if(loc.y-1 < 0 || loc.y+1 > arr.height())               //判斷超出邊界
		{
			cp.y =  loc.y;
		}
		else
		{
			if1 = arr.get(loc.y-1, loc.x);
			f0 = arr.get(loc.y, loc.x);
			f1 = arr.get(loc.y+1, loc.x);
			
	        dy = (f1[0]-if1[0])/(2*(2*f0[0]-if1[0]-f1[0]));
	        if(dy < -1 || dy > 1)                                                 //超出一個pixel歸0
	            dy = 0;

	        cp.y = (float) (loc.y + dy);
		}
	    return cp;
	}
	/**
	 * 計算此裂縫的長度
	 * @param vecPoint 矩陣點
	 * @return 裂縫長度
	 */
	public double getCrackLength(ArrayList  <PointF> vecPoint){
		double length = 0;
		double dist;
		PointF s,e;
		int num = vecPoint.size();
		for (int i = 0; i < num-1; i++)
		{
			s =  vecPoint.get(i);
			e =  vecPoint.get(i+1);
			dist = Math.sqrt(Math.pow((s.x-e.x),2)+Math.pow((s.y-e.y),2));
			length = length + dist;
		}
		return length;
		
	}
	/**
	 * 執行辦自動偵測
	 */
	public void run(){

		//---- 初始參數----
		if (vecNodePoint.size() != 0)
			vecNodePoint.clear();

		if (vecBounUpPoint.size() != 0)
			vecBounUpPoint.clear();

		if (vecBounDownPoint.size() != 0)
			vecBounDownPoint.clear();

		if (vecNodeWidth.size() != 0)
	        vecNodeWidth.clear();


		//m_fpNodeLen = 30;
		//m_fpNormalLen = 30;
		int m_nMaxNodePointNum = 1000;

		//------------求Node點-----------
		//內差位置
		interpPoint = lineInterp(startPoint,endPoint,m_fpNodeLen);
		//求斜率M
		if (endPoint.x == startPoint.x)
			M = -100.0;//垂直的斜率M為無線小
		else if (endPoint.y == startPoint.y)
			//M = 100.0;//水平的斜率M為無限大 不給0
			M = (endPoint.y - startPoint.y + 0.01) / (endPoint.x - startPoint.x);
		else
			M = (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x);

		//找法向量
		if (vecNormalPoint.size() != 0)
			vecNormalPoint.clear();

		if (M == 0)
			M = -100.0;
		vecNormalPoint = getNormal(interpPoint ,-1/M,m_fpNormalLen);

		//從法向量座標取的pixel
		Mat array =  new Mat(1, vecNormalPoint.size(), CvType.CV_8UC1);
		Mat array_smooth = new Mat(1, vecNormalPoint.size(), CvType.CV_8UC1);
		
		//IplImage * array = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
		//IplImage * array_smooth = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
		int num = vecNormalPoint.size();
		for (int i = 0; i < num ; i++)
		{
			int x = (int) vecNormalPoint.get(i).x;
			int y = (int) vecNormalPoint.get(i).y;
			if (x<0 || y<0 || x>image.width() || y>image.height())  //超出影像範圍
	        {
				array.release();
				array_smooth.release();
				//cvReleaseImage(&array);
	        	//cvReleaseImage(&array_smooth);
				//ShowMessage("Outside the Image!!");
				return;
	        }
			//**************下面有可能出錯*****************
			double [] temp = image.get(y-1, x-1);
	        //int temp = CV_IMAGE_ELEM(image,uchar,y-1,x-1);

			array.put(0,i,temp);
			//CV_IMAGE_ELEM(array,uchar,0,i) = temp;
		}
		//找出peak點
		//cvSmooth(array, array_smooth, CV_MEDIAN,3,0);
		Point minLoc = new Point(0,0);
		Point maxLoc = new Point(0,0);
		
		MinMaxLocResult mmr = Core.minMaxLoc(array);
		minLoc.x = (int) mmr.minLoc.x;
		minLoc.y = (int) mmr.minLoc.y;	
		//cvMinMaxLoc(array,NULL,NULL,&minLoc,NULL,NULL);
		
		PointF pt = vecNormalPoint.get(minLoc.x);
		vecNodePoint.add(startPoint);
		vecNodePoint.add(pt);

		double end_dist = calDist(pt,endPoint);

		//清除宣告的
		array.release();
		array_smooth.release();
		//cvReleaseImage(&array);
		//cvReleaseImage(&array_smooth);

		//循環至Node點至End點長度小於Node長
		while (end_dist > m_fpNodeLen)
		{
		   //內差位置
		   interpPoint = lineInterp(startPoint, pt, m_fpNodeLen + calDist(startPoint, pt));
		   //內插的位置超出影像範圍就跳出
		   if (interpPoint.x<0 || interpPoint.y<0 || interpPoint.x>image.width()-1 || interpPoint.y>image.height()-1)  //超出影像範圍
		   {
			   //ShowMessage("Node Outside the Image!!");
			   break;
		   }
		   //Node數量超出設定的m_nMaxModePointNum 跳出迴圈
			if (vecNodePoint.size()>m_nMaxNodePointNum)
				break;

		   //求斜率M
		   if (endPoint.x == pt.x)
			   M = -100.0;//垂直的斜率M為無限小
		   else if (endPoint.y == pt.y)
			   //M = 100.0;//水平的斜率M為無限大 不給0
	           M = (endPoint.y - pt.y + 0.01) / (endPoint.x - pt.x);
		   else
			   M = (endPoint.y - pt.y) / (endPoint.x - pt.x);

		   //找法向量
		   if (vecNormalPoint.size() != 0)
			   vecNormalPoint.clear();

		   if (M == 0)
	           M = -100.0;
		   vecNormalPoint = getNormal(interpPoint,-1/M,m_fpNormalLen);

			//從法向量座標取的pixel
			array.create(1, vecNormalPoint.size(), CvType.CV_8UC1);
			array_smooth.create(1, vecNormalPoint.size(), CvType.CV_8UC1);
			//IplImage * array = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
			//IplImage * array_smooth = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
			num = vecNormalPoint.size();
			for (int i = 0; i < num ; i++)
			{
				int x = (int) vecNormalPoint.get(i).x;
				int y = (int) vecNormalPoint.get(i).y;
				if (x<0 || y<0 || x>image.width() || y>image.height())  //超出影像範圍
				{
					array.release();
					array_smooth.release();
					//cvReleaseImage(&array);
			        //cvReleaseImage(&array_smooth);
					//ShowMessage("Normal Outside the Image!!");
	                return;
	            }
				//**************下面有可能出錯*****************
				double []temp = image.get(y-1, x-1); 
				//int temp = CV_IMAGE_ELEM(image,uchar,y-1,x-1);
			    array.put(0,i,temp);
				//CV_IMAGE_ELEM(array,uchar,0,i) = temp;
			}
		   //找出peak點
		   MinMaxLocResult mmr2 = Core.minMaxLoc(array);
		  //cvMinMaxLoc(array,NULL,NULL,&minLoc,NULL,NULL);
		   minLoc.x = (int) mmr2.minLoc.x;
		   minLoc.y = (int) mmr2.minLoc.y;
		   pt = vecNormalPoint.get(minLoc.x);
		   vecNodePoint.add(pt);

		   end_dist = calDist(pt,endPoint);

		   //清除宣告的
		   array.release();
		   array_smooth.release();
		   //cvReleaseImage(&array);
		   //cvReleaseImage(&array_smooth);

		}
		//vecNodePoint.add(endPoint);
		//------------求邊界------------

			for (int i = 0; i < vecNodePoint.size() - 1; i++) {
				PointF s = vecNodePoint.get(i);
				PointF e = vecNodePoint.get(i + 1);
				PointF up_pt = new PointF(0.0f, 0.0f);
				PointF down_pt = new PointF(0.0f, 0.0f);

				double dist = calDist(s, e);
				//for (int j = 1; j < dist + 1; j += (dist / 2)) {
				//	//內差位置
				//	interpPoint = lineInterp(s, e, j);
				interpPoint.x = (s.x + e.x)/2;
				interpPoint.y = (s.y + e.y)/2;
					//求斜率M
					if (e.x == s.x)
						M = -100.0;//垂直的斜率M為無限小
					else if (e.y == s.y)
						//M = 100.0;//水平的斜率M為無限大 不給0
						M = (e.y - s.y + 0.01) / (e.x - s.x);
					else
						M = (e.y - s.y) / (e.x - s.x);

					//找法向量
					if (vecNormalPoint.size() != 0)
						vecNormalPoint.clear();

					if (M == 0)
						M = -100;

					vecNormalPoint = getNormal(interpPoint, -1 / M, m_nProfileHeight);    //  default:m_fpNodeLen/10

				/*
				//從法向量座標取的pixel
				array.create(1, vecNormalPoint.size(), CvType.CV_8UC1);
				array_smooth.create(1, vecNormalPoint.size(), CvType.CV_8UC1);
				Mat array_diff = new Mat(1, vecNormalPoint.size(), CvType.CV_32FC1);
				//IplImage * array = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
				//IplImage * array_smooth = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_8U,1);
				//IplImage * array_diff = cvCreateImage(cvSize(vecNormalPoint.size(),1),IPL_DEPTH_64F,1);
				num = vecNormalPoint.size();
				for (int k=0; k<num; k++)
				{
					int x = (int) vecNormalPoint.get(k).x;
					int y = (int) vecNormalPoint.get(k).y;
	                if (x<0 || y<0 || x>image.width() || y>image.height())  //超出影像範圍
	                {
	                	array.release();
	                	array_smooth.release();
	                	array_diff.release();
	                	//cvReleaseImage(&array);
	                    //cvReleaseImage(&array_smooth);
	                    //cvReleaseImage(&array_diff);
						//ShowMessage("Normal Outside the Image!!");
	                    return;
	                }
	              //**************下面有可能出錯*****************
	                double [] temp = image.get(y-1,x-1);
					//int temp = CV_IMAGE_ELEM(image,uchar,y-1,x-1);
                    array.put(0,k,temp);
					//CV_IMAGE_ELEM(array,uchar,0,k) = temp;
				}
				//Imgproc.medianBlur(array, array, 5);
				//cvSmooth(array, array, CV_MEDIAN, m_nSmoothPara,0);
				//找出peak點為邊界
				for (int k=0; k<array_diff.width() - 1; k++)
				{
					double [] x1 = array.get(0,k);
					double [] x2 = array.get(0,k+1);
					double [] x3 = {0,0,0};  
					x3[0] = x1[0] - x2[0];
					array_diff.put(0,k,x3);
					//CV_IMAGE_ELEM(array_diff,double,0,k) = (double)CV_IMAGE_ELEM(array,uchar,0,k)
					//									- (double)CV_IMAGE_ELEM(array,uchar,0,k+1);
				}
				double [] x4 = {0,0,0};
				array_diff.put(0, 0, x4);
				array_diff.put(0, array_diff.width()-1, x4);
				//CV_IMAGE_ELEM(array_diff,double,0,0) = 0.0;
				//CV_IMAGE_ELEM(array_diff,double,0,array_diff->width - 1) = 0.0;
				MinMaxLocResult mmr2 = Core.minMaxLoc(array_diff);
				minLoc.x = (int) mmr2.minLoc.x; 
				minLoc.y = (int) mmr2.minLoc.y;
				maxLoc.x = (int) mmr2.maxLoc.x;
				maxLoc.y = (int) mmr2.maxLoc.y;
				//cvMinMaxLoc(array_diff,NULL,NULL,&minLoc,&maxLoc,NULL);
				up_pt = vecNormalPoint.get(maxLoc.x);
				down_pt = vecNormalPoint.get(minLoc.x);
				//up_pt = vecNormalPoint[maxLoc.x];
				//down_pt = vecNormalPoint[minLoc.x];
				*/
				//}
				//從座標Array->Pixel Array
				ArrayList<Double> vecNewLineValue = new ArrayList();
				ArrayList<PointF> vecNewNormalPoint = new ArrayList();
				for (int k = 0; k < vecNormalPoint.size(); k++) {
					int x = (int) vecNormalPoint.get(k).x;
					int y = (int) vecNormalPoint.get(k).y;
					if (x >= 0 && y >= 0 && x < image.width()-1 && y < image.height()-1)  //超出影像範圍
					{
						vecNewLineValue.add(image.get(y, x)[0]);
						vecNewNormalPoint.add(vecNormalPoint.get(k));
						//vecLineValue.add(new PointF(vecNormalPoint.get(k.x, vecNormalPoint.get(k).y));
					}
				}
				double crackwidth = getProfileCrackWidth(vecNewLineValue, vecNewNormalPoint, 0.7f, up_pt, down_pt);

				//扣除掉微小震動 - 1pixel
				crackwidth = crackwidth -1;

				vecNewLineValue.clear();
				vecNewNormalPoint.clear();

				//邊界丟入容器中
				vecBounUpPoint.add(up_pt);
				vecBounDownPoint.add(down_pt);
				vecNodeWidth.add(crackwidth);
				//}
				//寬丟入容器
				//double h = calDist(up_pt,down_pt);
				//vecNodeWidth.add(h);
			}
	}
	static public double  getProfileCrackWidth(ArrayList <Double> vecLineValue, ArrayList  <PointF> vecLinePos, double fSTD, PointF ptLeft, PointF ptRight){
        //取樣前30*10做平均  (10是subpixel)
		ArrayList <Double> vecTotalValue = new ArrayList();
		for (int i=0;i<vecLineValue.size();i++)
			vecTotalValue.add(vecLineValue.get(i));

		Collections.sort(vecTotalValue);
		int endsize = 200*10;
		if (vecTotalValue.size()<200*10)	{
			endsize = vecTotalValue.size();
		}
		if (endsize == 0)
			endsize = 1;

		double total = 0.0f;
		for (int i=0;i<endsize;i++){
			total = total + vecTotalValue.get(i);
		}
		double avg = total / endsize;                 //平均數
		total = 0;
		for (int i = 0; i < endsize; i++)
			total = total + (avg - vecTotalValue.get(i))*(avg - vecTotalValue.get(i));

		double si = Math.sqrt(total / endsize);    //標準差
		//計算裂縫的範圍,兩種方法找到邊界點
		int num = vecLineValue.size();
		int posL = 0;
		int posR = 0;
		//double fSTD = 1.0f;         //預設一個標準差
		//double fSTD = Double.parseDouble(mStdText.getText().toString());
		boolean isMinToBorder = true;

		if (isMinToBorder == true)
		{   //第一種方法:由最低點向外搜尋
			int posMin = 0;
			double fValuse = 255.0;
			for (int i = 1; i < num-1; i++)
			{
				if (vecLineValue.get(i) < fValuse)                  //計算vector中的最小值
				{
					fValuse = vecLineValue.get(i);
					posMin = i;
				}
			}
			for (int i = posMin; i > 1; i--)
			{
				if (vecLineValue.get(i) > vecLineValue.get(posMin) + si*fSTD )     //判斷是否大於N個標準差
				//if (vecLineValue.get(i) > avg - si*fSTD )     //判斷是否大於N個標準差
				{
					posL = i+1;                                 //要加回來
					//posL = i;
					break;
				}
			}
			for (int i = posMin; i < num; i++)
			{
				if (vecLineValue.get(i) > vecLineValue.get(posMin) + si*fSTD )     //判斷是否大於N個標準差
				//if (vecLineValue.get(i) > avg - si*fSTD )     //判斷是否大於N個標準差
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
					posL = i-1;                                 //要減回來
					//posL = i;                                     //一邊撿回來一邊就不用加
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
		//裂縫的長度
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
		double fLenCrack = Math.sqrt((L.x - R.x) * (L.x - R.x) + (L.y - R.y) * (L.y - R.y));

		ptLeft.x = L.x;
		ptLeft.y = L.y;
		ptRight.x = R.x;
		ptRight.y = R.y;

		return fLenCrack;
	}
}
