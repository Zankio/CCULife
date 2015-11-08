package org.zankio.cculife.ui.CCUSchedule;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;

import org.zankio.cculife.CCUSchedule;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.Base.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CCUScheduleActivity extends BaseActivity {

    private ScheduleAdapter adapter;
    private ListView listView;
    private int TODAY_DAY_OF_YEAR = -1;
    private int TODAY_YEAR =  -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccu_schedule);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new ScheduleAdapter();

        listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(adapter);

        Calendar today = Calendar.getInstance();
        TODAY_DAY_OF_YEAR = today.get(Calendar.DAY_OF_YEAR);
        TODAY_YEAR = today.get(Calendar.YEAR);

        new LoadDataAsyncTask().execute();
    }



    public class LoadDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, CCUSchedule.Schedule[]> {

        private CCUSchedule schedule = null;
        @Override
        protected void onError(String msg) {
            showMessage(msg);
        }

        @Override
        protected void _onPostExecute(CCUSchedule.Schedule[] result) {
            if(result == null || result.length == 0) {
                showMessage("沒有日程");
                return;
            }


            int i, length = 0;
            for (i = 0; i < result.length; i++) {
                length += result[i].list.length;
            }
            CCUSchedule.Item[] list = new CCUSchedule.Item[length + result.length];

            i = 0;
            for (CCUSchedule.Schedule s : result) {
                CCUSchedule.Item header = schedule.new Item();
                header.Title = s.Name;
                header.Date = s.list[0].Date;
                list[i++] = header;

                for (CCUSchedule.Item item : s.list) {
                    list[i++] = item;
                }
            }

            adapter.setItems(list);
            hideMessage();

            scrollToNow(list);
        }

        @Override
        protected CCUSchedule.Schedule[] _doInBackground(Void... params) throws Exception {
            schedule = new CCUSchedule(CCUScheduleActivity.this);
            return schedule.getScheduleList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.ccuschedule, menu);
        return true;
    }

    public void scrollToNow(CCUSchedule.Item[] list) {
        Calendar now = Calendar.getInstance();
        now = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        int i;
        for (i = list.length - 1; i >= 0 ; i--) {
            if(now.after(list[i].Date)) break;
        }
        listView.setSelection(i >= 0 ? i + 1 : 0);
    }


    public class ScheduleAdapter extends BaseAdapter {

        private CCUSchedule.Item[] items;
        private
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM / dd");
        private static final String weekName = "日一二三四五六";

        public void setItems(CCUSchedule.Item[] items){
            this.items = items;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.length;
        }

        @Override
        public Object getItem(int position) {
            return items == null ? null : items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) CCUScheduleActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            CCUSchedule.Item item = (CCUSchedule.Item) getItem(position);
            View view = convertView;
            String DateString = String.format("%s (%s)", simpleDateFormat.format(item.Date.getTime()), weekName.charAt(item.Date.get(Calendar.DAY_OF_WEEK) - 1));

            if (view == null) {
                view = inflater.inflate(R.layout.item_ccu_schedule, null);
            }

            if (item.Date.get(Calendar.YEAR) == TODAY_YEAR && item.Date.get(Calendar.DAY_OF_YEAR) == TODAY_DAY_OF_YEAR){
                view.setBackgroundColor(inflater.getContext().getResources().getColor(R.color.Today));
            } else {
                view.setBackgroundColor(0);
            }

            if(position == 0 || item.Date.compareTo(items[position - 1].Date) != 0) {
                ((TextView)view.findViewById(R.id.Date)).setText(DateString);
            } else {
                ((TextView)view.findViewById(R.id.Date)).setText("");
            }

            ((TextView)view.findViewById(R.id.Title)).setText(item.Title);
            return view;
        }
    }
}
