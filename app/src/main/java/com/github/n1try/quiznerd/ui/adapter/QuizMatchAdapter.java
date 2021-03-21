package com.github.n1try.quiznerd.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.ui.adapter.entity.ListItem;
import com.github.n1try.quiznerd.ui.adapter.entity.QuizMatchListHeader;
import com.github.n1try.quiznerd.ui.adapter.entity.QuizMatchListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizMatchAdapter extends ArrayAdapter<ListItem> {

    public static List<ListItem> generateItemList(Context context, List<QuizMatch> objects, final QuizUser me) {
        List<QuizMatch> sorted = new ArrayList<>(objects);
        Collections.sort(sorted, (t1, t2) -> {
            if (t1.isActive() != t2.isActive()) return Boolean.compare(t1.isActive(), t2.isActive());
            boolean myTurn1 = t1.isMyTurn(me);
            boolean myTurn2 = t2.isMyTurn(me);
            if (myTurn1 != myTurn2) return Boolean.compare(myTurn2, myTurn1);
            return t2.getUpdated().compareTo(t1.getUpdated());
        });
        List<ListItem> items = new ArrayList<>(sorted.size() + 1);

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

    private final Context context;

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