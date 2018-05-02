package com.tao.time;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.tao.time.calendar.DatePickerController;
import com.tao.time.calendar.DayPickerView;
import com.tao.time.calendar.SimpleMonthAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        DayPickerView daypickeview = findViewById(R.id.daypickeview);

        Calendar c = Calendar.getInstance();//首先要获取日历对象

        DayPickerView.DataModel dataModel = new DayPickerView.DataModel();

        SimpleMonthAdapter.CalendarDay startDay = new SimpleMonthAdapter.CalendarDay(2019, 19, 5);
        SimpleMonthAdapter.CalendarDay endDay = new SimpleMonthAdapter.CalendarDay(2019, 19, 20);
        // 默认选择的日期
        SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays = new SimpleMonthAdapter.SelectedDays<>(startDay, endDay);
        dataModel.selectedDays = selectedDays;


        // 被占用的日期
        List<SimpleMonthAdapter.CalendarDay> list = new ArrayList();
        list.add(new SimpleMonthAdapter.CalendarDay(2018, 4, 26));
        dataModel.busyDays = list;

        //TYPE_MULTI：多选，TYPE_RANGE：范围选，TYPE_DAY_NUMBER：单选，TYPE_CONTINUOUS 连续选择
        dataModel.mTimeType = DayPickerView.DataModel.TYPE.TYPE_RANGE;//选择
        dataModel.yearStart = 2018;//开始年份
        dataModel.monthStart = c.get(Calendar.MONTH);//开始的月份
        dataModel.monthCount = 24;//要显示几个月
        dataModel.leastDaysNum = 2;//最小选择天数
        dataModel.mostDaysNum = 4;//最大选择天数
        dataModel.defTag = "标2";//默认显示的标签
        dataModel.isToDayOperation = false;//今天日期能否被操作
        dataModel.numberOfDays = 2; //根据天数起始日期选择


        SimpleMonthAdapter.CalendarDay tag = new SimpleMonthAdapter.CalendarDay(1527436800000L, "你猜");
        SimpleMonthAdapter.CalendarDay tag2 = new SimpleMonthAdapter.CalendarDay(1524931259000L, "我不猜");
        List<SimpleMonthAdapter.CalendarDay> tags = new ArrayList<>();
        tags.add(tag);
        tags.add(tag2);
        dataModel.tags = tags;

        daypickeview.setParameter(dataModel, new DatePickerController() {

            @Override
            public void onDateSelected(List<SimpleMonthAdapter.CalendarDay> selectedDays) {
                Log.i("aaa", "111选择时间=" + selectedDays);
            }

            @Override
            public void alertSelected(int error) {
                Log.i("aaa", "错误代号=" + error);
            }
        });
    }
}
