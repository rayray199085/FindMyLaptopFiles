package com.example.stephencao.findmylaptopfiles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MyBaseAdapter extends BaseAdapter {
    private Context context;
    private List<String> stringList;
    private LayoutInflater layoutInflater;

    public MyBaseAdapter(Context context, List<String> stringList) {
        this.context = context;
        this.stringList = stringList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return stringList.size();
    }

    @Override
    public Object getItem(int position) {
        return stringList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.item, null);
            viewHolder.textView = convertView.findViewById(R.id.item_text_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String location = stringList.get(position);
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.textView.setText(location);
        return convertView;
    }

    class ViewHolder {
        public TextView textView;
    }
}
