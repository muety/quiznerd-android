package com.github.n1try.quiznerd.ui.adapter.entity;

import android.view.LayoutInflater;
import android.view.View;

public interface ListItem {
    enum RowType {
        LIST_ITEM, LIST_HEADER
    }

    int getViewType();

    View getView(LayoutInflater inflater, View convertView);
}
