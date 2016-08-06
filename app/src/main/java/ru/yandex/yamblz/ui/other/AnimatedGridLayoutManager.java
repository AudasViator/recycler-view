package ru.yandex.yamblz.ui.other;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;


public class AnimatedGridLayoutManager extends GridLayoutManager {

    private AnticipateOvershootInterpolator mInterpolator;

    public AnimatedGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public AnimatedGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        init();
    }

    public AnimatedGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
        init();
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);

        // Анимацию отменять не обязательно, VH переиспользуются
        Animator rotationX = ObjectAnimator
                .ofFloat(child, "rotationX", 0, 360)
                .setDuration(1000);
        rotationX.setInterpolator(mInterpolator);

        rotationX.start();
    }

    private void init() {
        mInterpolator = new AnticipateOvershootInterpolator();
    }
}
