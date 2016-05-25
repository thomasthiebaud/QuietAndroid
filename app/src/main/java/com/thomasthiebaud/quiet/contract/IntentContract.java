package com.thomasthiebaud.quiet.contract;

/**
 * Created by thomasthiebaud on 5/14/16.
 */
public interface IntentContract {
    String PHONE_NUMBER = "phoneNumber";
    String SCORE = "score";
    String AD = "ad";
    String SCAM = "scam";

    String TITLE = "title";
    String DESCRIPTION = "description";
    String ICON = "icon";
    String STATUS = "status";
    String OK = "ok";
    String WIDGET_ACTION = "com.thomasthiebaud.quiet.UPDATE_WIDGET_STATE";
    String AD_ACTION = "com.thomasthiebaud.quiet.AD";
    String OK_ACTION = "com.thomasthiebaud.quiet.OK";
    String SCAM_ACTION = "com.thomasthiebaud.quiet.SCAM";
    String PHONE_STATE = "android.intent.action.PHONE_STATE";
}