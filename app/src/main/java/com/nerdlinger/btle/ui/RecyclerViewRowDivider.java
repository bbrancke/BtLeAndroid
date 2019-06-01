package com.nerdlinger.btle.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

public class RecyclerViewRowDivider extends RecyclerView.ItemDecoration {
	private Drawable m_divider;
	private int m_orientation = LinearLayoutManager.VERTICAL;
	public RecyclerViewRowDivider(Drawable divider) {
		m_divider = divider;
	}
	@Override
	public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
		if (m_orientation == LinearLayoutManager.HORIZONTAL) {
			drawHorizontalDividers(canvas, parent);
		}
		else if (m_orientation == LinearLayoutManager.VERTICAL) {
			drawVerticalDividers(canvas, parent);
		}
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		super.getItemOffsets(outRect, view, parent, state);
		if (parent.getChildAdapterPosition(view) == 0) {
			return;
		}
		m_orientation = ((LinearLayoutManager)parent.getLayoutManager()).getOrientation();
		if (m_orientation == LinearLayoutManager.HORIZONTAL) {
			outRect.left = m_divider.getIntrinsicWidth();
		}
		else if (m_orientation == LinearLayoutManager.VERTICAL) {
			outRect.top = m_divider.getIntrinsicHeight();
		}
	}

	private void drawHorizontalDividers(Canvas canvas, RecyclerView parent) {
		int parentTop = parent.getPaddingTop();
		int parentBottom = parent.getHeight() - parent.getPaddingBottom();
		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount - 1; i++) {
			View child = parent.getChildAt(i);
			RecyclerView.LayoutParams p = (RecyclerView.LayoutParams)child.getLayoutParams();
			int parentLeft = child.getRight() + p.rightMargin;
			int parentRight = parentLeft + m_divider.getIntrinsicWidth();
			m_divider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
			m_divider.draw(canvas);
		}
	}

	private void drawVerticalDividers(Canvas canvas, RecyclerView parent) {
		int parentLeft = parent.getPaddingLeft();
		int parentRight = parent.getWidth() - parent.getPaddingRight();

		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount - 1; i++) {
			View child = parent.getChildAt(i);

			RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

			int parentTop = child.getBottom() + params.bottomMargin;
			int parentBottom = parentTop + m_divider.getIntrinsicHeight();

			m_divider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
			m_divider.draw(canvas);
		}
	}

}

