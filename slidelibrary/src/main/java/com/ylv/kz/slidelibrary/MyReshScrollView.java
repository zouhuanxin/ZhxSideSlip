package com.ylv.kz.slidelibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MyReshScrollView extends FrameLayout {
    private Context mContext;
    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mActivePointerId;
    private int dy = 0;
    private int my = 0;
    private View HeadView;
    private View ContentView;
    private View FooterView;
    private boolean PullOnloading = false;
    private boolean DropDownRefresh = false;
    //footer
    private LinearLayout loadMoreLayout;
    private ProgressBar loadMoreProgressBar;
    private TextView loadMoreText;
    //header
    private LinearLayout DropDwonLayout;
    private ImageView DropDwonProgressBar;
    private TextView DropDwonText;
    private ImageView DropDwonProgressBar2;
    private TextView DropDwonText2;
    private ProgressBar DropDwonProgressBar3;
    private TextView DropDwonText3;

    public MyReshScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyReshScrollView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        //setOrientation(VERTICAL);
        mContext = context;
        mScroller = new OverScroller(mContext);
        mVelocityTracker = VelocityTracker.obtain();
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        HeadView = LayoutInflater.from(context).inflate(R.layout.header_layout, null);
        FooterView = LayoutInflater.from(context).inflate(R.layout.footer_layout, null);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyReshScrollView);
        PullOnloading = a.getBoolean(R.styleable.MyReshScrollView_pull, false);
        DropDownRefresh = a.getBoolean(R.styleable.MyReshScrollView_drop, false);
        ViewGroup.LayoutParams headparams = new ViewGroup.LayoutParams(getScreenSize(context).widthPixels, DropDownRefresh ? 140 : 0);
        ViewGroup.LayoutParams footparams = new ViewGroup.LayoutParams(getScreenSize(context).widthPixels, PullOnloading ? 140 : 0);
        HeadView.setLayoutParams(headparams);
        FooterView.setLayoutParams(footparams);
        PullView(HeadView);
        DropDownView(FooterView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (ContentView == null) {
            ContentView = getChildAt(0);
        }
        removeAllViews();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        ContentView.setLayoutParams(params);
        addView(HeadView);
        addView(ContentView);
        addView(FooterView);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
        getChildAt(0).layout(l, 0, r, getChildAt(0).getMeasuredHeight());
        getChildAt(1).layout(l, getChildAt(0).getMeasuredHeight(), r,
                getChildAt(1).getMeasuredHeight() + getChildAt(0).getMeasuredHeight());
        getChildAt(2).layout(l, getChildAt(0).getMeasuredHeight() + getChildAt(1).getMeasuredHeight(), r,
                getChildAt(0).getMeasuredHeight() + getChildAt(1).getMeasuredHeight() + getChildAt(2).getMeasuredHeight());
        scrollBy(0, getChildAt(0).getMeasuredHeight());
        loadMoreLayout.setVisibility(GONE);
        DropDwonLayout.setVisibility(GONE);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished())
                    mScroller.abortAnimation();
                dy = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                my = (int) event.getY();
                if (DropDownRefresh && getScrollY() < getChildAt(0).getMeasuredHeight()) {
                    DropDwonLayout.setVisibility(VISIBLE);
                    if (getScrollY() > 0) {
                        DropDwonText.setVisibility(VISIBLE);
                        DropDwonText2.setVisibility(INVISIBLE);
                        DropDwonText3.setVisibility(INVISIBLE);
                        DropDwonProgressBar.setVisibility(VISIBLE);
                        DropDwonProgressBar2.setVisibility(INVISIBLE);
                        DropDwonProgressBar3.setVisibility(INVISIBLE);
                    } else {
                        DropDwonText.setVisibility(INVISIBLE);
                        DropDwonText2.setVisibility(VISIBLE);
                        DropDwonText3.setVisibility(INVISIBLE);
                        DropDwonProgressBar.setVisibility(INVISIBLE);
                        DropDwonProgressBar2.setVisibility(VISIBLE);
                        DropDwonProgressBar3.setVisibility(INVISIBLE);
                    }
                }else if (getScrollY() - getChildAt(0).getMeasuredHeight() >= Math.max(0, getChildAt(1).getMeasuredHeight() - getHeight())){
                    loadMoreLayout.setVisibility(VISIBLE);
                }
                scrollBy(0, dy - my);
                dy = my;
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
                completeMove(-initialVelocity);
                break;
        }
        return true;
    }

    public static DisplayMetrics getScreenSize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    private void completeMove(float velocityY) {
        if (getScrollY() < getChildAt(0).getMeasuredHeight()) {
            //超出顶部边界
            if (DropDownRefresh) {
                DropDwonText.setVisibility(INVISIBLE);
                DropDwonText2.setVisibility(INVISIBLE);
                DropDwonText3.setVisibility(VISIBLE);
                DropDwonProgressBar.setVisibility(INVISIBLE);
                DropDwonProgressBar2.setVisibility(INVISIBLE);
                DropDwonProgressBar3.setVisibility(VISIBLE);
                mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), 300);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            setDropLoadMoreCompleted();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                mScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - getChildAt(0).getMeasuredHeight()), 300);
            }
        } else if (getScrollY() - getChildAt(0).getMeasuredHeight() >= Math.max(0, getChildAt(1).getMeasuredHeight() - getHeight())) {
            if (PullOnloading) {
                mScroller.startScroll(0, getScrollY(), 0,
                        Math.max(0,
                                getChildAt(1).getMeasuredHeight() - getHeight()) - getScrollY() + getChildAt(0).getMeasuredHeight()
                                + getChildAt(2).getMeasuredHeight(),
                        300);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            setPullLoadMoreCompleted();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                mScroller.startScroll(0, getScrollY(), 0,
                        Math.max(getChildAt(0).getMeasuredHeight(),
                                getChildAt(1).getMeasuredHeight() - getHeight()) - getScrollY() ,
                        300);
            }
        } else {
            mScroller.fling(0, getScrollY(), 0, (int) velocityY, 0, 0,
                    getChildAt(0).getMeasuredHeight(),
                    getChildAt(1).getMeasuredHeight() + getChildAt(0).getMeasuredHeight() - getHeight());
        }
        invalidate();
    }

    public void setPullLoadMoreCompleted() {
        mScroller.startScroll(0, getScrollY(), 0,
                Math.max(0,
                        getChildAt(1).getMeasuredHeight() - getHeight()) - getScrollY() + getChildAt(0).getMeasuredHeight(),
                300);
        invalidate();
        DropDwonLayout.post(new Runnable() {
            @Override
            public void run() {
                DropDwonLayout.setVisibility(GONE);
            }
        });
    }

    public void setDropLoadMoreCompleted() {
        mScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - getChildAt(0).getMeasuredHeight()), 300);
        invalidate();
        loadMoreLayout.post(new Runnable() {
            @Override
            public void run() {
                loadMoreLayout.setVisibility(GONE);
            }
        });
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    private void DropDownView(View v) {
        loadMoreLayout = (LinearLayout) v.findViewById(R.id.loadMoreLayout);
        loadMoreProgressBar = (ProgressBar) v.findViewById(R.id.loadMoreProgressBar);
        loadMoreText = (TextView) v.findViewById(R.id.loadMoreText);
    }

    private void PullView(View v) {
        DropDwonLayout = (LinearLayout) v.findViewById(R.id.DropDwonLayout);
        DropDwonProgressBar = (ImageView) v.findViewById(R.id.DropDwonProgressBar);
        DropDwonText = (TextView) v.findViewById(R.id.DropDwonText);
        DropDwonProgressBar2 = (ImageView) v.findViewById(R.id.DropDwonProgressBar2);
        DropDwonText2 = (TextView) v.findViewById(R.id.DropDwonText2);
        DropDwonProgressBar3 = (ProgressBar) v.findViewById(R.id.DropDwonProgressBar3);
        DropDwonText3 = (TextView) v.findViewById(R.id.DropDwonText3);
    }

}