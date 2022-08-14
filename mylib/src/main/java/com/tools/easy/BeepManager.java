package com.tools.easy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

public class BeepManager {
    private static final String TAG = BeepManager.class.getSimpleName();

    private final Context context;
    private float BEEP_VOLUME = 0.10f;
    private long VIBRATE_DURATION = 200L;

    private boolean beepEnabled = true;
    private boolean vibrateEnabled = false;

    public BeepManager(Activity activity) {
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // We do not keep a reference to the Activity itself, to prevent leaks
        this.context = activity.getApplicationContext();
    }

    public void setBeepVolume(float volume) {
        this.BEEP_VOLUME = volume;
    }

    public void setVibrateDuration(long l) {
        this.VIBRATE_DURATION = l;
    }

    public boolean isBeepEnabled() {
        return beepEnabled;
    }

    /**
     * Call updatePrefs() after setting this.
     *
     * If the device is in silent mode, it will not beep.
     *
     * @param beepEnabled true to enable beep
     */
    public void setBeepEnabled(boolean beepEnabled) {
        this.beepEnabled = beepEnabled;
    }

    public boolean isVibrateEnabled() {
        return vibrateEnabled;
    }

    /**
     * Call updatePrefs() after setting this.
     *
     * @param vibrateEnabled true to enable vibrate
     */
    public void setVibrateEnabled(boolean vibrateEnabled) {
        this.vibrateEnabled = vibrateEnabled;
    }

    @SuppressLint("MissingPermission")
    public synchronized void playBeepSoundAndVibrate() {
        if (beepEnabled) {
            playBeepSound();
        }
        if (vibrateEnabled) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            try {
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(VIBRATE_DURATION);
                }
            } catch (Exception e) {
            }
        }
    }


    public MediaPlayer playBeepSound() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.w(TAG, "Failed to beep " + what + ", " + extra);
                // possibly media player error, so release and recreate
                mp.stop();
                mp.release();
                return true;
            }
        });
        try {
            AssetFileDescriptor file = context.getResources().openRawResourceFd(R.raw.zxing_beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            } finally {
                file.close();
            }
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
            mediaPlayer.start();
            return mediaPlayer;
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            mediaPlayer.release();
            return null;
        }
    }
}
