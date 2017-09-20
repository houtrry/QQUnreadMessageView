package com.houtrry.qqunreadmessageview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * @author houtrry
 * @version $Rev$
 * @time 2017/9/20 19:43
 * @desc ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDesc $TODO$
 */

public class UnreadBubbleView extends View {

    private static final String TAG = UnreadBubbleView.class.getSimpleName();
    private PointF currentPointF = new PointF();
    private int mBubbleColor = Color.parseColor("#ffe91e63");
    private String mTextValue = String.valueOf(1);
    private int mTextColor = Color.WHITE;
    private int mTextSize = 30;
    private int mWidth;
    private int mHeight;
    private Paint mPaint;
    private Paint mTextPaint;
    private Rect mTextRect = new Rect();
    private float mTextX;
    private float mTextY;
    private float mRadius;
    private float mSettledRadius;
    private PointF mCenterPoint = new PointF();
    private double mDistance;
    private BubbleStatus mBubbleStatus = BubbleStatus.STATUS_IDLE;
    private Path mBezierPath = new Path();
    /**
     * 贝塞尔存在的最大距离
     */
    private float mCriticalDistance = 0;
    /**
     * 保存贝塞尔曲线的5个关键点
     * mBezierPoints[0]: 控制点
     * mBezierPoints[1]: 固定圆的y值小的那个点
     * mBezierPoints[2]: 固定圆的y值大的那个点
     * mBezierPoints[3]: 移动圆的y值大的那个点
     * mBezierPoints[4]: 移动圆的y值大的那个点
     */
    private PointF[] mBezierPoints = {new PointF(), new PointF(), new PointF(), new PointF(), new PointF()};
    private ObjectAnimator mObjectAnimator;

    public UnreadBubbleView(Context context) {
        this(context, null);
    }

    public UnreadBubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnreadBubbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mBubbleColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);


    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mRadius = Math.min(mWidth, mHeight) * 0.5f;

        currentPointF.set(mRadius, mRadius);

        mCenterPoint.set(currentPointF);

        mSettledRadius = mRadius * 0.6f;

        mCriticalDistance = 6 * mRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: (" + currentPointF.x + ", " + currentPointF.y + ")");
        drawBubble(canvas);
        drawText(canvas);
    }

    private void drawBubble(Canvas canvas) {
        Log.d(TAG, "drawBubble: (" + currentPointF.x + ", " + currentPointF.y + "), " + mRadius + ", ");
        drawSettledCircle(canvas);
        drawMoveCircle(canvas);
        drawBezier(canvas);

    }

    /**
     * 画固定位置的圆
     *
     * @param canvas
     */
    private void drawSettledCircle(Canvas canvas) {
        if (mBubbleStatus == BubbleStatus.STATUS_CONNECT || mBubbleStatus == BubbleStatus.STATUS_RECOVER) {
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mSettledRadius, mPaint);
        }
    }

    /**
     * 画随手指移动的圆
     *
     * @param canvas
     */
    private void drawMoveCircle(Canvas canvas) {
        if (mBubbleStatus != BubbleStatus.STATUS_DISMISSED) {
            canvas.drawCircle(currentPointF.x, currentPointF.y, mRadius, mPaint);
        }
    }

    /**
     * 画贝塞尔曲线
     *
     * @param canvas
     */
    private void drawBezier(Canvas canvas) {
        if (mBubbleStatus == BubbleStatus.STATUS_CONNECT || mBubbleStatus == BubbleStatus.STATUS_RECOVER) {
            mBezierPath.reset();
            calculateBezierPoints();
            mBezierPath.moveTo(mBezierPoints[1].x, mBezierPoints[1].y);
            mBezierPath.quadTo(mBezierPoints[0].x, mBezierPoints[0].y, mBezierPoints[3].x, mBezierPoints[3].y);
            mBezierPath.lineTo(mBezierPoints[4].x, mBezierPoints[4].y);
            mBezierPath.quadTo(mBezierPoints[0].x, mBezierPoints[0].y, mBezierPoints[2].x, mBezierPoints[2].y);
            mBezierPath.close();
            canvas.drawPath(mBezierPath, mPaint);
        }
    }

    private double mSinx = 0;
    private double mCosx = 0;
    private float mSixFixed = 1;

    private void calculateBezierPoints() {
        mBezierPoints[0].x = currentPointF.x + (mCenterPoint.x - currentPointF.x) * 0.5f;
        mBezierPoints[0].y = currentPointF.y + (mCenterPoint.y - currentPointF.y) * 0.5f;

        mSinx = (mCenterPoint.x - currentPointF.x) / mDistance;
        mCosx = (mCenterPoint.y - currentPointF.y) / mDistance;


        mSixFixed = mSinx > 0 ? 1.0f : -1.0f;
        mBezierPoints[1].x = (float) (mCenterPoint.x + mSettledRadius * mCosx * mSixFixed);
        mBezierPoints[1].y = (float) (mCenterPoint.y - mSettledRadius * mSinx * mSixFixed);

        mBezierPoints[2].x = (float) (mCenterPoint.x - mSettledRadius * mCosx * mSixFixed);
        mBezierPoints[2].y = (float) (mCenterPoint.y + mSettledRadius * mSinx * mSixFixed);

        mBezierPoints[3].x = (float) (currentPointF.x + mRadius * mCosx * mSixFixed);
        mBezierPoints[3].y = (float) (currentPointF.y - mRadius * mSinx * mSixFixed);

        mBezierPoints[4].x = (float) (currentPointF.x - mRadius * mCosx * mSixFixed);
        mBezierPoints[4].y = (float) (currentPointF.y + mRadius * mSinx * mSixFixed);

    }

    /**
     * 画文字
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        if (mBubbleStatus != BubbleStatus.STATUS_DISMISSED) {
            mTextPaint.getTextBounds(mTextValue, 0, mTextValue.length(), mTextRect);
            mTextX = currentPointF.x - mTextPaint.measureText(mTextValue) * 0.5f;
            mTextY = currentPointF.y + mTextRect.height() * 0.5f;
            canvas.drawText(String.valueOf(mTextValue), mTextX, mTextY, mTextPaint);
        }
    }

    private float mDownX = 0;
    private float mDownY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                getParent().requestDisallowInterceptTouchEvent(true);
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                currentPointF.set(event.getX(), event.getY());

                mDistance = Math.hypot(currentPointF.x - mCenterPoint.x, currentPointF.y - mCenterPoint.y);

                if (mDistance > mCriticalDistance) {
                    mBubbleStatus = BubbleStatus.STATUS_DRAG;
                } else {
                    mBubbleStatus = BubbleStatus.STATUS_CONNECT;
                }

                ViewCompat.postInvalidateOnAnimation(this);
                break;
            }
            case MotionEvent.ACTION_UP: {
                getParent().requestDisallowInterceptTouchEvent(false);

                currentPointF.set(event.getX(), event.getY());

                mDistance = Math.hypot(currentPointF.x - mCenterPoint.x, currentPointF.y - mCenterPoint.y);
                mEndPointF.set(currentPointF);
                if (mDistance > mCriticalDistance) {
                    mBubbleStatus = BubbleStatus.STATUS_DISMISSING;
                    startDismissAnimate();
                } else {
                    mBubbleStatus = BubbleStatus.STATUS_RECOVER;
                    startRecoverAnimate(currentPointF);
                }
                break;
            }
        }
        return true;
    }

    private void startDismissAnimate() {
        animate().alpha(0.0f).setInterpolator(new FastOutLinearInInterpolator()).setDuration(1300).setListener(mAnimatorListener).start();
    }

    public void setCurrentPointF(PointF currentPointF) {
        this.currentPointF = currentPointF;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private PointF mEndPointF = new PointF();

    private void startRecoverAnimate(PointF pointF) {
        if (mObjectAnimator != null && mObjectAnimator.isRunning()) {
            mObjectAnimator.cancel();
        }
        mObjectAnimator = ObjectAnimator.ofObject(this, "currentPointF", new PointFEvaluator(), pointF, mCenterPoint);
        mObjectAnimator.setDuration(500);
        mObjectAnimator.setInterpolator(new OvershootInterpolator(3));
        mObjectAnimator.addListener(mAnimatorListener);
        mObjectAnimator.start();
    }

    public enum BubbleStatus {
        STATUS_DISMISSED,//消失状态, 已经消失
        STATUS_CONNECT,//拖动, 随手指移动, 有贝塞尔曲线
        STATUS_DRAG,//拖动, 随手指移动, 无贝塞尔曲线

        STATUS_RECOVER,//手指松开, 正在恢复初始化状态
        STATUS_DISMISSING,//手指松开, 正在消失

        STATUS_IDLE,//空闲状态
    }

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (mBubbleStatus == BubbleStatus.STATUS_RECOVER) {
                mBubbleStatus = BubbleStatus.STATUS_IDLE;
                mObjectAnimator.removeListener(this);
            } else if (mBubbleStatus == BubbleStatus.STATUS_DISMISSING) {
                mBubbleStatus = BubbleStatus.STATUS_DISMISSED;

            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

}
