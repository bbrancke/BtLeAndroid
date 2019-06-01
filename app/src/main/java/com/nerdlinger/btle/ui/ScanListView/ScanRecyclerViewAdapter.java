package com.nerdlinger.btle.ui.ScanListView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nerdlinger.btle.R;

import org.w3c.dom.Text;

import java.util.List;

public class ScanRecyclerViewAdapter extends RecyclerView.Adapter<ScanRecyclerViewAdapter.MyViewHolder> {
	private List<OneBtDevice> m_devices;

	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name;
		public TextView bd_addr;
		public TextView m_id;

		public MyViewHolder(View view)
		{
			super(view);
			name = view.findViewById(R.id.text_view_one_device_row_name);
			bd_addr = view.findViewById(R.id.text_view_one_device_row_bdaddr);
			m_id = view.findViewById(R.id.tvId);
		}
	}

	public ScanRecyclerViewAdapter(List<OneBtDevice> devices) {
		m_devices = devices;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View deviceView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.scan_results_one_device_row, parent, false);
		// TODO: Find:
		// R.layout.layout_list_view_row_items (here, scan_result_view)
		// in RecyclerViewDemo
		// (Name of xml file??
		return new MyViewHolder(deviceView);
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		OneBtDevice device = m_devices.get(position);
		holder.name.setText(device.getDeviceName());
		holder.bd_addr.setText(device.getDeviceBdAddr());
		holder.m_id.setText(device.getId());
	}

	public void clear() {
		m_devices.clear();
	}

	@Override
	public int getItemCount() {
		return m_devices.size();
	}
}
