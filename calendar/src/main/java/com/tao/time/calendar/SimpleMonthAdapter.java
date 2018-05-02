package com.tao.time.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class SimpleMonthAdapter extends RecyclerView.Adapter<SimpleMonthAdapter.ViewHolder> implements SimpleMonthView.OnDayClickListener {
    protected static final int MONTHS_IN_YEAR = 12;
    protected static final int ERROR_ONE = 1;
    protected static final int ERROR_TWO = 2;
    protected static final int ERROR_THREE = 3;
    protected static final int ERROR_FOUR = 4;
    protected static final int ERROR_FIVE = 5;
    private final TypedArray typedArray;
    private final Context mContext;
    private final DatePickerController mController;             // 回调
    private Calendar mCalendar;
    private SelectedDays<CalendarDay> rangeDays;                // 选择日期
    private List<CalendarDay> mMoreDays;     //多选的日期

    private List<CalendarDay> mBusyDays;                        // 被占用的日期
    private List<CalendarDay> mTags;                            // 日期下面的标签
    private String mDefTag;                                     // 默认标签

    private int mLeastDaysNum;                                  // 至少选择几天
    private int mMostDaysNum;                                   // 至多选择几天

    private List<CalendarDay> mInvalidDays;                     // 无效的日期

    private CalendarDay mNearestDay;                            // 比离入住日期大且是最近的已被占用或者无效日期

    private DayPickerView.DataModel dataModel;

    public SimpleMonthAdapter(Context context, TypedArray typedArray, DatePickerController datePickerController, DayPickerView.DataModel dataModel) {
        mContext = context;
        this.typedArray = typedArray;
        mController = datePickerController;
        this.dataModel = dataModel;

//        // 今天是否默认选中
//        if (typedArray.getBoolean(R.styleable.DayPickerView_currentDaySelected, false))
//            onDayTapped(new CalendarDay(System.currentTimeMillis()));

        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mCalendar = Calendar.getInstance();

        if (dataModel.invalidDays == null) {
            dataModel.invalidDays = new ArrayList<>();
        }

        if (dataModel.busyDays == null) {
            dataModel.busyDays = new ArrayList<>();
        }

        if (dataModel.mMoreDays == null) {
            dataModel.mMoreDays = new ArrayList<>();
        }

        if (dataModel.tags == null) {
            dataModel.tags = new ArrayList<>();
        }

        if (dataModel.selectedDays == null) {
            dataModel.selectedDays = new SelectedDays<>();
        }

        if (dataModel.yearStart <= 0) {
            dataModel.yearStart = mCalendar.get(Calendar.YEAR);
        }
        if (dataModel.monthStart <= 0) {
            dataModel.monthStart = mCalendar.get(Calendar.MONTH);
        }

        if (dataModel.leastDaysNum <= 0) {
            dataModel.leastDaysNum = 0;
        }

        if (dataModel.mostDaysNum <= 0) {
            dataModel.mostDaysNum = 100;
        }

        if (dataModel.leastDaysNum > dataModel.mostDaysNum) {
            throw new IllegalArgumentException("可选择的最小天数不能小于最大天数");
        }

        //获取设置显示几个月
        if (dataModel.monthCount <= 0) {
            dataModel.monthCount = 12;
        }

        if (dataModel.defTag == null) {
            dataModel.defTag = "标签";
        }

        mLeastDaysNum = dataModel.leastDaysNum;
        mMostDaysNum = dataModel.mostDaysNum;

        mBusyDays = dataModel.busyDays;
        //判断今天日期能否被操作
        if (!dataModel.isToDayOperation) {
            mBusyDays.add(new CalendarDay(System.currentTimeMillis()));
        }

        mInvalidDays = dataModel.invalidDays;
        rangeDays = dataModel.selectedDays;

        mMoreDays = dataModel.mMoreDays;
        mTags = dataModel.tags;
        mDefTag = dataModel.defTag;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final SimpleMonthView simpleMonthView = new SimpleMonthView(mContext, typedArray, dataModel);
        return new ViewHolder(simpleMonthView, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final SimpleMonthView v = viewHolder.simpleMonthView;
        final ArrayMap<String, Object> drawingParams = new ArrayMap<>();
        int month;          // 月份
        int year;           // 年份

        int monthStart = dataModel.monthStart;
        int yearStart = dataModel.yearStart;

        month = (monthStart + (position % MONTHS_IN_YEAR)) % MONTHS_IN_YEAR;
        year = position / MONTHS_IN_YEAR + yearStart + ((monthStart + (position % MONTHS_IN_YEAR)) / MONTHS_IN_YEAR);


//        v.reuse();

        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_BEGIN_DATE, rangeDays.getFirst());//开始日期
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_LAST_DATE, rangeDays.getLast());//结束日期
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_NEAREST_DATE, mNearestDay);//无效日期
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_YEAR, year);//年份
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_MONTH, month);//月份
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_WEEK_START, mCalendar.getFirstDayOfWeek());//返回一周的第一天。
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_MORE_DATE, mMoreDays);//年份
        v.setMonthParams(drawingParams);
        v.invalidate();
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return dataModel.monthCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final SimpleMonthView simpleMonthView;

        public ViewHolder(View itemView, SimpleMonthView.OnDayClickListener onDayClickListener) {
            super(itemView);
            simpleMonthView = (SimpleMonthView) itemView;
            simpleMonthView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            simpleMonthView.setClickable(true);
            simpleMonthView.setOnDayClickListener(onDayClickListener);
        }
    }

    /**
     * 时间监听
     *
     * @param simpleMonthView
     * @param calendarDay
     */
    @Override
    public void onDayClick(SimpleMonthView simpleMonthView, CalendarDay calendarDay) {
            monDayTapped(calendarDay);
    }

    /**
     * 时间监听
     *
     * @param calendarDay 点击的时间对象
     */
    protected void monDayTapped(CalendarDay calendarDay) {
        if (dataModel.mTimeType == null || calendarDay == null || mController == null )
            return;

        if (dataModel.mTimeType.equals(DayPickerView.DataModel.TYPE.TYPE_MULTI)) {//如果是多选

            //多选状态下最大和最小限制是无效的
            multiSelectedDay(calendarDay);

        } else if (dataModel.mTimeType.equals(DayPickerView.DataModel.TYPE.TYPE_RANGE)) {//范围选

            rangeSelectedDay(calendarDay);

        } else if (dataModel.mTimeType.equals(DayPickerView.DataModel.TYPE.TYPE_DAY_NUMBER)) {//单选

            singleSelectedDay(calendarDay);

        } else if (dataModel.mTimeType.equals(DayPickerView.DataModel.TYPE.TYPE_CONTINUOUS)) {//根据天数连续选择

            if (dataModel.numberOfDays == 0) {
                if (mController != null) {
                    mController.alertSelected(ERROR_FIVE);
                    return;
                }
            } else if (dataModel.numberOfDays == 1) {
                singleSelectedDay(calendarDay);
            } else {
                continuousSelectedDay(calendarDay);


            }


        }

    }


    /**
     * 多选
     *
     * @param calendarDay
     */
    private void multiSelectedDay(CalendarDay calendarDay) {


        for (CalendarDay multiDay : mMoreDays) {
            if (multiDay.equals(calendarDay)) {
                mMoreDays.remove(calendarDay);
                notifyDataSetChanged();
                return;
            }
        }


        //如果日期被占用
        if (getIsTakeUpDay(calendarDay)) {
            if (mController != null) {
                mController.alertSelected(ERROR_ONE);
            }
        } else {
            if (mController != null) {
                mMoreDays.add(calendarDay);
                mController.onDateSelected(mMoreDays);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 单选
     *
     * @param calendarDay
     */
    private void singleSelectedDay(CalendarDay calendarDay) {
        if (calendarDay == null)
            return;

        if (rangeDays.getFirst() != null && rangeDays.getFirst().equals(calendarDay)) {
            rangeDays.setFirst(null);
            notifyDataSetChanged();
            return;
        }
        //如果日期被占用
        if (getIsTakeUpDay(calendarDay)) {
            if (mController != null) {
                mController.alertSelected(ERROR_ONE);
            }
        } else {
            if (mController != null) {
                rangeDays.setFirst(calendarDay);
                mController.onDateSelected(addSingleDay());
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 范围选择
     *
     * @param calendarDay
     */
    private void rangeSelectedDay(CalendarDay calendarDay) {

        if (rangeDays.getFirst() != null && rangeDays.getLast() == null) {


            // 所选日期范围内是否有被占用的日期
            if (isContainSpecialDays(rangeDays.getFirst(), calendarDay, mBusyDays) || mBusyDays.contains(calendarDay) || mBusyDays.contains(rangeDays.getFirst())) {
                if (mController != null) {
                    mController.alertSelected(ERROR_TWO);
                }
                return;
            }

            // 如果结束日期在开始日期之前，刷新开始日期
            if (calendarDay.getDate().before(rangeDays.getFirst().getDate())) {
                if (mController != null) {
                    rangeDays.setFirst(calendarDay);
                    mController.onDateSelected(addFirst());
                    notifyDataSetChanged();
                }
                return;
            }

            int dayDiff = dateDiff(rangeDays.getFirst(), calendarDay);
            // 所选的日期范围不能小于最小限制
            if (dayDiff > 1 && mLeastDaysNum > dayDiff) {
                if (mController != null) {
                    mController.alertSelected(ERROR_THREE);
                }
                return;
            }
            // 所选日期范围不能大于最大限制
            if (dayDiff > 1 && mMostDaysNum < dayDiff) {
                if (mController != null) {
                    mController.alertSelected(ERROR_FOUR);
                }
                return;
            }


            rangeDays.setLast(calendarDay);
            // 把开始日期和结束日期中间的所有日期都加到list中
            if (mController != null) {
                if (!calendarDay.equals(rangeDays.getFirst())) {
                    mController.onDateSelected(addSelectedDays());
                }
            }

        } else if (rangeDays.getFirst() != null && rangeDays.getLast() != null) {   // 重新选择开始日期


            //如果日期被占用
            if (getIsTakeUpDay(calendarDay)) {
                if (mController != null)
                    mController.alertSelected(ERROR_ONE);

            } else {
                rangeDays.setFirst(calendarDay);
                rangeDays.setLast(null);
                if (mController != null)
                    mController.onDateSelected(addFirst());
            }
        } else {//第一次选择日期
            //如果日期被占用
            if (getIsTakeUpDay(calendarDay)) {
                if (mController != null) {
                    mController.alertSelected(ERROR_ONE);
                }
            } else {
                rangeDays.setFirst(calendarDay);
                if (mController != null) {
                    //添加开始时间监听
                    mController.onDateSelected(addFirst());
                }
            }
        }

        notifyDataSetChanged();
    }

    /**
     * 根据起始日期天数连续选择
     *
     * @param calendarDay
     */
    private void continuousSelectedDay(CalendarDay calendarDay) {

        if (rangeDays.getFirst() != null && rangeDays.getLast() != null) {
            rangeDays.setFirst(null);
            rangeDays.setLast(null);
            notifyDataSetChanged();
            return;
        }

        //如果起始日期被占用
        if (getIsTakeUpDay(calendarDay)) {
            if (mController != null) {
                mController.alertSelected(ERROR_ONE);
                return;
            }
        } else {
            //根据几天数得到几天后的时间
            CalendarDay afterDay = StringData(calendarDay.year, (calendarDay.month + 1), calendarDay.day, (dataModel.numberOfDays - 1));
            // 所选日期范围内是否有被占用的日期
            if (isContainSpecialDays(calendarDay, afterDay, mBusyDays) || mBusyDays.contains(calendarDay) || mBusyDays.contains(afterDay)) {
                if (mController != null) {
                    mController.alertSelected(ERROR_TWO);
                }
                return;
            }
            rangeDays.setFirst(calendarDay);
            rangeDays.setLast(afterDay);

            mController.onDateSelected(addSelectedDays());
        }

        notifyDataSetChanged();
    }

    // 计算三天后时间
    public static CalendarDay StringData(int mYear, int mMonth, int mNowDay, int add) {
        int mNextDay;
        String m_month;
        String m_day;

        String month, day;
        if (mMonth <= 9) {
            month = "0" + mMonth;
        } else {
            month = String.valueOf(mMonth);
        }
        if (mNowDay <= 9) {
            day = "0" + mNowDay;
        } else {
            day = String.valueOf(mNowDay);
        }

        final Calendar c = Calendar.getInstance();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        try {
            c.setTime(formatter.parse(mYear + month + day));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.set(Calendar.DATE, c.get(Calendar.DATE) + add);// 延后几天
        mNextDay = c.get(Calendar.DAY_OF_MONTH);

        // 判断延后的日期小于今天的日期，月份加一
        if (mNowDay > mNextDay) {
            mMonth += 1;
        }
        // 判断延后的月份大于本月的月份，月份设置为一月份，年份加一
        if (mMonth > 12) {
            mMonth = 1;
            mYear += 1;
        }
        // 测试今天的日期
//        com.puding.tigeryou.utils.Log.e("今日时间===>", mYear + "年" + mMonth + "月" + mNowDay + "日");
        // 如果 月份为个位数则加个0在前面
        if (mMonth < 10) {
            m_month = "0" + mMonth;
        } else {
            m_month = "" + mMonth;
        }
        // 如果 天数为个位数则加个0在前面
        if (mNextDay < 10) {
            m_day = "0" + mNextDay;
        } else {
            m_day = "" + mNextDay;
        }
        CalendarDay calendarDay = new CalendarDay();
        calendarDay.setDay(mYear, mMonth - 1, mNextDay);
        return calendarDay;
    }


    /**
     * 判断选择的日期是否是占用日期
     *
     * @param calendarDay 选择的日期
     * @return
     */
    protected boolean getIsTakeUpDay(CalendarDay calendarDay) {
        List<CalendarDay> list = new ArrayList<>();
        list.addAll(mBusyDays);
        Collections.sort(list);
        for (CalendarDay day : list) {
            if (day.equals(calendarDay)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 把比离入住日期大且是最近的已被占用或者无效日期找出来
     *
     * @param calendarDay 入住日期
     * @return
     */
    protected CalendarDay getNearestDay(CalendarDay calendarDay) {
        List<CalendarDay> list = new ArrayList<>();
        list.addAll(mBusyDays);
        list.addAll(mInvalidDays);
        Collections.sort(list);
        for (CalendarDay day : list) {
            if (calendarDay.compareTo(day) < 0) {
                return day;
            }
        }
        return null;
    }

    public SelectedDays<CalendarDay> getRangeDays() {
        return rangeDays;
    }

    /**
     * 判断选择的日期范围是否包含有特殊的日期（无效的或者已被占用的日期）
     *
     * @param first
     * @param last
     * @param specialDays
     * @return
     */
    protected boolean isContainSpecialDays(CalendarDay first, CalendarDay last, List<CalendarDay> specialDays) {
        Date firstDay = first.getDate();
        Date lastDay = last.getDate();
        for (CalendarDay day : specialDays) {
            //  after   当Date1大于Date2时，返回TRUE，当小于等于时，返回false；
            if (day.getDate().after(firstDay) && day.getDate().before(lastDay)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 两个日期中间隔多少天
     *
     * @param first
     * @param last
     * @return
     */
    protected int dateDiff(CalendarDay first, CalendarDay last) {
        long dayDiff = (last.getDate().getTime() - first.getDate().getTime()) / (1000 * 3600 * 24);
        return Integer.valueOf(String.valueOf(dayDiff)) + 1;
    }

    /**
     * 单选
     *
     * @return
     */
    protected List<CalendarDay> addSingleDay() {
        List<CalendarDay> ran = new ArrayList<>();
        ran.clear();
        CalendarDay firstDay = this.rangeDays.getFirst();
        ran.add(firstDay);
        return ran;
    }

    /**
     * 范围选择时，把设置初始时间
     *
     * @return
     */
    protected List<CalendarDay> addFirst() {
        List<CalendarDay> ran = new ArrayList<>();
        CalendarDay firstDay = this.rangeDays.getFirst();
        ran.add(firstDay);
        return ran;
    }

    /**
     * 范围选择时，把选中的所有日期加进list中
     *
     * @return
     */
    protected List<CalendarDay> addSelectedDays() {
        List<CalendarDay> rangeDays = new ArrayList<>();
        CalendarDay firstDay = this.rangeDays.getFirst();
        CalendarDay lastDay = this.rangeDays.getLast();
        rangeDays.add(firstDay);
        int diffDays = dateDiff(firstDay, lastDay);
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.setTime(firstDay.getDate());
        for (int i = 1; i < diffDays; i++) {
            tempCalendar.set(Calendar.DATE, tempCalendar.get(Calendar.DATE) + 1);
            CalendarDay calendarDay = new CalendarDay(tempCalendar);
            boolean isTag = false;
            for (CalendarDay calendarTag : mTags) {
                if (calendarDay.compareTo(calendarTag) == 0) {
                    isTag = true;
                    rangeDays.add(calendarTag);
                    break;
                }
            }
            if (!isTag) {
                calendarDay.tag = mDefTag;
                rangeDays.add(calendarDay);
            }
        }
        return rangeDays;
    }

    public static class CalendarDay implements Serializable, Comparable<CalendarDay> {
        private static final long serialVersionUID = -5456695978688356202L;
        private Calendar calendar;

        public int day;
        public int month;
        public int year;
        public String tag;

        public CalendarDay(Calendar calendar, String tag) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            this.tag = tag;
        }

        public CalendarDay() {
            setTime(System.currentTimeMillis());
        }

        public CalendarDay(int year, int month, int day) {
            setDay(year, month, day);
        }

        public CalendarDay(long timeInMillis) {
            setTime(timeInMillis);
        }

        public CalendarDay(long timeInMillis, String tag) {
            setTime(timeInMillis);
            this.tag = tag;
        }

        public CalendarDay(Calendar calendar) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        private void setTime(long timeInMillis) {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.setTimeInMillis(timeInMillis);
            month = this.calendar.get(Calendar.MONTH);
            year = this.calendar.get(Calendar.YEAR);
            day = this.calendar.get(Calendar.DAY_OF_MONTH);
        }

        public void set(CalendarDay calendarDay) {
            year = calendarDay.year;
            month = calendarDay.month;
            day = calendarDay.day;
        }

        public void setDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public Date getDate() {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.clear();
            calendar.set(year, month, day);
            return calendar.getTime();
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        @Override
        public String toString() {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{ year: ");
            stringBuilder.append(year);
            stringBuilder.append(", month: ");
            stringBuilder.append(month);
            stringBuilder.append(", day: ");
            stringBuilder.append(day);
            stringBuilder.append(" }");

            return stringBuilder.toString();
        }

        /**
         * 只比较年月日
         *
         * @param calendarDay
         * @return
         */
        @Override
        public int compareTo(@NonNull CalendarDay calendarDay) {
//            return getDate().compareTo(calendarDay.getDate());
            if (calendarDay == null) {
                throw new IllegalArgumentException("被比较的日期不能是null");
            }

            if (year == calendarDay.year && month == calendarDay.month && day == calendarDay.day) {
                return 0;
            }

            if (year < calendarDay.year ||
                    (year == calendarDay.year && month < calendarDay.month) ||
                    (year == calendarDay.year && month == calendarDay.month && day < calendarDay.day)) {
                return -1;
            }
            return 1;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CalendarDay) {
                CalendarDay calendarDay = (CalendarDay) o;
                if (compareTo(calendarDay) == 0) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 大于比较的日期（只比较年月日）
         *
         * @param o
         * @return
         */
        public boolean after(Object o) {
            if (o instanceof CalendarDay) {
                CalendarDay calendarDay = (CalendarDay) o;
                if (compareTo(calendarDay) == 1) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 小于比较的日期（只比较年月日）
         *
         * @param o
         * @return
         */
        public boolean before(Object o) {
            if (o instanceof CalendarDay) {
                CalendarDay calendarDay = (CalendarDay) o;
                if (compareTo(calendarDay) == -1) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class SelectedDays<K> implements Serializable {
        private static final long serialVersionUID = 3942549765282708376L;
        private K first;
        private K last;

        public SelectedDays() {
        }

        public SelectedDays(K first, K last) {
            this.first = first;
            this.last = last;
        }

        public K getFirst() {
            return first;
        }

        public void setFirst(K first) {
            this.first = first;
        }

        public K getLast() {
            return last;
        }

        public void setLast(K last) {
            this.last = last;
        }
    }
}