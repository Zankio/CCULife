package org.zankio.cculife.ui.Ecourse;


import android.annotation.TargetApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.services.DownloadService;
import org.zankio.cculife.ui.Base.BasePage;

public class CourseFilePage extends BasePage {

    private Ecourse.Course course;
    private ExpandableListView list;
    private FileAdapter adapter;

    private static Ecourse.FileList[] _file;
    private static Ecourse.Course _course;
    private static LoadFileDataAsyncTask _fileTask;

    public CourseFilePage(LayoutInflater inflater, Ecourse.Course course) {
        super(inflater);
        this.course = course;

        if (_course != course) {
            if (_fileTask != null) _fileTask.cancel(false);
            _fileTask = null;
            _file = null;

            _course = course;
        }
    }

    @Override
    protected View createView() {
        return inflater.inflate(R.layout.fragment_course_file, null);
    }

    @Override
    public View getMainView() {
        return PageView.findViewById(R.id.list);
    }

    @Override
    public void initViews() {
        adapter = new FileAdapter();

        list = (ExpandableListView) PageView.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnChildClickListener(
                new ExpandableListView.OnChildClickListener() {
                    @Override
                    @TargetApi(11)
                    //@TargetApi(Build.VERSION_CODES.GINGERBREAD)
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        Ecourse.File file;
                        String filename;
                        file = (Ecourse.File) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
                        assert file != null;
                        filename = file.Name != null ? file.Name : URLUtil.guessFileName(file.URL, null, null);
                        DownloadService.downloadFile(inflater.getContext(), file.URL, filename);

                        Toast.makeText(inflater.getContext(), "下載 : " + filename, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
        );

        getData();
    }


    public class LoadFileDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Ecourse.FileList[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showMessage("讀取中...", true);
        }

        @Override
        protected Ecourse.FileList[] _doInBackground(Void... params) throws Exception {
            if (course == null) throw new Exception("請重試...");
            return course.getFiles();
        }

        @Override
        protected void onError(String msg) {
            showMessage(msg);
        }

        @Override
        protected void _onPostExecute(Ecourse.FileList[] result) {
            onDataLoaded(result);
        }
    }

    private void getData() {
        if (_file == null) {
            new LoadFileDataAsyncTask().execute();
        } else {
            onDataLoaded(_file);
        }
    }

    private void onDataLoaded(Ecourse.FileList[] files) {
        if (files == null || files.length == 0) {
            showMessage("沒有檔案");
            return;
        }

        _file = files;

        adapter.setFiles(files);
        if (files.length == 1) {
            list.setGroupIndicator(null);
            list.expandGroup(0);

        }
        hideMessage();
    }

    public class FileAdapter extends BaseExpandableListAdapter {
        private Ecourse.FileList[] filelists;

        public void setFiles(Ecourse.FileList[] filelists) {
            this.filelists = filelists;
            this.notifyDataSetChanged();
        }

        @Override
        public int getGroupCount() {
            return filelists == null ? 0 : filelists.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return filelists[groupPosition].Files.length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return filelists[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return filelists[groupPosition].Files[childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            Ecourse.FileList group = (Ecourse.FileList) getGroup(groupPosition);
            View view;

            if (convertView == null)
                view = inflater.inflate(R.layout.item_file_group, null);
            else
                view = convertView;

            ((TextView) view.findViewById(R.id.Name)).setText(group.Name);
            if (getGroupCount() == 1)
                view.setLayoutParams(new AbsListView.LayoutParams(1, 1));
            else
                view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Ecourse.File file = (Ecourse.File) getChild(groupPosition, childPosition);
            View view;

            if (convertView == null)
                view = inflater.inflate(R.layout.item_file, null);
            else
                view = convertView;

            ((TextView) view.findViewById(R.id.Name)).setText(file.Name);
            ((TextView) view.findViewById(R.id.Size)).setText(file.Size != null ? file.Size : "");

            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
    /*public class FileAdapter extends BaseExpandableListAdapterAdapter {

        private Ecourse.FileList[] files;

        public void setFiles(Ecourse.FileList[] files){
            this.files = files;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return files == null ? 0 : files.length;
        }

        @Override
        public Object getItem(int position) {
            return files == null ? null : files[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Ecourse.File file = (Ecourse.File) getItem(position);
            View view;

            if(convertView == null)
                view = inflater.inflate(R.layout.item_file, null);
            else
                view = convertView;

            ((TextView)view.findViewById(R.id.Name)).setText(file.Name);
            ((TextView)view.findViewById(R.id.Size)).setText(file.Size != null ? file.Size : "");

            return view;
        }
    }*/

}
