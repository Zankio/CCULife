package org.zankio.cculife.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;

import org.zankio.cculife.CCUService.base.helper.ConnectionHelper;
import org.zankio.cculife.R;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Net;
import org.zankio.cculife.ui.Base.BaseActivity;
import org.zankio.cculife.ui.CCUSchedule.CCUScheduleActivity;
import org.zankio.cculife.ui.CourseSchedule.CourseTimeTableActivity;
import org.zankio.cculife.ui.Ecourse.CourseListActivity;
import org.zankio.cculife.ui.ScoreQuery.ScoreQueryActivity;

public class HomeActivity extends BaseActivity {

    private static final int ACTIVITY_LOGIN = 1;

    private CCUService loadServices;
    private SessionManager sessionManager;
    private CCUService[] ccuServices = {
            new CCUService(CourseListActivity.class, "Ecourse", R.drawable.ecourse, true)
          , new CCUService(CourseTimeTableActivity.class, "課表", R.drawable.schedule, true)
          , new CCUService(ScoreQueryActivity.class, "成績查詢", R.drawable.score, true)
          , new CCUService(CCUScheduleActivity.class, "行事曆", R.drawable.ccuschedule)
          //, new CCUService(null, "飲食", null)
          //, new CCUService(null, "選課", null, true)
          //, new CCUService(null, "工讀生", null, true)
          //, new CCUService(null, "Wifi 自動連線", null)

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ConnectionHelper.setSSLSocketFactory(Net.generateSSLSocketFactory(this));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("CCULife");
        actionBar.setSubtitle("Enjoy your life!");

        sessionManager = SessionManager.getInstance(this);

        GridView serviceView = (GridView)findViewById(R.id.gridView);
        serviceView.setAdapter(new CCUServiceAdaper(ccuServices));
        serviceView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadServices = (CCUService)parent.getAdapter().getItem(position);
                if(loadServices.needLogin && !sessionManager.isLogined()) {
                    startActivityForResult(new Intent(HomeActivity.this, LoginActivity.class), ACTIVITY_LOGIN);
                } else if (loadServices.Activity != null) {
                    startActivity(new Intent(HomeActivity.this, loadServices.Activity));
                }
            }
        });

        // new Updater(this).checkUpdate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_LOGIN) {
            if(resultCode == RESULT_OK && loadServices.Activity != null)
                startActivity(new Intent(HomeActivity.this, loadServices.Activity));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    public class CCUService {
        public String Title;
        public int Image;
        public Class<?> Activity;
        public boolean needLogin = false;
        public CCUService (Class<?> activity, String title, int image) {
            this(activity, title, image, false);
        }
        public CCUService (Class<?> activity, String title, int image, boolean needLogin) {
            this.Title = title;
            this.Activity = activity;
            this.Image = image;
            this.needLogin = needLogin;
        }

    }

    public class CCUServiceAdaper extends BaseAdapter {

        private CCUService[] services;

        public CCUServiceAdaper (CCUService[] services) {
            this.services = services;
        }

        @Override
        public int getCount() {
            return services.length;
        }

        @Override
        public Object getItem(int position) {
            return services[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) HomeActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view;

            CCUService service = services[position];
            if (convertView == null)
                view = inflater.inflate(R.layout.item_home, null);
            else
                view = convertView;

            ((TextView)view.findViewById(R.id.title)).setText(service.Title);
            ((ImageView)view.findViewById(R.id.icon)).setImageResource(service.Image);
            return view;
        }
    }

}
