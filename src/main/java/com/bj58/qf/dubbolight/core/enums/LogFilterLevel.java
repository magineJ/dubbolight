package com.bj58.qf.dubbolight.core.enums;

/**
 * Created by Administrator on 2017/12/4.
 */
public enum LogFilterLevel {
    ACCESS(5),
    ARGUMENTS(8),
    RETURNS(10);

    public final int level;

    private LogFilterLevel(int level) {
        this.level = level;
    }
}
