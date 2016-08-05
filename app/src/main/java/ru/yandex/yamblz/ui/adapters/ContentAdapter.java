package ru.yandex.yamblz.ui.adapters;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.yandex.yamblz.R;


public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentHolder> {

    @IntDef(
            flag = true,
            value = {STYLE_SIMPLE, STYLE_AMAZING, STYLE_SWAPPED})
    public @interface StyleMode {
    }

    public static final int STYLE_SIMPLE = 1 << 1;
    public static final int STYLE_AMAZING = 1 << 2;
    public static final int STYLE_SWAPPED = 1 << 3;

    private final Random rnd = new Random();
    private final List<Integer> colors = new ArrayList<>();
    @ActionBar.NavigationMode
    private int mStyle;
    private int[] mLastSwapped = new int[]{-1, -1};

    @Override
    public ContentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.content_item, parent, false),
                parent.getContext().getResources().getDrawable(R.drawable.ramko),
                this);
    }

    @Override
    public void onBindViewHolder(ContentHolder holder, int position) {
        @StyleMode int style = mStyle;

        if (mStyle == STYLE_AMAZING && position % 2 != 0) {
            style = STYLE_SIMPLE;
        }

        if (position != mLastSwapped[0] && position != mLastSwapped[1]) {
            holder.bind(createColorForPosition(position), style);
        } else {
            holder.bind(createColorForPosition(position), STYLE_SWAPPED | style);
        }
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }


    @Override
    public boolean onFailedToRecycleView(ContentHolder holder) {
        return true;
    }

    public void deleteItem(int position) {
        colors.remove(position);

        if (mLastSwapped[0] == position) {
            mLastSwapped[0] = -1;
        }

        if (mLastSwapped[1] == position) {
            mLastSwapped[1] = -1;
        }

        if (mLastSwapped[0] > position) {
            mLastSwapped[0]--;
        }
        if (mLastSwapped[1] > position) {
            mLastSwapped[1]--;
        }

        notifyItemRemoved(position);
    }

    public void swapItems(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(colors, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(colors, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public void swapItemsNotifyCostyl(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        if (fromPosition < toPosition) {
            mLastSwapped[0] = fromPosition;
            mLastSwapped[1] = toPosition;
        } else {
            mLastSwapped[0] = toPosition;
            mLastSwapped[1] = fromPosition;
        }
    }

    public void changeColor(int color, int position) {
        colors.set(position, color);
    }

    public void setAmazingStyle(boolean isAmazingStyle) {
        if (isAmazingStyle) {
            mStyle = STYLE_AMAZING;
        } else {
            mStyle = STYLE_SIMPLE;
        }
    }

    public boolean getAmazingStyle() {
        return mStyle == STYLE_AMAZING;
    }

    private Integer createColorForPosition(int position) {
        if (position >= colors.size()) {
            colors.add(Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
        }
        return colors.get(position);
    }

    static class ContentHolder extends RecyclerView.ViewHolder {
        private Drawable mBackgroundDrawable;
        private ValueAnimator mAnim;
        private ContentAdapter mContentAdapter; // Как-то неправильно это
        private int mColor;

        @BindView(R.id.content_item_text_view)
        TextView mTextView;

        @BindView(R.id.content_item_image_view)
        ImageView mImageView;

        ContentHolder(View itemView, Drawable background, ContentAdapter contentAdapter) {
            super(itemView);
            mBackgroundDrawable = background;
            mContentAdapter = contentAdapter;
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                if (mAnim != null && mAnim.isRunning()) {
                    mAnim.cancel();
                } else {
                    // И чем мне ArgbEvaluator не понравился?..
                    int r = (mColor >> 16) & 0xFF;
                    int g = (mColor >> 8) & 0xFF;
                    int b = mColor & 0xFF;

                    final float[] current = new float[3];
                    final float[] to = new float[3];
                    final float[] from = new float[3];

                    Color.RGBToHSV(r, g, b, from);
                    Color.RGBToHSV(r, g, b, current);

                    to[0] = (from[0] + 180) % 360;
                    to[1] += (float) Math.random();

                    mAnim = ValueAnimator.ofFloat(0, 1);
                    mAnim.setDuration(5000);
                    mAnim.addUpdateListener(animation -> {
                        current[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
                        current[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();

                        int position = getAdapterPosition();
                        if (position != -1) {
                            int color = Color.HSVToColor(current);
                            setColor(color);
                            mContentAdapter.changeColor(color, position);
                        }
                    });
                    mAnim.start();
                }
            });
        }

        void setColor(int color) {
            mColor = color;
            mTextView.setText("#".concat(Integer.toHexString(mColor).substring(2)));
            mTextView.setBackgroundColor(mColor);
        }

        void bind(Integer color, @StyleMode int styleMode) {
            // Через ItemDecoration делать не круто, при перетаскивании рамка останется на месте

            // Just for fun
            boolean simple = (styleMode >> 1 & 0x1) == 1;
            boolean amazing = (styleMode >> 2 & 0x1) == 1;
            boolean swapped = (styleMode >> 3 & 0x1) == 1;

            setColor(color);

            if (simple) {
                mImageView.setImageDrawable(null);
            }

            if (amazing) {
                mTextView.setBackground(mBackgroundDrawable);
            }

            if (swapped) {
                mTextView.setText("SWAPPED");
            }
        }
    }
}