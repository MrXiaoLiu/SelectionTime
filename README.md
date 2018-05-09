
# SelectionTime（1.0.1）
`SelectionTime `是用于Android设备上选择日期开源库，高度订制，打造适合自己的日期控件
## 效果图（多选、范围选、单选）

![multi logo](https://github.com/MrXiaoLiu/SelectionTime/blob/master/images/multi.gif)
![MacDown logo](https://github.com/MrXiaoLiu/SelectionTime/blob/master/images/range.gif)
![MacDown logo](https://github.com/MrXiaoLiu/SelectionTime/blob/master/images/range.gif)
## 安装说明
**Gradle：**

```
compile 'com.lyt:calendarutils:1.0.1'
```
**Maven：**

```
<dependency>
  <groupId>com.lyt</groupId>
  <artifactId>calendarutils</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```
## 使用说明
**在XML中声明**

```
  <com.tao.time.calendar.DayPickerView
        android:id="@+id/daypickeview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:colorBusyDaysBg="@android:color/transparent"
        app:displayTag="false"
        app:enablePreviousDay="false"
        app:colorBusyDaysText="@color/years_text"
        />
```
**设置数据**

```
DayPickerView.DataModel dataModel = new DayPickerView.DataModel();   
  

//监听类型(TYPE_MULTI：多选，TYPE_RANGE：范围选，TYPE_DAY_NUMBER：单选，TYPE_CONTINUOUS 连续选择)注意：连续选择状态下，选择天数不能为0
dataModel.mTimeType = DayPickerView.DataModel.TYPE.TYPE_RANGE;
//开始年份
dataModel.yearStart = calendar.get(Calendar.YEAR);
//开始的月份
dataModel.monthStart = calendar.get(Calendar.MONTH);
//要显示几个月
dataModel.monthCount = 24;
//数据和监听(记得实现DatePickerController)
daypickeview.setParameter(dataModel,this);
```
**监听**

```
/**
 * 返回选择日期的集合
 * @param selectedDays 返回月份加1
 */
 @Override
 public void onDateSelected(List<SimpleMonthAdapter.CalendarDay> selectedDays) {
    }

 /**
  * 错误回调，方便自定义提示语
  * @param error 1 点击了占用日期
  *              2 所选范围内有占用日期
  *              3 没有达到做少的选择天数
  *              4 超过的最多的选择天数
  */
  @Override
 public void alertSelected(int error) {}
```
**属性**

| 属性名称 | 说明  | 类型 |
|:------------- |:---------------:| -------------:|
| colorYearMonthText      | 头部年份月份字体颜色 |  color|
| colorWeekText      | 头部星期几字体颜色        |  color|
| colorCurrentDay | 今天字体颜色        | color|
|colorRoundDay|今天字体下小圆圈颜色|color|
|colorSelectedDayBackground|被选中的日期背景颜色| color |
|colorSelectedDayText|被选中的日期字体颜色| color |
|colorPreviousDayText|已过去的日期字体颜色| color |
|colorNormalDayText|正常日期颜色| color |
|colorBusyDaysBg|被占用的日期背景颜色| color |
|colorBusyDaysText|被占用的日期字体颜色| color |
|colorTagText|底部文字字体颜色| color |
|textSizeYearMonth|头部年份月份字体大小|dimension|
|textSizeWeek|头部星期几字体大小|dimension|
|textSizeDay|正常日期字体大小|dimension|
|textSizeTag|标签字体大小|dimension|
|headerMonthHeight|头部高度|dimension|
|selectedDayRadius|日期半径|dimension|
|calendarHeight|行高|dimension|
|enablePreviousDay|已过去的日期是否能被操作|boolean|
|displayTag|是否显示标签|boolean|

## 更多方法
**最小选择天数**

```
dataModel.leastDaysNum = 2;
```
**最大选择天数**

```
dataModel.mostDaysNum = 4;
```
**今天日期能否被操作**

```
dataModel.isToDayOperation = false;
```
**根据天数日期选择（连续选择状态下此为必填项）**

```
dataModel.numberOfDays = 2;
```
**添加被占用日期**

```
List<SimpleMonthAdapter.CalendarDay> list = new ArrayList();
list.add(new SimpleMonthAdapter.CalendarDay(2018, 4, 23));
dataModel.busyDays = list;
```
**默认选择日期**

```
SimpleMonthAdapter.CalendarDay startDay = new SimpleMonthAdapter.CalendarDay(2019, 9, 5);
SimpleMonthAdapter.CalendarDay endDay = new SimpleMonthAdapter.CalendarDay(2019, 9, 20);
SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays = new SimpleMonthAdapter.SelectedDays<>(startDay, endDay);
dataModel.selectedDays = selectedDays;

```
## 联系
项目需要完善的地方还有很多，如有BUG或者更好的建议欢迎issues
项目会持续更新，不断完善，欢迎start
## 鸣谢
[CalendarView](https://github.com/henry-newbie/CalendarView)
