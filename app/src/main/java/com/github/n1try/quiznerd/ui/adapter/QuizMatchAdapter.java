package com.github.n1try.quiznerd.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.ui.adapter.entity.ListItem;
import com.github.n1try.quiznerd.ui.adapter.entity.QuizMatchListHeader;
import com.github.n1try.quiznerd.ui.adapter.entity.QuizMatchListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QuizMatchAdapter extends ArrayAdapter<ListItem> {

    public static List<ListItem> generateItemList(Context context, List<QuizMatch> objects, QuizUser me) {
        List<QuizMatch> sorted = new ArrayList<>(objects);
        Collections.sort(sorted, new Comparator<QuizMatch>() {
            @Override
            public int compare(QuizMatch t1, QuizMatch t2) {
                return Boolean.compare(t2.isActive(), t1.isActive());
            }
        });
        List<ListItem> items = new ArrayList<>(sorted.size() + 2);

        if (!sorted.isEmpty() && sorted.get(0).isActive()) {
            items.add(new QuizMatchListHeader(context.getString(R.string.active_matches)));
        }
        boolean headerAdded = false;
        for (QuizMatch m : sorted) {
            if (!headerAdded && !m.isActive()) {
                items.add(new QuizMatchListHeader(context.getString(R.string.previous_matches)));
                headerAdded = true;
            }
            items.add(new QuizMatchListItem(context, m, me));
        }
        return items;
    }

    private Context context;

    public QuizMatchAdapter(@NonNull Context context, List<ListItem> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public int getViewTypeCount() {
        return ListItem.RowType.values().length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getItem(position).getView(LayoutInflater.from(context), convertView);
    }
}