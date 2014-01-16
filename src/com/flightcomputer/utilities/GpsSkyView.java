package com.flightcomputer.utilities;
import java.util.Iterator;

import com.flightcomputer.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
public class GpsSkyView extends View {
	private static final String TAG = "SPEED";
		// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;	
	private RectF faceRect;
	private Bitmap faceTexture;
	private Paint facePaint;
	private Paint backgroundPaint; 	
	private Bitmap background; 		
	static final int centerDegree = 90; 
	//////////////////////////777
	 private Paint  mHorizonStrokePaint, mGridStrokePaint,
     mSatelliteFillPaint, mSatelliteStrokePaint;

		private float mOrientation = 0.0f;		
		private float mSnrs[], mElevs[], mAzims[];
		private int mSvCount;		
		private  float mSnrThresholds[];
		private  int mSnrColors[];		
		private static final int SAT_RADIUS = 5;
		private boolean mStarted=false;
	public GpsSkyView(Context context) {
		super(context);
		init();
	}
	public GpsSkyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public GpsSkyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}	
	@Override
	protected void onDetachedFromWindow() {		
		super.onDetachedFromWindow();
	}
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);		
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		return state;
	}

	private void init() {	
		initDrawingTools();
	}
	private void initDrawingTools() {
		rimRect = new RectF(0.01f,0.01f, 0.99f, 0.99f);

		// the linear gradient is a bit skewed for realism
		rimPaint = new Paint();
		
		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.YELLOW);
		rimCirclePaint.setStrokeWidth(0.005f);

		float rimSize = 0.005f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
			     rimRect.right - rimSize, rimRect.bottom - rimSize);		

		faceTexture = BitmapFactory.decodeResource(getContext().getResources(), 
				   R.drawable.plastic);
		BitmapShader paperShader = new BitmapShader(faceTexture, 
												    Shader.TileMode.MIRROR, 
												    Shader.TileMode.MIRROR);
		Matrix paperMatrix = new Matrix();
		facePaint = new Paint();
		facePaint.setFilterBitmap(true);
		paperMatrix.setScale(1.0f / faceTexture.getWidth(), 
							 1.0f / faceTexture.getHeight());
		paperShader.setLocalMatrix(paperMatrix);
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setShader(paperShader);
		
		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
		/////////////////////
		 mHorizonStrokePaint = new Paint();
         mHorizonStrokePaint.setColor(Color.BLACK);
         mHorizonStrokePaint.setStyle(Paint.Style.STROKE);
         mHorizonStrokePaint.setStrokeWidth(2.0f);

         mGridStrokePaint = new Paint();
         mGridStrokePaint.setColor(Color.WHITE);
         mGridStrokePaint.setStyle(Paint.Style.STROKE);

         mSatelliteFillPaint = new Paint();
         mSatelliteFillPaint.setColor(Color.YELLOW);
         mSatelliteFillPaint.setStyle(Paint.Style.FILL);

         mSatelliteStrokePaint = new Paint();
         mSatelliteStrokePaint.setColor(Color.BLACK);
         mSatelliteStrokePaint.setStyle(Paint.Style.STROKE);
         mSatelliteStrokePaint.setStrokeWidth(2.0f);

         mSnrThresholds = new float[] { 0.0f,       10.0f,     20.0f,        30.0f       };
         mSnrColors     = new int[]   { Color.GRAY, Color.RED, Color.YELLOW, Color.GREEN };

         setFocusable(true);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
		Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);
		
		int chosenDimension = Math.min(chosenWidth, chosenHeight);
		
		setMeasuredDimension(chosenDimension, chosenDimension);
	}
	
	private int chooseDimension(int mode, int size) {
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		} 
	}
	
	// in case there is no size specified
	private int getPreferredSize() {
		return 300;
	}

	private void drawRim(Canvas canvas) {
		// first, draw the metallic body
		canvas.drawOval(rimRect, rimPaint);
		// now the outer rim circle
		canvas.drawOval(rimRect, rimCirclePaint);
	}
	
	private void drawFace(Canvas canvas) {		
		canvas.drawOval(faceRect, facePaint);
		// draw the inner rim circle
		canvas.drawOval(faceRect, rimCirclePaint);		
	}
		
	private void drawBackground(Canvas canvas) {
		if (background == null) {			
		} else {
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			
		regenerateBackground();
	}
	
	private void regenerateBackground() {
		// free the old bitmap
		if (background != null) {
			background.recycle();
		}
		
		background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		float scale = (float) getWidth();		
		backgroundCanvas.scale(scale, scale);
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);		
	}
	///////////////////////////////////7
	
	public void setStarted() {
        mStarted = true;
        invalidate();
    }

    public void setStopped() {
        mStarted = false;
        mSvCount = 0;
        invalidate();
    }
    public void setRotate(float heading) {
    	 mOrientation = heading;
         invalidate();
    }
    public void setSats(GpsStatus status) {
        Iterator<GpsSatellite> satellites = status.getSatellites().iterator();

        if (mSnrs == null) {
            int length = status.getMaxSatellites();
            mSnrs = new float[length];
            mElevs = new float[length];
            mAzims = new float[length];
        }

        mSvCount = 0;
        while (satellites.hasNext()) {
            GpsSatellite satellite = satellites.next();
            mSnrs[mSvCount] = satellite.getSnr();
            mElevs[mSvCount] = satellite.getElevation();
            mAzims[mSvCount] = satellite.getAzimuth();
            mSvCount++;
        }

        mStarted = true;
        invalidate();
    }

    private void drawLine(Canvas c, float x1, float y1, float x2, float y2) {
        // rotate the line based on orientation
        double angle = Math.toRadians(-mOrientation);
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);

        float centerX = (x1 + x2) / 2.0f;
        float centerY = (y1 + y2) / 2.0f;
        x1 -= centerX;
        y1 = centerY - y1;
        x2 -= centerX;
        y2 = centerY - y2;

        float X1 = cos * x1 + sin * y1 + centerX;
        float Y1 = -(-sin * x1 + cos * y1) + centerY;
        float X2 = cos * x2 + sin * y2 + centerX;
        float Y2 = -(-sin * x2 + cos * y2) + centerY;

        c.drawLine(X1, Y1, X2, Y2, mGridStrokePaint);
    }
    private void drawSatellite(Canvas c, int s, float elev, float azim, float snr) {
        double radius, angle;
        float x, y;
        Paint thisPaint;

        thisPaint = getSatellitePaint(mSatelliteFillPaint, snr);

        radius = elevationToRadius(s, elev);
        azim -= mOrientation;
        angle = (float)Math.toRadians(azim);

        x = (float)((s / 2) + (radius * Math.sin(angle)));
        y = (float)((s / 2) - (radius * Math.cos(angle)));

        c.drawCircle(x, y, SAT_RADIUS, thisPaint);
        c.drawCircle(x, y, SAT_RADIUS, mSatelliteStrokePaint);
    }

    private float elevationToRadius(int s, float elev) {
        return ((s / 2) - SAT_RADIUS) * (1.0f - (elev / 90.0f));
    }

    private Paint getSatellitePaint(Paint base, float snr) {
        int numSteps;
        Paint newPaint;

        newPaint = new Paint(base);

        numSteps = mSnrThresholds.length;

        if (snr <= mSnrThresholds[0]) {
            newPaint.setColor(mSnrColors[0]);
            return newPaint;
        }

        if (snr >= mSnrThresholds[numSteps - 1]) {
            newPaint.setColor(mSnrColors[numSteps - 1]);
            return newPaint;
        }

        for (int i = 0; i < numSteps - 1; i++) {
            float threshold = mSnrThresholds[i];
            float nextThreshold = mSnrThresholds[i + 1];
            if (snr >= threshold && snr <= nextThreshold) {
                int c1, r1, g1, b1, c2, r2, g2, b2, c3, r3, g3, b3;
                float f;

                c1 = mSnrColors[i];
                r1 = Color.red(c1);
                g1 = Color.green(c1);
                b1 = Color.blue(c1);

                c2 = mSnrColors[i + 1];
                r2 = Color.red(c2);
                g2 = Color.green(c2);
                b2 = Color.blue(c2);

                f = (snr - threshold) / (nextThreshold - threshold);

                r3 = (int)(r2 * f + r1 * (1.0f - f));
                g3 = (int)(g2 * f + g1 * (1.0f - f));
                b3 = (int)(b2 * f + b1 * (1.0f - f));
                c3 = Color.rgb(r3, g3, b3);

                newPaint.setColor(c3);

                return newPaint;
            }
        }
        newPaint.setColor(Color.MAGENTA);
        return newPaint;
    }
    private void drawHorizon(Canvas c, int s) {
        float radius = s / 2;

       // c.drawCircle(radius, radius, radius, mHorizonStrokePaint);
        drawLine(c, 0, radius, 2 * radius, radius);
        drawLine(c, radius, 0, radius, 2 * radius);
        c.drawCircle(radius, radius, elevationToRadius(s, 60.0f), mGridStrokePaint);
        c.drawCircle(radius, radius, elevationToRadius(s, 30.0f), mGridStrokePaint);
        //c.drawCircle(radius, radius, elevationToRadius(s,  0.0f), mGridStrokePaint);
       // c.drawCircle(radius, radius, radius, mHorizonStrokePaint);
    }
	///////////////////////////////////////
	@Override
	protected void onDraw(Canvas canvas) {
		 drawBackground(canvas);
		 int w, h, s;
         w = canvas.getWidth();
         h = canvas.getHeight();
         s = (w < h) ? w : h;

        drawHorizon(canvas, s);

         if (mElevs != null) {
             int numSats = mSvCount;

             for (int i = 0; i < numSats; i++) {
                 if (mSnrs[i] > 0.0f && (mElevs[i] != 0.0f || mAzims[i] != 0.0f))
                     drawSatellite(canvas, s, mElevs[i], mAzims[i], mSnrs[i]);
             }
         }
		float scale = (float) getWidth();		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale, scale);
		canvas.restore();
	
	}
		
}
