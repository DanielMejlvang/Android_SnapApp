package com.dmejlvang.snapapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.dmejlvang.snapapp.R;
import com.dmejlvang.snapapp.TaskListener;
import com.dmejlvang.snapapp.repository.Repository;

import java.util.List;

public class SnapAdapter extends BaseAdapter {
    private List<String> snaps;
    private final LayoutInflater layoutInflater;

    public SnapAdapter(List<String> snaps, Context context) {
        this.snaps = snaps;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return snaps.size();
    }

    @Override
    public Object getItem(int position) {
        return snaps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    View view;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.snaprow, null);
        }
        view = convertView;

        return view;
    }
}
