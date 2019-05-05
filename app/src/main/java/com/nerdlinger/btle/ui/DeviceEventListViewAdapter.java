package com.nerdlinger.btle.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nerdlinger.btle.R;

import java.util.List;

public class DeviceEventListViewAdapter extends BaseAdapter {
	private Context m_context;
	private List<OneDeviceEvent> m_list;

	public DeviceEventListViewAdapter(Context context, List<OneDeviceEvent> List) {
		m_context = context;
		m_list = List;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ItemHolder holder = null;
		LayoutInflater inflater = ((Activity) m_context).getLayoutInflater();
		row = inflater.inflate(R.layout.one_event_row, parent, false);

		holder = new ItemHolder();
		holder.item = m_list.get(position);
		holder.tvId = (TextView) row.findViewById(R.id.tvId);
		holder.tvStatus = (TextView) row.findViewById(R.id.tvEvent);

		row.setTag(holder);
		holder.tvId.setText(holder.item.getId());
		holder.tvStatus.setText(holder.item.getEvent());

		return row;
	}

	public static class ItemHolder {
		OneDeviceEvent item;
		TextView tvId;
		TextView tvStatus;
	}

	@Override
	public int getCount() {
		return m_list.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}


/*
package com.nerdlinger.scrollablelistviewdemo;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewAdapter extends BaseAdapter {

	public View getView(int position, View convertView, ViewGroup parent) {

				}

public static class ItemHolder {
	ListItem item;
	TextView tvId;
	TextView tvName;
	TextView tvStatus;
}

	@Override
	public int getCount() {
		return m_list.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}

 */