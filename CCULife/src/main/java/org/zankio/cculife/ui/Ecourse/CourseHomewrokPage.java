package org.zankio.cculife.ui.Ecourse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Homework;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.Base.BasePage;

public class CourseHomewrokPage extends BasePage implements AdapterView.OnItemClickListener {

    private Ecourse.Course course;

    private static Ecourse.Course _course;
    private static Homework[] _homework;
    private static LoadHomeworkDataAsyncTask _homeworkTask;
    private HomeworkAdapter adapter;

    public CourseHomewrokPage(LayoutInflater inflater, Ecourse.Course course) {
        super(inflater);
        this.course = course;

        if(_course != course) {
            if(_homeworkTask != null) _homeworkTask.cancel(false);

            _homework = null;
            _homeworkTask = null;
            _course = course;
        }

    }



    @Override
    protected View createView() {
        return inflater.inflate(R.layout.fragment_course_homework, null);
    }

    @Override
    public View getMainView() {
        return PageView.findViewById(R.id.list);
    }

    @Override
    public void initViews() {

        adapter = new HomeworkAdapter();

        ListView list = (ListView) PageView.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        getData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Homework homework = (Homework) parent.getAdapter().getItem(position);
        new LoadHomeworkContentAnsyncTask().execute(homework);
    }

    public class LoadHomeworkContentAnsyncTask extends AsyncTask<Homework, Void, Homework>{

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Homework doInBackground(Homework... params) {
            params[0].getContent();
            return params[0];
        }

        @Override
        protected void onPostExecute(Homework homework) {
            Context context = inflater.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            TextView message = new TextView(context);
            String err = homework.getContent();
            String content = null, url = null;
            if (err != null)
                content = err;
            else {
                switch (homework.getContentType()) {
                    case 1:
                        url = homework.contentUrl;
                        break;
                    case 0:
                        content = homework.content;
                        break;
                    default:
                        content = "讀取錯誤";
                }
            }

            if (url != null) {
                inflater.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(homework.contentUrl)));
            } else if (content != null) {
                message.setText(Html.fromHtml(content));
                message.setAutoLinkMask(Linkify.WEB_URLS);
                message.setMovementMethod(LinkMovementMethod.getInstance());
                message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                message.setPadding(20, 20, 20, 20);
                builder.setView(message);

                //builder.setMessage(Html.fromHtml(announce.getContent()));
                builder.setTitle(homework.title);
                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final AlertDialog dialog = builder.create();
                if (context instanceof Activity && ((Activity)context).isFinishing()) return;
                dialog.show();
            } else {
                Toast.makeText(context, "讀取題目錯誤", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class LoadHomeworkDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Homework[]> {

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
        protected Homework[] _doInBackground(Void... params) throws Exception {
            return course.getHomework();
        }

        @Override
        protected void _onPostExecute(Homework[] result){
            onDataLoaded(result);
        }
    }

    private void getData() {
        if (_homework == null) {
            new LoadHomeworkDataAsyncTask().execute();
        } else {
            onDataLoaded(_homework);
        }
    }

    private void onDataLoaded(Homework[] homework) {
        if(homework == null || homework.length == 0) {
            showMessage("沒有作業");
            return;
        }
        _homework = homework;

        adapter.setHomeworks(homework);
        hideMessage();
    }


    public class HomeworkAdapter extends BaseAdapter {

        private Homework[] homeworks;

        public void setHomeworks(Homework[] homework){
            this.homeworks = homework;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return homeworks == null ? 0 : homeworks.length;
        }

        @Override
        public Object getItem(int position) {
            return homeworks == null ? null : homeworks[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Homework homework = (Homework) getItem(position);

            View view = convertView == null ? inflater.inflate(R.layout.item_homework, null) : convertView;
            ((TextView)view.findViewById(R.id.Title)).setText(homework.title);
            ((TextView)view.findViewById(R.id.Deadline)).setText(homework.deadline);
            ((TextView)view.findViewById(R.id.Score)).setText(homework.score);

            return view;
        }
    }

}
