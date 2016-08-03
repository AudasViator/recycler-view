package ru.yandex.yamblz.ui.adapters;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    private final Random rnd = new Random();
    private final List<Integer> colors = new ArrayList<>();
    private boolean mIsAmazingStyle;

    @Override
    public ContentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.content_item, parent, false), parent.getContext().getResources().getDrawable(R.drawable.ramko));
    }

    @Override
    public void onBindViewHolder(ContentHolder holder, int position) {
        holder.bind(createColorForPosition(position), mIsAmazingStyle);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public void deleteItem(int position) {
        colors.remove(position);
        notifyItemRemoved(position);
    }

    public void swapItems(int fromPosition, int toPosition) {
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

    public void updateColor(int color, int position) {
        colors.set(position, color);
    }

    public void setAmazingStyle(boolean isAmazingStyle) {
        mIsAmazingStyle = isAmazingStyle;
    }

    public boolean getAmazingStyle() {
        return mIsAmazingStyle;
    }

    private Integer createColorForPosition(int position) {
        if (position >= colors.size()) {
            colors.add(Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
        }
        return colors.get(position);
    }

    static class ContentHolder extends RecyclerView.ViewHolder {
        private Drawable mBackgroundDrawable;
        private int mColor;

        @BindView(R.id.content_item_text_view)
        TextView mTextView;

        @BindView(R.id.content_item_image_view)
        ImageView mImageView;

        ContentHolder(View itemView, Drawable background) {
            super(itemView);
            mBackgroundDrawable = background;
            ButterKnife.bind(this, itemView);
            itemView.setOnTouchListener(new touchListener());
        }

        void bind(Integer color, boolean amazingStyle) {
            // Через ItemDecoration делать некруто, при перетаскивании рамка останется на месте
            if (amazingStyle && getAdapterPosition() % 2 == 0) {
                mImageView.setImageDrawable(mBackgroundDrawable);
            } else {
                mImageView.setImageDrawable(null);
            }
            mColor = color;
            mTextView.setBackgroundColor(color);
            mTextView.setText("#".concat(Integer.toHexString(color).substring(2)));
        }

        private class touchListener implements View.OnTouchListener {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int r = (mColor >> 16) & 0xFF;
                int g = (mColor >> 8) & 0xFF;
                int b = mColor & 0xFF;

                float[] hsv = new float[3];
                Color.RGBToHSV(r, g, b, hsv);
                hsv[0] += 0.05f;


                mColor = Color.HSVToColor(hsv);
                mTextView.setText("#".concat(Integer.toHexString(mColor).substring(2)));
                mTextView.setBackgroundColor(mColor);
                return false;
            }
        }
    }
}