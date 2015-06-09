package com.michaelchen.wearlogger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class ClassSelectorActivity extends Activity implements WearableListView.ClickListener {

    private WearableListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mListView = (WearableListView) stub.findViewById(R.id.listView1);
                mListView.setAdapter(new ClassificationSelectionAdapter(ClassSelectorActivity.this));
                mListView.setClickListener(ClassSelectorActivity.this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_class_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static ArrayList<String> classMethods;
    static {
        classMethods = new ArrayList<>();
        classMethods.add("KNN");
        classMethods.add("Cluster");
        classMethods.add("NN w/Thresh");
        classMethods.add("K Perceptron");
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        setResult(position);
        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    private class ClassificationSelectionAdapter extends WearableListView.Adapter {

        private final LayoutInflater mInflater;

        private ClassificationSelectionAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.list_simple, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int i) {
            TextView textView = (TextView) viewHolder.itemView.findViewById(R.id.listTextView);
            textView.setText(classMethods.get(i).toString());
            viewHolder.itemView.setTag(i);
        }

        @Override
        public int getItemCount() {
            return classMethods.size();
        }
    }
}
