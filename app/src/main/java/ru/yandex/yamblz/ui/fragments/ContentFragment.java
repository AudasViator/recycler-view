package ru.yandex.yamblz.ui.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import ru.yandex.yamblz.R;

public class ContentFragment extends BaseFragment {

    @BindView(R.id.rv)
    RecyclerView rv;

    @BindView(R.id.fragment_content_plus_button)
    Button buttonPlus;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rv.setAdapter(new ContentAdapter());
        //rv.addItemDecoration(new amazingDividerItemDecoration(getContext()));

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                ContentAdapter adapter = (ContentAdapter) rv.getAdapter();
                int endAdapterPosition = target.getAdapterPosition();
                int startAdapterPosition = viewHolder.getAdapterPosition();
                adapter.swapItems(startAdapterPosition, endAdapterPosition);
                //adapter.notifyItemChanged(startAdapterPosition);
                //adapter.notifyItemChanged(endAdapterPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ContentAdapter adapter = (ContentAdapter) rv.getAdapter();
                adapter.deleteItem(position);
                updateRv();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                int intX = (int) Math.sqrt(dX * dX + dY * dY) / 2 % 255;
                c.drawRGB(255, 255 - intX, 255 - intX);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rv);
    }

    @OnClick(R.id.fragment_content_plus_button)
    void plusSpan() {
        updateSpanCount(true, rv);
    }

    @OnClick(R.id.fragment_content_minus_button)
    void minusSpan() {
        updateSpanCount(false, rv);
    }

    @OnClick(R.id.fragment_content_style_button)
    void changeStyle() {
        ContentAdapter contentAdapter = (ContentAdapter) rv.getAdapter();
        contentAdapter.setAmazingStyle(!contentAdapter.getAmazingStyle());

        updateRv();
    }

    private void updateRv() {
        // Некрасиво
        ContentAdapter contentAdapter = (ContentAdapter) rv.getAdapter();
        GridLayoutManager gridLayoutManager = (GridLayoutManager) rv.getLayoutManager();
        int spanCount = gridLayoutManager.getSpanCount();
        int first = gridLayoutManager.findFirstVisibleItemPosition();
        int last = gridLayoutManager.findLastVisibleItemPosition();
        if (last + spanCount < contentAdapter.getItemCount()) {
            contentAdapter.notifyItemRangeChanged(first, last + spanCount);
        } else {
            contentAdapter.notifyItemRangeChanged(first, last);
        }
    }

    private void updateSpanCount(boolean increase, RecyclerView recyclerView) {
        try {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            int spanCount = gridLayoutManager.getSpanCount();

            if (increase) {
                spanCount++;
            } else {
                if (spanCount > 1) {
                    spanCount--;
                } else {
                    Toast.makeText(getContext(), "Не надо", Toast.LENGTH_SHORT).show();
                }
            }

            gridLayoutManager.setSpanCount(spanCount);

            // Идея нагло украдена у @guliash (Artem Gilmudinov)
            // Хотя в презентации написано, что эти методы с анимацией, но ктож её смотрит?
            // Эксперименты показали, что можно вызвать любой метод notifyXyzChanged
            // Те элементы, которые сейчас показываются на экране анимированно сдвигаются
            // При скроллинге новые элементы появляются сразу в нужном виде
            // Пробежавшись отладчиком, можно увидеть, как rv что-то там обновляет и анимирует =)
            rv.getAdapter().notifyItemChanged(0);
        } catch (ClassCastException e) {
            Toast.makeText(getContext(), "Не grid", Toast.LENGTH_SHORT).show();
        }
    }

    private class amazingDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public amazingDividerItemDecoration(Context context) {
            final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.listDivider});
            mDivider = a.getDrawable(0);
            a.recycle();
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getTop() + params.topMargin;
                int bottom = child.getBottom() + params.bottomMargin;

                int left = child.getLeft() + params.leftMargin;
                int right = child.getRight() + params.rightMargin;

                mDivider.setBounds(left, top, right, bottom);
                //mDivider.draw(c);
                Paint paint = new Paint();
                paint.setStrokeWidth(10f);
                c.drawLine(left, top, right, top, paint);
                c.drawLine(left, top, left, bottom, paint);
                c.drawLine(left, top, left, bottom, paint);
                c.drawLine(right, top, right, bottom, paint);
            }
        }
    }
}