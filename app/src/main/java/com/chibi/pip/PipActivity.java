package com.chibi.pip;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class PipActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = PipActivity.class.getName();

    private ConstraintLayout clBg;
    private TextView tvColorCode;
    private ImageView ivPause;

    private Handler mHandler;
    private static int incValues = 0;
    private String[] colorNames;
    private boolean isPictureInPictureMode;
    private boolean isPause = true;

    /**
     * The arguments to be used for Picture-in-Picture mode.
     */
    private final PictureInPictureParams.Builder mPictureInPictureParamsBuilder = new PictureInPictureParams.Builder();

    /**
     * A {@link BroadcastReceiver} to receive action item events from Picture-in-Picture mode.
     */
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip);

        clBg = findViewById(R.id.cl_bg);
        tvColorCode = findViewById(R.id.tv_color_code);
        ivPause = findViewById(R.id.iv_pause);

        ivPause.setOnClickListener(this);

        colorNames = getResources().getStringArray(R.array.colors);
        mHandler = new Handler();

        startRepeatingTask();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!isPictureInPictureMode) {
            enterInToPictureInPictureMode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            isPictureInPictureMode = true;
            tvColorCode.setVisibility(View.GONE);
            ivPause.setVisibility(View.GONE);

            // Starts receiving events from action items in PiP mode.
            mReceiver =
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (intent == null
                                    || !Constant.ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                                return;
                            }

                            // This is where we are called back from Picture-in-Picture action
                            // items.
                            final int controlType = intent.getIntExtra(Constant.EXTRA_CONTROL_TYPE, 0);
                            switch (controlType) {
                                case Constant.REQUEST_CODE_PLAY:
                                    startRepeatingTask();
                                    break;
                                case Constant.REQUEST_CODE_PAUSE:
                                    stopRepeatingTask();
                                    break;
                            }
                        }
                    };
            registerReceiver(mReceiver, new IntentFilter(Constant.ACTION_MEDIA_CONTROL));

        } else {
            isPictureInPictureMode = false;
            tvColorCode.setVisibility(View.VISIBLE);
            ivPause.setVisibility(View.VISIBLE);

            // We are out of PiP mode. We can stop receiving events from it.
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isPause) {
            if (!isPictureInPictureMode) {
                enterInToPictureInPictureMode();
            }
        }
        else super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        startRepeatingTask();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustFullScreen(newConfig);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_pause:
                pauseOrResumeRepeatingTask(isPause);
                break;
        }
    }


    /**
     * Adjusts immersive full-screen flags depending on the screen orientation.
     *
     * @param config The current {@link Configuration}.
     */
    private void adjustFullScreen(Configuration config) {
        final View decorView = getWindow().getDecorView();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    /*
     * start picture in picture mode by passing the ascept ratio using the param builder and call enterPictureInPictureMode
     * */
    private void enterInToPictureInPictureMode() {

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            // Calculate the aspect ratio of the PiP screen.
            Rational aspectRatio = new Rational(clBg.getWidth(), clBg.getHeight());

            mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();

            enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }
    }

    private void startRepeatingTask() {
        mStatusChecker.run();
        updatePictureInPictureActions("pause", Constant.REQUEST_CODE_PAUSE, Constant.REQUEST_CODE_PAUSE);
        ivPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
        updatePictureInPictureActions("play", Constant.REQUEST_CODE_PLAY, Constant.REQUEST_CODE_PLAY);
        ivPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
    }

    private void pauseOrResumeRepeatingTask(boolean isPause) {

        if (isPause) {
            this.isPause = false;
            stopRepeatingTask();
        } else {
            this.isPause = true;
            startRepeatingTask();
        }
    }

    /**
     * Update the state of pause/resume action item in Picture-in-Picture mode.
     *
     * @param title       The title text.
     * @param requestCode The request code for the {@link PendingIntent}.
     */
    void updatePictureInPictureActions(String title, int controlType, int requestCode) {
        final ArrayList<RemoteAction> actions = new ArrayList<>();

        // This is the PendingIntent that is invoked when a user clicks on the action item.
        // You need to use distinct request codes for play and pause, or the PendingIntent won't
        // be properly updated.
        final PendingIntent intent =
                PendingIntent.getBroadcast(PipActivity.this, requestCode,
                        new Intent(Constant.ACTION_MEDIA_CONTROL).putExtra(Constant.EXTRA_CONTROL_TYPE, controlType), 0);

        final Icon icon = Icon.createWithResource(PipActivity.this, controlType == 0 ? R.drawable.pause : R.drawable.play);

        actions.add(new RemoteAction(icon, title, title, intent));


        mPictureInPictureParamsBuilder.setActions(actions);

        // This is how you can update action items (or aspect ratio) for Picture-in-Picture mode.
        // Note this call can happen even when the app is not in PiP mode. In that case, the
        // arguments will be used for at the next call of #enterPictureInPictureMode.
        setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
    }


    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (incValues >= colorNames.length) incValues = 0;

                final String value = colorNames[incValues++];

                Log.v(TAG, " Colors " + value);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clBg.setBackgroundColor(Color.parseColor(value));
                        tvColorCode.setText(value);
                    }
                });

            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, 2000);
            }
        }
    };

}
