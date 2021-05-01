package com.projects.mara.msm_caller;

/**
 * Created by Yashasvi on 05-06-2017.
 */

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Collections;
import java.util.List;


public class USSDService extends AccessibilityService {

    public static String TAG = USSDService.class.getSimpleName();
    public static String MY_ACTION = "MY_ACTION";
    String text;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent");

        AccessibilityNodeInfo source = event.getSource();
    /* if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName().equals("android.app.AlertDialog")) { // android.app.AlertDialog is the standard but not for all phones  */
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !String.valueOf(event.getClassName()).contains("AlertDialog")) {
            return;
        }
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && (source == null || !source.getClassName().equals("android.widget.TextView"))) {
            return;
        }
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && TextUtils.isEmpty(source.getText())) {
            return;
        }

        List<CharSequence> eventText;

        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            eventText = event.getText();
        } else {
            eventText = Collections.singletonList(source.getText());
        }

        text = processUSSDText(eventText);

        if( TextUtils.isEmpty(text) ) return;
        // Close dialog
        performGlobalAction(GLOBAL_ACTION_BACK);
        text = text.substring(7,13);
        Log.i(TAG, text);
        //Handle USSD response here
        Float bal = 2f;

        try{
            bal = Float.parseFloat(text);
            Intent intent = new Intent();
            intent.setAction(MY_ACTION);
            intent.putExtra("bal",bal);
            sendBroadcast(intent);
        }catch (NumberFormatException e){
            e.printStackTrace();
        }

    }

    private String processUSSDText(List<CharSequence> eventText) {
        for (CharSequence s : eventText) {
            String text = String.valueOf(s);
            // Return text if text is the expected ussd response
            if( true ) {
                return text;
            }
        }
        return null;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = new String[]{"com.android.phone"};
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }
}
