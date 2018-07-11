package com.github.n1try.quiznerd.ui.adapter.entity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;

public class QuizMatchListHeader implements ListItem {
    private String text;

    public QuizMatchListHeader(String text) {
        this.text = text;
    }

    @Override
    public int getViewType() {
        return RowType.LIST_HEADER.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_quiz_list_header, null, false);
        }

        TextView headerTv = convertView.findViewById(R.id.header_text_tv);
        headerTv.setText(text);
        convertView.setEnabled(false);

        return convertView;
    }
}