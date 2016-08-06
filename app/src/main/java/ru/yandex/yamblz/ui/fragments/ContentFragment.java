package ru.yandex.yamblz.ui.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import ru.yandex.yamblz.R;
import ru.yandex.yamblz.ui.adapters.ContentAdapter;

public class ContentFragment extends BaseFragment {
    public static final int SPAN_COUNT = 2;

    private ContentAdapter mRvAdapter = new ContentAdapter();

    // Используются для отображения свапнутых элементов через ViewHolder
    private boolean mIsSwapped;
    private int mFirstSwappedItem = -1;
    private int mSecondSwappedItem = -1;

    @BindView(R.id.rv)
    RecyclerView rv;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT));
        rv.getRecycledViewPool().setMaxRecycledViews(0, SPAN_COUNT * 4);
        rv.setAdapter(mRvAdapter);
        rv.addItemDecoration(new SwapItemDecoration());

        setupItemTouchHelper();
    }

    // Увеличивает число колонок
    @OnClick(R.id.fragment_content_plus_button)
    void plusSpan() {
        updateSpanCount(true, rv);
    }

    // Уменьшает число колонок
    @OnClick(R.id.fragment_content_minus_button)
    void minusSpan() {
        updateSpanCount(false, rv);
    }

    // Изменяет стиль
    @OnClick(R.id.fragment_content_style_button)
    void changeStyle() {
        mRvAdapter.setAmazingStyle(!mRvAdapter.getAmazingStyle());
        updateRv();
    }

    private void setupItemTouchHelper() {
        rv.setOnTouchListener((v, event) -> {
            // Обновляем ViewHolder`ы только тогда, когда палец отпустили,
            // иначе свап прекращается после первого свапа
            // т.е. потащили вниз, нижний элемент перехал на место того, что тащим
            // Тут свайп прекратился, так как notifyItemChange
            // Обновлять VH нужно для изменения стиля
            if (mIsSwapped && event.getAction() == MotionEvent.ACTION_UP) {
                mRvAdapter.swapItemsNotifyCostyl(mFirstSwappedItem, mSecondSwappedItem);
                updateRv();
                mIsSwapped = false;
                mFirstSwappedItem = mSecondSwappedItem = -1;
            }
            return false;
        });

        int dragDirs = ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(dragDirs, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int firstChangedItem = viewHolder.getAdapterPosition();
                if (mFirstSwappedItem == -1) { // Запоминаем элемент, который потащили
                    mFirstSwappedItem = firstChangedItem;
                }

                mSecondSwappedItem = target.getAdapterPosition();
                mRvAdapter.swapItems(firstChangedItem, mSecondSwappedItem); // Как обычно меняем элементы
                mIsSwapped = true; // Говорим onTouchListener`у, что надо обновить ViewHolder`ы

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                mRvAdapter.deleteItem(position);
                updateRv();
            }

            // Изменение цвета при перетаскивании
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

    // Обновляет видимые на экране элементы
    private void updateRv() {
        GridLayoutManager gridLayoutManager = (GridLayoutManager) rv.getLayoutManager();
        int spanCount = gridLayoutManager.getSpanCount();
        int first = gridLayoutManager.findFirstVisibleItemPosition();
        int last = gridLayoutManager.findLastVisibleItemPosition();
        if (last + spanCount < mRvAdapter.getItemCount()) {
            mRvAdapter.notifyItemRangeChanged(first, last + spanCount);
        } else {
            mRvAdapter.notifyItemRangeChanged(first, last);
        }
    }

    private void updateSpanCount(boolean increase, RecyclerView recyclerView) {
        try {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            int spanCount = gridLayoutManager.getSpanCount();

            if (increase) {
                spanCount++;
                // Иначе не хватит холдеров на все элементы
                rv.getRecycledViewPool().setMaxRecycledViews(0, spanCount * 4);
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
            rv.getAdapter().notifyItemChanged(0);
        } catch (ClassCastException e) {
            Toast.makeText(getContext(), "Не grid", Toast.LENGTH_SHORT).show();
        }
    }

    private class SwapItemDecoration extends RecyclerView.ItemDecoration {
        private static final float STROKE_WIDTH = 10f;
        private Paint mPaint = new Paint();

        SwapItemDecoration() {
            mPaint.setStrokeWidth(STROKE_WIDTH);
            mPaint.setColor(Color.CYAN);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int childrenCount = parent.getChildCount();
            for (int i = 0; i < childrenCount; i++) {
                View view = parent.getChildAt(i);
                RecyclerView.ViewHolder vh = parent.getChildViewHolder(view);
                if (vh instanceof ContentAdapter.ContentHolder) {
                    ContentAdapter.ContentHolder holder = (ContentAdapter.ContentHolder) vh;
                    // Можно сделать метод setSwapped(int first, int second),
                    // но во VH состояние уже хранится
                    if (holder.isSwapped()) {
                        markChild(c, view);
                    }
                }
            }
        }

        private void markChild(Canvas canvas, View child) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getTop() + params.topMargin;
            int bottom = child.getBottom() + params.bottomMargin;

            int left = child.getLeft() + params.leftMargin;
            int right = child.getRight() + params.rightMargin;

            canvas.drawLine(left, top, right, top, mPaint);
            canvas.drawLine(right, top, right, bottom, mPaint);
            canvas.drawLine(left, bottom, right, bottom, mPaint);
            canvas.drawLine(left, top, left, bottom, mPaint);
        }
    }
}