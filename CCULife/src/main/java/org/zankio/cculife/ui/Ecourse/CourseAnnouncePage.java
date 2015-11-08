package org.zankio.cculife.ui.Ecourse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.Base.BasePage;

public class CourseAnnouncePage extends BasePage implements AdapterView.OnItemClickListener {

    private Ecourse.Course course;

    private static Ecourse.Course _course;
    private static Ecourse.Announce[] _announces;
    private static LoadAnnounceDataAsyncTask _announceTask;
    private AnnounceAdapter adapter;

    public CourseAnnouncePage(LayoutInflater inflater, Ecourse.Course course) {
        super(inflater);
        this.course = course;

        if(_course != course) {
            if(_announceTask != null) _announceTask.cancel(false);

            _announces = null;
            _announceTask = null;
            _course = course;
        }

    }



    @Override
    protected View createView() {
        return inflater.inflate(R.layout.fragment_course_announce, null);
    }

    @Override
    public View getMainView() {
        return PageView.findViewById(R.id.list);
    }

    @Override
    public void initViews() {

        adapter = new AnnounceAdapter();

        ListView list = (ListView) PageView.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        getData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Ecourse.Announce announce = (Ecourse.Announce) parent.getAdapter().getItem(position);
        new LoadAnnounceContentAnsyncTask().execute(announce);
    }

    public class LoadAnnounceContentAnsyncTask extends AsyncTask<Ecourse.Announce, Void, Ecourse.Announce>{

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Ecourse.Announce doInBackground(Ecourse.Announce... params) {
            params[0].getContent();
            return params[0];
        }

        @Override
        protected void onPostExecute(Ecourse.Announce announce) {
            Context context = inflater.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            TextView message = new TextView(inflater.getContext());
            if (announce.getContent() != null) {
                message.setText(Html.fromHtml(announce.getContent()));
            }
            message.setAutoLinkMask(Linkify.WEB_URLS);
            message.setMovementMethod(LinkMovementMethod.getInstance());
            message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            message.setPadding(20, 20, 20, 20);
            builder.setView(message);

            //builder.setMessage(Html.fromHtml(announce.getContent()));
            builder.setTitle(announce.Title);
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog dialog = builder.create();
            if (context instanceof Activity && ((Activity)context).isFinishing()) return;
            dialog.show();

        }
    }

    public class LoadAnnounceDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Ecourse.Announce[]> {

        @Override
        protected void onError(String msg) {
            showMessage(msg);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showMessage("讀取中...", true);
        }

        @Override
        protected Ecourse.Announce[] _doInBackground(Void... params) throws Exception {
            return course.getAnnounces();
        }

        @Override
        protected void _onPostExecute(Ecourse.Announce[] result){
            onDataLoaded(result);
        }
    }

    private void getData() {
        if (_announces == null) {
            new LoadAnnounceDataAsyncTask().execute();
        } else {
            onDataLoaded(_announces);
        }
    }

    private void onDataLoaded(Ecourse.Announce[] announces) {
        if(announces == null || announces.length == 0) {
            showMessage("沒有公告");
            return;
        }
        _announces = announces;

        adapter.setAnnounces(announces);
        hideMessage();
    }


    public class AnnounceAdapter extends BaseAdapter {

        private Ecourse.Announce[] announces;

        public void setAnnounces(Ecourse.Announce[] announces){
            this.announces = announces;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return announces == null ? 0 : announces.length;
        }

        @Override
        public Object getItem(int position) {
            return announces == null ? null : announces[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Ecourse.Announce announce = (Ecourse.Announce) getItem(position);

            View view = inflater.inflate(R.layout.item_announce, null);
            ((TextView)view.findViewById(R.id.Title)).setText(announce.Title);
            ((TextView)view.findViewById(R.id.Date)).setText(announce.Date);
            if (announce.isnew) view.setBackgroundColor(inflater.getContext().getResources().getColor(R.color.Unread));
            else view.setBackgroundColor(0);

            return view;
        }
    }

}
