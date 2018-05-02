package com.tao.time.calendar;

import java.util.List;

public interface DatePickerController {
    void onDateSelected(List<SimpleMonthAdapter.CalendarDay> selectedDays);    // 选择回调函数，月份记得加1

    /**
     * 点击错误回调
     * @param error 1 点击了占用日期
     *              2 所选范围内有占用日期
     *              3 没有达到做少的选择天数
     *              4 超过的最多的选择天数
     *              5 连续选择天数不能为0
     */
    void alertSelected(int error);
}