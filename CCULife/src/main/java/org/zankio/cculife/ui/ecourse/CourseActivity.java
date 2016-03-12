package org.zankio.cculife.ui.ecourse;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseFragmentActivity;
import org.zankio.cculife.ui.base.IGetCourseData;

public class CourseActivity extends BaseFragmentActivity
    implements CourseListFragment.OnCourseSelectedListener, IGetCourseData{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        if (findViewById(R.id.container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            CourseListFragment courseListFragment = new CourseListFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, courseListFragment).commit();
        }
    }

    @Override
    public Ecourse getEcourse() {
        return CourseDataFragment.getFragment(getSupportFragmentManager()).getEcourse();
    }

    @Override
    public Course getCourse(String id) {
        return CourseDataFragment.getFragment(getSupportFragmentManager()).getCourse(id);
    }

    @Override
    public void onCourseSelected(Ecourse ecourse, Course course) {
        CourseDataFragment.getFragment(getSupportFragmentManager()).onCourseSelected(ecourse, course);

        // change course
        CourseFragment fragment = new CourseFragment();
        Bundle data = new Bundle();
        data.putString("id", course.courseid);
        fragment.setArguments(data);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack("list");

        transaction.commitAllowingStateLoss();
    }
}
