package opendoor.xunlebao.com.withchildcheck;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2017/7/19.
 * 选中的同时，对子控件中继承了Checkable的控件同时修改选中状态
 * 当触摸事件发生在Checkable的子控件时拦截事件，修改选中状态返回回调，所有子Checkable修改状态，
 * 其它控件按正常流程走。TouchEvent事件全部消费了，不消费只有Down事件不好做Click事件。
 */

public class WithChildCheckLayout extends FrameLayout implements Checkable {
    private CompoundButton.OnCheckedChangeListener mOnCheckChangeListener;
    private boolean mChecked;
    private boolean mBroadcasting = false;
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    public WithChildCheckLayout(@NonNull Context context) {
        super(context);
        setClickable(true);
    }

    public WithChildCheckLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
    }

    public WithChildCheckLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WithChildCheckLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setClickable(true);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();

            // Avoid infinite recursions if setChecked() is called from a listener
            //防止无限递归，如果回调调用了setChecked()
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckChangeListener != null) {
                mOnCheckChangeListener.onCheckedChanged(null, mChecked);
            }
            setChildChecked(this, mChecked);
            mBroadcasting = false;
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final Drawable buttonDrawable = getBackground();
        if (buttonDrawable != null && buttonDrawable.isStateful()
                && buttonDrawable.setState(getDrawableState())) {
            invalidateDrawable(buttonDrawable);
        }
    }

    private void setChildChecked(View child, boolean checked) {
        if (child instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) child).getChildCount(); i++) {
                View childAt = ((ViewGroup) child).getChildAt(i);
                setChildChecked(childAt, checked);
            }
        }
        if (child instanceof Checkable) {
            ((Checkable) child).setChecked(checked);
        }
    }

    @Override
    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean performClick() {
        toggle();
        final boolean handled = super.performClick();
        if (!handled) {
            // View only makes a sound effect if the onClickListener was
            // called, so we'll need to make one here instead.
            playSoundEffect(SoundEffectConstants.CLICK);
        }
        return handled;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean b = super.onInterceptTouchEvent(ev);
        Rect local = new Rect();
        if (!b) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof Checkable) {
                    child.getLocalVisibleRect(local);
                    if (ev.getX() >= child.getX()
                            && ev.getX() <= child.getX() + local.right
                            && ev.getY() >= child.getY()
                            && ev.getY() <= child.getY() + local.bottom) {
                        return true;
                    }
                }
            }
        }
        return b;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean hander = super.onTouchEvent(event);
//        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void setOnCheckChangeListener(CompoundButton.OnCheckedChangeListener onCheckChangeListener) {
        this.mOnCheckChangeListener = onCheckChangeListener;
    }
}
