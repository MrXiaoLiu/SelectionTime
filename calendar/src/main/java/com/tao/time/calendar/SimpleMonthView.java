package com.tao.time.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.util.ArrayMap;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 每个月作为一个ItemView
 */
class SimpleMonthView extends View {

    public static final String VIEW_PARAMS_SELECTED_BEGIN_DATE = "selected_begin_date";
    public static final String VIEW_PARAMS_SELECTED_LAST_DATE = "selected_last_date";
    public static final String VIEW_PARAMS_NEAREST_DATE = "mNearestDay";
    public static final String VIEW_PARAMS_MORE_DATE = "mMoreDays";

    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";


    //内部接口
    private OnDayClickListener mOnDayClickListener;
    //用于获取今日周几
    private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();


    //在由字符串命名的时区中构造一个Time对象参数“时区”。 时间初始化为1970年1月1日。
    final Time mTime;
    private final Calendar mCalendar;
    // 用于显示星期几
    private final Calendar mDayLabelCalendar;
    //设置字体
    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;


    // 头部星期几的字体画笔
    protected Paint mWeekTextPaint;
    // 头部年月的画笔
    protected Paint mYearMonthPaint;
    //日期字体paint
    protected Paint mDayTextPaint;
    // 日期底部的文字画笔
    protected Paint mTagTextPaint;
    //被选中的日期背景画笔
    protected Paint mSelectedDayBgPaint;
    //被占用的日期画笔
    protected Paint mBusyDayBgPaint;
    //设置透明度取值范围为0~255，数值越小越透明
    private static final int SELECTED_CIRCLE_ALPHA = 128;


    protected static int DEFAULT_HEIGHT = 32;                           // 默认一行的高度
    protected static final int DEFAULT_NUM_ROWS = 6;
    protected static int DAY_SELECTED_RECT_SIZE;                        // 选中圆角矩形半径
    protected static int ROW_SEPARATOR = 12;                            // 每行中间的间距
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;                     // 日期字体的最小尺寸
    private static int TAG_TEXT_SIZE;                                   // 标签字体大小
    protected static int MONTH_HEADER_SIZE;                             // 头部的高度（包括年份月份，星期几）
    protected static int YEAR_MONTH_TEXT_SIZE;                          // 头部年份月份的字体大小
    protected static int WEEK_TEXT_SIZE;                                // 头部星期几的字体大小
    private boolean isDisplayTag;                                    // 是否显示标签
    private String mDefTag = "标签";                                     //  设置标签显示内容
    private final Boolean isPrevDayEnabled;                             // 今天以前的日期是否能被操作

    protected int mCurrentDayTextColor;                                 // 今天的字体颜色
    protected int mCurrentDayColor;                                 // 今天的字体下小圆圈颜色
    protected int mYearMonthTextColor;                                  // 头部年份和月份字体颜色
    protected int mWeekTextColor;                                       // 头部星期几字体颜色
    protected int mDayTextColor;                                        // 日期字体颜色
    protected int mTagTextColor;                                        // 底部文字字体颜色
    protected int mSelectedDayTextColor;                                // 被选中的日期字体颜色
    protected int mPreviousDayTextColor;                                // 过去的字体颜色
    protected int mSelectedDaysBgColor;                                 // 选中的日期背景颜色
    protected int mBusyDaysBgColor;                                     // 被占用的日期背景颜色
    protected int mBusyDaysTextColor;                                   // 被占用的日期字体颜色

    protected int mPadding = 0;


    protected boolean mHasToday = false;
    protected int mToday = -1;                  //得到今天具体日期
    protected int mWeekStart = 1;               // 一周的第一天（不同国家的一星期的第一天不同）
    protected int mNumDays = 7;                 // 一行几列
    protected int mNumCells;                    // 一个月有多少天
    private int mDayOfWeekStart = 0;            // 日期对应星期几
    //    protected Boolean mDrawRect;          // 圆角还是圆形
    protected int mRowHeight = DEFAULT_HEIGHT;  // 行高
    protected int mWidth;                       // simpleMonthView的宽度
    private int mNumRows;                       //每个月的日期占用的行数
    protected int mYear;//年
    protected int mMonth;//月


    private boolean isToDayOperation;                                       //今天日期能否被操作
    private List<SimpleMonthAdapter.CalendarDay> mBusyDays;                 // 被占用的日期
    private SimpleMonthAdapter.CalendarDay mNearestDay;                     // 比离开始日期大且是最近的已被占用或者无效日期
    private List<SimpleMonthAdapter.CalendarDay> mCalendarTags;             // 日期下面的标签

    SimpleMonthAdapter.CalendarDay mStartDate;          // 开始日期
    SimpleMonthAdapter.CalendarDay mEndDate;            // 结束日期
    SimpleMonthAdapter.CalendarDay cellCalendar;        // cell的对应的日期
    private List<SimpleMonthAdapter.CalendarDay> mMoreDays;              // 多选的日期

    /**
     * @param context
     * @param typedArray
     * @param dataModel
     */
    public SimpleMonthView(Context context, TypedArray typedArray, DayPickerView.DataModel dataModel) {
        super(context);

        Resources resources = context.getResources();

        //获取日历对象
        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();
        mTime = new Time(Time.getCurrentTimezone());
        mTime.setToNow();

        //获取字体
        mDayOfWeekTypeface = resources.getString(R.string.sans_serif);
        mMonthTitleTypeface = resources.getString(R.string.sans_serif);

        //今天字体下小圆圈颜色
        mCurrentDayColor = typedArray.getColor(R.styleable.DayPickerView_colorRoundDay, Color.BLUE);
        //今天字体颜色
        mCurrentDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorCurrentDay, resources.getColor(R.color.to_day));
        //头部年份月份字体颜色
        mYearMonthTextColor = typedArray.getColor(R.styleable.DayPickerView_colorYearMonthText, resources.getColor(R.color.years_text));
        //头部星期几字体颜色
        mWeekTextColor = typedArray.getColor(R.styleable.DayPickerView_colorWeekText, resources.getColor(R.color.week_day));
        //正常日期颜色
        mDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorNormalDayText, resources.getColor(R.color.normal_day));
        //底部文字的字体颜色
        mTagTextColor = typedArray.getColor(R.styleable.DayPickerView_colorTagText, resources.getColor(R.color.bottom_normal_day));
        //已过去的日期字体颜色
        mPreviousDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorPreviousDayText, resources.getColor(R.color.formerly_day));
        //被选中的日期背景颜色
        mSelectedDaysBgColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayBackground, resources.getColor(R.color.selected_day_background));
        //被选中的日期字体颜色
        mSelectedDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayText, Color.WHITE);
        //被占用的日期背景颜色
        mBusyDaysBgColor = typedArray.getColor(R.styleable.DayPickerView_colorBusyDaysBg, Color.TRANSPARENT);
        //被占用的日期字体颜色
        mBusyDaysTextColor = typedArray.getColor(R.styleable.DayPickerView_colorBusyDaysText, resources.getColor(R.color.normal_day));
//        mDrawRect = typedArray.getBoolean(R.styleable.DayPickerView_drawRoundRect, true);


        //正常日期字体大小
        MINI_DAY_NUMBER_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDay, resources.getDimensionPixelSize(R.dimen.text_size_day));
        //标签字体大小
        TAG_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeTag, resources.getDimensionPixelSize(R.dimen.text_size_tag));
        //头部年份月份字体大小
        YEAR_MONTH_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeYearMonth, resources.getDimensionPixelSize(R.dimen.text_size_month));
        //头部星期几字体大小
        WEEK_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeWeek, resources.getDimensionPixelSize(R.dimen.text_size_day_name));
        //头部高度
        MONTH_HEADER_SIZE = typedArray.getDimensionPixelOffset(R.styleable.DayPickerView_headerMonthHeight, resources.getDimensionPixelOffset(R.dimen.header_month_height));
        //日期半径
        DAY_SELECTED_RECT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_selectedDayRadius, resources.getDimensionPixelOffset(R.dimen.selected_day_radius));
        //行高
        mRowHeight = ((typedArray.getDimensionPixelSize(R.styleable.DayPickerView_calendarHeight, resources.getDimensionPixelOffset(R.dimen.calendar_height)) - MONTH_HEADER_SIZE - ROW_SEPARATOR) / 6);
        //已过去的日期是否能被操作
        isPrevDayEnabled = typedArray.getBoolean(R.styleable.DayPickerView_enablePreviousDay, false);
        //是否显示标签
        isDisplayTag = typedArray.getBoolean(R.styleable.DayPickerView_displayTag, false);
        //获取设置占用日期
        mBusyDays = dataModel.busyDays;
        //今天的日期能都被操作
        isToDayOperation = dataModel.isToDayOperation;
        //日期下面对应的标签
        mCalendarTags = dataModel.tags;
        //默认显示的标签
        mDefTag = dataModel.defTag;

        cellCalendar = new SimpleMonthAdapter.CalendarDay();

        //初始化画笔
        initView();
    }

    /**
     * 常用的字体类型名称还有：
     * Typeface.DEFAULT //常规字体类型
     * Typeface.DEFAULT_BOLD //黑体字体类型
     * Typeface.MONOSPACE //等宽字体类型
     * Typeface.SANS_SERIF //sans serif字体类型
     * 常用的字体风格名称还有：
     * Typeface.BOLD //粗体
     * Typeface.BOLD_ITALIC //粗斜体
     * Typeface.ITALIC //斜体
     * Typeface.NORMAL //常规
     * 初始化一些paint
     */
    protected void initView() {
        // 头部年份和月份的字体paint
        mYearMonthPaint = new Paint();
        //设置文本粗体
        mYearMonthPaint.setFakeBoldText(false);
        //设置抗锯齿，如果不设置，加载位图的时候可能会出现锯齿状的边界，如果设置，边界就会变的稍微有点模糊，锯齿就看不到了。
        mYearMonthPaint.setAntiAlias(true);
        //设置字体大小
        mYearMonthPaint.setTextSize(YEAR_MONTH_TEXT_SIZE);
        //设置字体样式，可以是Typeface设置的样式，也可以通过Typeface的createFromAsset(AssetManager mgr, String path)方法加载样式
        mYearMonthPaint.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
        //设置画笔颜色
        mYearMonthPaint.setColor(mYearMonthTextColor);
        mYearMonthPaint.setTextAlign(Align.CENTER);
        mYearMonthPaint.setStyle(Style.FILL);

        // 头部星期几字体paint
        mWeekTextPaint = new Paint();
        mWeekTextPaint.setAntiAlias(true);
        mWeekTextPaint.setTextSize(WEEK_TEXT_SIZE);
        mWeekTextPaint.setColor(mWeekTextColor);
        mWeekTextPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
        mWeekTextPaint.setStyle(Style.FILL);
        mWeekTextPaint.setTextAlign(Align.CENTER);
        mWeekTextPaint.setFakeBoldText(false);

        // 被选中的日期背景paint
        mSelectedDayBgPaint = new Paint();
        mSelectedDayBgPaint.setFakeBoldText(false);
        mSelectedDayBgPaint.setAntiAlias(true);
        mSelectedDayBgPaint.setColor(mSelectedDaysBgColor);
        mSelectedDayBgPaint.setTextAlign(Align.CENTER);
        mSelectedDayBgPaint.setStyle(Style.FILL);
        mSelectedDayBgPaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        // 被占用的日期paint
        mBusyDayBgPaint = new Paint();
        mBusyDayBgPaint.setFakeBoldText(false);
        mBusyDayBgPaint.setAntiAlias(true);
        mBusyDayBgPaint.setColor(mBusyDaysBgColor);
        mBusyDayBgPaint.setTextSize(TAG_TEXT_SIZE);
        mBusyDayBgPaint.setTextAlign(Align.CENTER);
        mBusyDayBgPaint.setStyle(Style.FILL);
        mBusyDayBgPaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        // 日期字体paint
        mDayTextPaint = new Paint();
        mDayTextPaint.setAntiAlias(true);
        mDayTextPaint.setColor(mDayTextColor);
        mDayTextPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mDayTextPaint.setStyle(Style.FILL);
        mDayTextPaint.setTextAlign(Align.CENTER);
        mDayTextPaint.setFakeBoldText(false);

        // 标签字体paint
        mTagTextPaint = new Paint();
        mTagTextPaint.setAntiAlias(true);
        mTagTextPaint.setColor(mTagTextColor);
        mTagTextPaint.setTextSize(TAG_TEXT_SIZE);
        mTagTextPaint.setStyle(Style.FILL);
        mTagTextPaint.setTextAlign(Align.CENTER);
        mTagTextPaint.setFakeBoldText(false);
    }

    /**
     * 设置传递进来的参数
     * 获取设置的年月日
     *
     * @param params
     */
    @SuppressLint("WrongConstant")
    public void setMonthParams(ArrayMap<String, Object> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);

        //得到开始日期
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_DATE)) {
            mStartDate = (SimpleMonthAdapter.CalendarDay) params.get(VIEW_PARAMS_SELECTED_BEGIN_DATE);
        }
        //得到结束日期
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_DATE)) {
            mEndDate = (SimpleMonthAdapter.CalendarDay) params.get(VIEW_PARAMS_SELECTED_LAST_DATE);
        }
        //比离开始日期大且是最近的已被占用或者无效日期
        if (params.containsKey(VIEW_PARAMS_NEAREST_DATE)) {
            mNearestDay = (SimpleMonthAdapter.CalendarDay) params.get(VIEW_PARAMS_NEAREST_DATE);
        }
        //多选的日期
        if (params.containsKey(VIEW_PARAMS_MORE_DATE)) {
            mMoreDays = (List<SimpleMonthAdapter.CalendarDay>) params.get(VIEW_PARAMS_MORE_DATE);
        }
        //获取年月
        mMonth = (int) params.get(VIEW_PARAMS_MONTH);
        mYear = (int) params.get(VIEW_PARAMS_YEAR);

        mHasToday = false;
        mToday = -1;

        //设置初始数据
        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);
        //得到一周的第一天
        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = (int) params.get(VIEW_PARAMS_WEEK_START);
        } else {

            mWeekStart = mCalendar.getFirstDayOfWeek();

        }
        //得到一个月有多少天
        mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);

        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, mTime)) {
                mHasToday = true;
                mToday = day;
            }
        }

        mNumRows = calculateNumRows();
    }

    /**
     * 判断日期是否是今日
     *
     * @param monthDay 需要判断的日期
     * @param time     系统具体日期
     * @return
     */
    private boolean sameDay(int monthDay, Time time) {
        return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
    }

    /**
     * 计算每个月的日期占用的行数
     *
     * @return
     */
    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    /**
     * 设置控件宽高
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 设置simpleMonthView的宽度和高度  行高*每个月行数+头部高度+每行中间的间距
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + MONTH_HEADER_SIZE + ROW_SEPARATOR);
    }

    /**
     * 获取控件宽度
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            drawMonthTitle(canvas);
            drawMonthDayLabels(canvas);
            drawMonthCell(canvas);
        } catch (Exception e) {

        }

    }

    /**
     * 绘制头部年份月份
     *
     * @param canvas
     */
    private void drawMonthTitle(Canvas canvas) {

        //得到X轴起点位置
        int x = mWidth / 2;
        int y = (MONTH_HEADER_SIZE - WEEK_TEXT_SIZE) / 2 + (YEAR_MONTH_TEXT_SIZE / 3);
        StringBuilder stringBuilder = new StringBuilder(getMonthAndYearString().toLowerCase());
        stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
        canvas.drawText(stringBuilder.toString(), x, y, mYearMonthPaint);
    }

    /**
     * 绘制头部的一行星期几
     *
     * @param canvas
     */
    private void drawMonthDayLabels(Canvas canvas) {
        //头部的高度总高度         头部星期几字体大小
        int y = MONTH_HEADER_SIZE - (WEEK_TEXT_SIZE / 2);
        int dayWidthHalf = mWidth / (mNumDays * 2);
        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            int x = (2 * i + 1) * dayWidthHalf;
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            canvas.drawText(mDateFormatSymbols.getShortWeekdays()[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()),
                    x, y, mWeekTextPaint);
        }
    }

    /**
     * 绘制所有的cell
     *
     * @param canvas
     */
    protected void drawMonthCell(Canvas canvas) {
        //头部的高度总高度  每行中间间距   行高
        int y = MONTH_HEADER_SIZE + ROW_SEPARATOR + mRowHeight / 2;
        //每行分为14份
        int paddingDay = mWidth / (2 * mNumDays);
        //获取每月的第一天是周几
        int dayOffset = findDayOffset();
        int day = 1;
        //循环每一天的绘制
        while (day <= mNumCells) {
            int x = paddingDay * (1 + dayOffset * 2);


            mDayTextPaint.setColor(mDayTextColor);
            mDayTextPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            mTagTextPaint.setColor(mTagTextColor);

            cellCalendar.setDay(mYear, mMonth, day);

            // 绘制当天
            boolean isToady = false;
            if (mHasToday && (mToday == day) && isToDayOperation) {
                isToady = true;
                mDayTextPaint.setColor(mCurrentDayTextColor);
                //如果显示标签靠上绘制
                if (isDisplayTag) {
                    canvas.drawText(String.format("%d", day), x, getTextYCenter(mDayTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                } else {
                    canvas.drawText(String.format("%d", day), x, getTextYCenter(mDayTextPaint, y), mDayTextPaint);
                }
                mDayTextPaint.setColor(mCurrentDayColor);
                canvas.drawCircle(x, getTextYCenter(mDayTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), 6, mDayTextPaint);
            }
            //绘制当天下面的小圆圈
            if (mHasToday && (mToday == day)) {
                mDayTextPaint.setColor(mCurrentDayColor);
                canvas.drawCircle(x, getTextYCenter(mDayTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), 6, mDayTextPaint);
            }
            // 绘制已过去的日期
            boolean isPrevDay = false;
            if (!isPrevDayEnabled && prevDay(day, mTime)) {
                isPrevDay = true;
                mDayTextPaint.setColor(mPreviousDayTextColor);
                //如果显示标签靠上绘制
                if (isDisplayTag) {
                    canvas.drawText(String.format("%d", day), x, getTextYCenter(mDayTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                } else {
                    canvas.drawText(String.format("%d", day), x, getTextYCenter(mDayTextPaint, y), mDayTextPaint);
                }


            }

            boolean isBeginDay = false;
            // 绘制起始日期的方格
            if (mStartDate != null && cellCalendar.equals(mStartDate) && !mStartDate.equals(mEndDate)) {
                isBeginDay = true;
                drawDayBg(canvas, x, y, mSelectedDayBgPaint);
                mDayTextPaint.setColor(mSelectedDayTextColor);
                if (isDisplayTag) {

                    mDayTextPaint.setTextSize(TAG_TEXT_SIZE);
                    canvas.drawText("开始", x, getTextYCenter(mDayTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                }
                mDayTextPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);

                //如果是今天并且显示标签就靠上绘制
                if (isToady) {
                    if (isDisplayTag) {
                        canvas.drawText(String.format("%d", day), x, getTextYCenter(mDayTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                    } else {
                        canvas.drawText(String.format("%d", day), x, getTextYCenter(mDayTextPaint, y), mDayTextPaint);
                    }
                }
            }

            boolean isLastDay = false;
            // 绘制结束日期的方格
            if (mEndDate != null && cellCalendar.equals(mEndDate) && !mStartDate.equals(mEndDate)) {
                isLastDay = true;
                drawDayBg(canvas, x, y, mSelectedDayBgPaint);
                mDayTextPaint.setColor(mSelectedDayTextColor);
                if (isDisplayTag) {
                    mDayTextPaint.setTextSize(TAG_TEXT_SIZE);
                    canvas.drawText("结束", x, getTextYCenter(mDayTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                }
                mDayTextPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
            }


            // 在开始和结束之间的日期
            if (cellCalendar.after(mStartDate) && cellCalendar.before(mEndDate)) {
                mDayTextPaint.setColor(mSelectedDayTextColor);
                drawDayBg(canvas, x, y, mSelectedDayBgPaint);
                // 标签变为白色
                mTagTextPaint.setColor(mSelectedDayTextColor);
            }

            // 被占用的日期
            boolean isBusyDay = false;
            for (SimpleMonthAdapter.CalendarDay calendarDay : mBusyDays) {
                if (cellCalendar.equals(calendarDay) && !isPrevDay) {
                    isBusyDay = true;

                    // 选择了开始和结束日期，结束日期等于mNearestDay的情况
                    if (mStartDate != null && mEndDate != null && mNearestDay != null &&
                            mEndDate.equals(mNearestDay) && mEndDate.equals(calendarDay)) {

                    } else {
                        // 选择了开始日期，没有选择结束日期，mNearestDay变为可选且不变灰色
                        if (mStartDate != null && mEndDate == null && mNearestDay != null && cellCalendar.equals(mNearestDay)) {
                            mDayTextPaint.setColor(mDayTextColor);
                        } else {

                            mBusyDayBgPaint.setColor(mBusyDaysBgColor);
                            drawDayBg(canvas, x, y, mBusyDayBgPaint);
                            mDayTextPaint.setColor(mBusyDaysTextColor);

                        }

                        if (isDisplayTag) {
                            mDayTextPaint.setTextSize(TAG_TEXT_SIZE);
                            canvas.drawText("占用", x, getTextYCenter(mBusyDayBgPaint, y + DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                        }
                    }
                    mDayTextPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                    mDayTextPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
                    //如果显示标签靠上绘制
                    if (isDisplayTag) {
                        canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                    } else {
                        canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y), mDayTextPaint);
                    }
                    mDayTextPaint.setFlags(0);

                }
            }

            // 绘制多选的日期
            boolean isMultiDay = false;
            if (mMoreDays != null) {
                for (SimpleMonthAdapter.CalendarDay calendarDay : mMoreDays) {
                    if (cellCalendar.equals(calendarDay) && !isPrevDay) {
                        isMultiDay = true;
                        mDayTextPaint.setColor(mSelectedDayTextColor);
                        mTagTextPaint.setColor(mSelectedDayTextColor);
                        drawDayBg(canvas, x, y, mSelectedDayBgPaint);
                        //如果显示标签靠上绘制
                        if (isDisplayTag) {
                            canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                        } else {
                            canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y), mDayTextPaint);
                        }

                    }
                }
            }

            // 绘制标签&& !isInvalidDays
            if (isDisplayTag && !isPrevDay && !isBusyDay && !isBeginDay && !isLastDay) {
                boolean isCalendarTag = false;
                for (SimpleMonthAdapter.CalendarDay calendarDay : mCalendarTags) {
                    if (cellCalendar.equals(calendarDay)) {
                        isCalendarTag = true;
                        canvas.drawText(calendarDay.tag, x, getTextYCenter(mTagTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mTagTextPaint);
                    }
                }
                if (!isCalendarTag) {
                    canvas.drawText(mDefTag, x, getTextYCenter(mTagTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mTagTextPaint);
                }
            }

            // 绘制日期&& !isInvalidDays
            if (!isToady && !isPrevDay && !isBusyDay) {
                //如果显示标签日期靠上绘制
                if (isDisplayTag) {
                    //靠上绘制，以方便添加  底部标签
                    canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                } else {
                    //居中绘制
                    canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y), mDayTextPaint);
                }

            }

            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += mRowHeight;
            }
            day++;
        }
    }


    public boolean onTouchEvent(MotionEvent event) {

        try {
            //点击屏幕松开手指时触发
            if (event.getAction() == MotionEvent.ACTION_UP) {
                SimpleMonthAdapter.CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());

                if (calendarDay == null)
                    return true;

                //遍历占用日期
                for (SimpleMonthAdapter.CalendarDay day : mBusyDays) {
                    // 选择了开始日期，这时候比开始日期大且离开始日期最近的不可用日期变为可选
                    //如果点击的日期是占用日期   !(结束日期为空  并且点击日期不是占用)
//                if (calendarDay.equals(day) && !(mEndDate == null && mNearestDay != null && calendarDay.equals(mNearestDay))) {
//                    return true;
//                }
                }
                onDayClick(calendarDay);
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private void onDayClick(SimpleMonthAdapter.CalendarDay calendarDay) {
        //监听不为空
        if (mOnDayClickListener != null && (isPrevDayEnabled || !prevDay(calendarDay.day, mTime))) {
            mOnDayClickListener.onDayClick(this, calendarDay);
        }
    }

    /**
     * 根据坐标获取对应的日期
     *
     * @param x
     * @param evenY
     * @return
     */
    public SimpleMonthAdapter.CalendarDay getDayFromLocation(float x, float evenY) {
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }


        //Y-头部高度/行高

        int yDay = ((evenY - MONTH_HEADER_SIZE) / mRowHeight) > 0 ? (int) (evenY - MONTH_HEADER_SIZE) / mRowHeight : -1;


        //(X*7列)/总宽度-每个月第一天是星期几+yDay*总列数
        int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

        if (mMonth > 11 || mMonth < 0 || CalendarUtils.getDaysInMonth(mMonth, mYear) < day || day < 1)
            return null;

        SimpleMonthAdapter.CalendarDay calendar = new SimpleMonthAdapter.CalendarDay(mYear, mMonth, day);

        // 获取日期下面的tag
        boolean flag = false;
        for (SimpleMonthAdapter.CalendarDay calendarTag : mCalendarTags) {
            if (calendarTag.compareTo(calendar) == 0) {
                flag = true;
                calendar = calendarTag;
            }
        }
        if (!flag) {
            calendar.tag = mDefTag;
        }
        return calendar;
    }


//    public void reuse() {
//        mNumRows = DEFAULT_NUM_ROWS;
//        requestLayout();
//    }


    /**
     * 每个月第一天是星期几
     *
     * @return
     */
    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }

    /**
     * 判断是否是已经过去的日期
     *
     * @param monthDay
     * @param time
     * @return true是已过去的
     */
    private boolean prevDay(int monthDay, Time time) {
        return ((mYear < time.year)) || (mYear == time.year && mMonth < time.month) ||
                (mYear == time.year && mMonth == time.month && monthDay < time.monthDay);
    }

    /**
     * 获取年份和月份
     *
     * @return
     */
    private String getMonthAndYearString() {

        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        //获取当前时间戳（毫秒）
        long millis = mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);
    }

    /**
     * 绘制cell
     * 画圆角矩形
     *
     * @param canvas
     * @param x
     * @param y
     */
    private void drawDayBg(Canvas canvas, int x, int y, Paint paint) {
        RectF rectF = new RectF(x - DAY_SELECTED_RECT_SIZE, y - DAY_SELECTED_RECT_SIZE,
                x + DAY_SELECTED_RECT_SIZE, y + DAY_SELECTED_RECT_SIZE);
        if (isDisplayTag) {
            //绘制圆角矩形
            canvas.drawRoundRect(rectF, 10.0f, 10.0f, paint);
        } else {
            //绘制实心圆形
            canvas.drawCircle(x, y, DAY_SELECTED_RECT_SIZE, paint);
        }


    }

    /**
     * 在使用drawText方法时文字不能根据y坐标居中，所以重新计算y坐标
     *
     * @param paint
     * @param y
     * @return
     */
    private float getTextYCenter(Paint paint, int y) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offY = fontTotalHeight / 2 - fontMetrics.bottom;
        return y + offY;
    }

    //接口暴露外部
    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public interface OnDayClickListener {
        void onDayClick(SimpleMonthView simpleMonthView, SimpleMonthAdapter.CalendarDay calendarDay);
    }
}