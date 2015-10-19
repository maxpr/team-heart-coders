package ch.epfl.sweng.swissaffinity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private final static List<String> HEADERS =
            Arrays.asList(new String[]{"My Events :", "Upcoming Events :"});

    private ExpandableListAdapter<String, String> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createData();

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.mainEventListView);
        listView.setAdapter(mListAdapter);
        listView.expandGroup(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void createData() {
        mListAdapter = new ExpandableListAdapter<String, String>(this);

        for (int i = 1; i < 3; i++) {
            for (String header : HEADERS) {
                mListAdapter.addChild(header, "Test" + i);
            }
        }
    }
}
