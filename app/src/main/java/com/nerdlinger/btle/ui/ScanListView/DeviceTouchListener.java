package com.nerdlinger.btle.ui.ScanListView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DeviceTouchListener implements RecyclerView.OnItemTouchListener {
	private GestureDetector m_gestureDetector;
	private ClickListener m_clickListener;

	public DeviceTouchListener(Context context,
	                           final RecyclerView rvScanResults,
	                           final ClickListener clickListener) {
		m_clickListener = clickListener;
		m_gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				View child = rvScanResults.findChildViewUnder(e.getX(), e.getY());
				if (child != null && clickListener != null) {
					clickListener.onLongClick(child, rvScanResults.getChildAdapterPosition(child));
				}
			}
		});
	}

	@Override
	public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
		View child = rv.findChildViewUnder(e.getX(), e.getY());
		if (child != null && m_clickListener != null && m_gestureDetector.onTouchEvent(e)) {
			m_clickListener.onClick(child, rv.getChildAdapterPosition(child));
		}
		return false;
	}

	@Override
	public void onTouchEvent(RecyclerView rv, MotionEvent e) {
		// empty on purpose... needed for "implements RecyclerView.OnItemTouchListener"
	}

	@Override
	public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		// empty on purpose... needed for "implements RecyclerView.OnItemTouchListener"
	}
	// ===============================================
	public interface ClickListener {
		void onClick(View view, int position);
		void onLongClick(View view, int position);
	}
}
