package com.tao.time.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义RecyclerView
 */
public class DayPickerView extends RecyclerView {
    protected Context mContext;
    protected SimpleMonthAdapter mAdapter;
    private DatePickerController mController;
    protected int mCurrentScrollState = 0;
    protected long mPreviousScrollPosition;
    protected int mPreviousScrollState = 0;
    private TypedArray typedArray;
    private OnScrollListener onScrollListener;

    private DataModel dataModel;

    public DayPickerView(Context context) {
        this(context, null);
    }

    public DayPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DayPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.DayPickerView);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        init(context);
    }

    public void init(Context paramContext) {
        setLayoutManager(new LinearLayoutManager(paramContext));
        mContext = paramContext;
        setUpListView();

        onScrollListener = new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final SimpleMonthView child = (SimpleMonthView) recyclerView.getChildAt(0);
                if (child == null) {
                    return;
                }

                mPreviousScrollPosition = dy;
                mPreviousScrollState = mCurrentScrollState;
            }
        };
    }

    protected void setUpAdapter() {
        if (mAdapter == null) {
            mAdapter = new SimpleMonthAdapter(getContext(), typedArray, mController, dataModel);
            setAdapter(mAdapter);
        }
        mAdapter.notifyDataSetChanged();
    }

    protected void setUpListView() {
        setVerticalScrollBarEnabled(false);
        setOnScrollListener(onScrollListener);
        setFadingEdgeLength(0);
    }

    public void setParameter(DataModel dataModel) {
        if (dataModel == null) {
            return;
        }
        this.dataModel = dataModel;
        setUpAdapter();
        // 跳转到入住日期所在的月份
        scrollToSelectedPosition(dataModel.selectedDays, dataModel.monthStart,dataModel.yearStart);
    }

    /**
     * 设置参数
     *
     * @param dataModel   数据
     * @param mController 回调监听
     */
    public void setParameter(DataModel dataModel, DatePickerController mController) {
        if (dataModel == null) {
//            Log.e("crash", "请设置参数");
            return;
        }
        this.dataModel = dataModel;
        this.mController = mController;
        setUpAdapter();
        // 跳转到入住日期所在的月份
        scrollToSelectedPosition(dataModel.selectedDays, dataModel.monthStart,dataModel.yearStart);
    }

    private void scrollToSelectedPosition(SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays, int monthStart,int yearStart) {
        try {
            int position = 0;
            if (selectedDays != null && selectedDays.getFirst() != null) {
                if (selectedDays.getFirst().year > yearStart) {
                    if (selectedDays.getFirst().month > monthStart) {
                        position = 12 + (selectedDays.getFirst().month - monthStart);
                    } else if (selectedDays.getFirst().month < monthStart) {
                        position = 12 - (monthStart - selectedDays.getFirst().month);
                    } else if (selectedDays.getFirst().month == monthStart) {
                        position = 12;
                    }
                } else if (selectedDays.getFirst().year == yearStart && selectedDays.getFirst().month > monthStart) {
                    position = selectedDays.getFirst().month - monthStart;
                }

                scrollToPosition(position);
            }
        }catch (Exception e){

        }
    }

    public static class DataModel implements Serializable {
        //TYPE_MULTI：多选，TYPE_RANGE：范围选，TYPE_DAY_NUMBER：单选，TYPE_CONTINUOUS 连续选择
        public enum TYPE {
            TYPE_MULTI, TYPE_RANGE, TYPE_DAY_NUMBER, TYPE_CONTINUOUS
        }

        public TYPE mTimeType;                                          // 类型
        public List<SimpleMonthAdapter.CalendarDay> invalidDays;        // 无效的日期
        public List<SimpleMonthAdapter.CalendarDay> busyDays;           // 被占用的日期
        public List<SimpleMonthAdapter.CalendarDay> mMoreDays;          //多选的日期
        public List<SimpleMonthAdapter.CalendarDay> tags;               // 日期下面对应的标签
        public SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays;  // 默认选择的日期

        public int yearStart;                                           // 日历开始的年份
        public int monthStart;                                          // 日历开始的月份
        public int monthCount;                                          // 要显示几个月
        public int leastDaysNum;                                        // 至少选择几天
        public int mostDaysNum;                                         // 最多选择几天
        public String defTag;                                           // 默认显示的标签

        public boolean isToDayOperation;                                // 今天日期是否可以操作

        public int numberOfDays;                                       //天数

    }
}