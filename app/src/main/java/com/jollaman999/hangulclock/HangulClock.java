package com.jollaman999.hangulclock;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class HangulClock extends AppCompatActivity {

    private TextView text_clock_timer;
    private CheckBox chk_screen_keep_on;
    private CheckBox chk_24hour;
    private Button btn_current_time;
    private Button btn_clock_setting;

    private EditText edit_timer_hour;
    private EditText edit_timer_minute;
    private EditText edit_timer_second;
    private Button btn_start_pause;

    private boolean is_screen_keep_on = false;
    private boolean is_24hour = false;

    private int mHour;
    private int mMinute;
    private int mSecond;
    private Calendar calendar;

    private final static int TIME_DIALOG_ID = 0;

    ClockRefresher mClockRefresher;
    TimerRefresher mTimerRefresher;
    Thread ClockThread;
    Thread TimerThread;

    static boolean is_clock;
    static boolean is_time_changed;
    static boolean is_timer_paused;
    static boolean is_timer_cleared;

    MediaPlayer mMediaPlayer;

    public void Init_Clock() {
        is_clock = true;
        is_time_changed = false;

        text_clock_timer = (TextView) findViewById(R.id.text_clock);
        chk_screen_keep_on = (CheckBox) findViewById(R.id.chk_screen_keep_on);
        chk_24hour = (CheckBox) findViewById(R.id.chk_24hour);
        btn_current_time = (Button) findViewById(R.id.btn_current_time);
        btn_clock_setting = (Button) findViewById (R.id.btn_clock_setting);

        chk_screen_keep_on.setChecked(is_screen_keep_on);
        if (is_screen_keep_on) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        chk_screen_keep_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_screen_keep_on) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    is_screen_keep_on = false;
                } else {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    is_screen_keep_on = true;
                }

                chk_screen_keep_on.setChecked(is_screen_keep_on);
            }
        });

        chk_24hour.setChecked(is_24hour);
        chk_24hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_24hour) {
                    is_24hour = false;
                } else {
                    is_24hour = true;
                }

                chk_24hour.setChecked(is_24hour);
                UpdateClockTimer();
            }
        });

        btn_current_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_time_changed = false;
                TimeSync();
            }
        });
        btn_clock_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });

        TimeSync();

        mClockRefresher = new ClockRefresher();
        ClockThread = new Thread(mClockRefresher);
        ClockThread.start();
    }

    public void Init_Timer() {
        is_clock = false;
        is_timer_paused = true;
        is_timer_cleared = true;

        edit_timer_hour = (EditText) findViewById(R.id.edit_timer_hour);
        edit_timer_minute = (EditText) findViewById(R.id.edit_timer_minute);
        edit_timer_second = (EditText) findViewById(R.id.edit_timer_second);

        text_clock_timer = (TextView) findViewById(R.id.text_timer);
        chk_screen_keep_on = (CheckBox) findViewById(R.id.chk_screen_keep_on);
        btn_start_pause = (Button) findViewById(R.id.btn_start_pause);

        btn_start_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s;

                CancelThreads();

                if (!is_timer_cleared) {
                    if (is_timer_paused) {
                        btn_start_pause.setText(R.string.pause);
                        is_timer_paused = false;

                        mTimerRefresher = new TimerRefresher();
                        TimerThread = new Thread(mTimerRefresher);
                        TimerThread.start();
                    } else {
                        btn_start_pause.setText(R.string.start);
                        is_timer_paused = true;
                    }

                    return;
                }

                btn_start_pause.setText(R.string.pause);
                is_timer_paused = false;
                is_timer_cleared = false;

                s = edit_timer_hour.getText().toString();
                if (s == null || s.equals("")) {
                    mHour = 0;
                } else if (Integer.parseInt(s) > 0 && Integer.parseInt(s) < 12) {
                    mHour = Integer.parseInt(s);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "시간의 범위가 초과하였습니다!",
                            Toast.LENGTH_SHORT).show();
                }

                s = edit_timer_minute.getText().toString();
                if (s == null || s.equals("")) {
                    mMinute = 0;
                } else if (Integer.parseInt(s) > 0 && Integer.parseInt(s) < 60) {
                    mMinute = Integer.parseInt(s);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "분의 범위가 초과하였습니다!",
                            Toast.LENGTH_SHORT).show();
                }

                s = edit_timer_second.getText().toString();
                if (s == null || s.equals("")) {
                    mSecond = 0;
                } else if (Integer.parseInt(s) > 0 && Integer.parseInt(s) < 60) {
                    mSecond = Integer.parseInt(s);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "초의 범위가 초과하였습니다!",
                            Toast.LENGTH_SHORT).show();
                }

                UpdateClockTimer();

                mTimerRefresher = new TimerRefresher();
                TimerThread = new Thread(mTimerRefresher);
                TimerThread.start();
            }
        });

        chk_screen_keep_on.setChecked(is_screen_keep_on);
        if (is_screen_keep_on) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        chk_screen_keep_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_screen_keep_on) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    is_screen_keep_on = false;
                } else {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    is_screen_keep_on = true;
                }

                chk_screen_keep_on.setChecked(is_screen_keep_on);
            }
        });

        mHour = 0;
        mMinute = 0;
        mSecond = 0;

        UpdateClockTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        TimeSync();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        UpdateClockTimer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.clock);
        is_screen_keep_on = false;
        Init_Clock();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hangul_clock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        CancelThreads();

        switch (id) {
            case R.id.action_clock:
                setContentView(R.layout.clock);
                Init_Clock();
                return true;
            case R.id.action_timer:
                setContentView(R.layout.timer);
                Init_Timer();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinute = minute;
                    UpdateClockTimer();
                    is_time_changed = true;
                }
            };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, mTimeSetListener,
                        mHour, mMinute, false);
        }
        return null;
    }

    public void TimeSync() {
        if (!is_clock || is_time_changed) {
            return;
        }

        calendar = Calendar.getInstance();

        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        mSecond = calendar.get(Calendar.SECOND);

        UpdateClockTimer();
    }

    private String Num2Hangul_1 (int value) {
        String s = new String("");

        switch (value) {
            case 0:
                s += "영";
                break;
            case 1:
                s += "한";
                break;
            case 2:
                s += "두";
                break;
            case 3:
                s += "세";
                break;
            case 4:
                s += "네";
                break;
            case 5:
                s += "다섯";
                break;
            case 6:
                s += "여섯";
                break;
            case 7:
                s += "일곱";
                break;
            case 8:
                s += "여덟";
                break;
            case 9:
                s += "아홉";
                break;
            case 10:
                s += "열";
                break;
            case 11:
                s += "열한";
                break;
            case 12:
                s += "열두";
                break;
            default:
                break;
        }

        return s;
    }

    private String Num2Hangul_2 (int value) {
        String s = new String("");

        switch (value) {
            case 0:
                s += "공";
                break;
            case 1:
                s += "일";
                break;
            case 2:
                s += "이";
                break;
            case 3:
                s += "삼";
                break;
            case 4:
                s += "사";
                break;
            case 5:
                s += "오";
                break;
            case 6:
                s += "육";
                break;
            case 7:
                s += "칠";
                break;
            case 8:
                s += "팔";
                break;
            case 9:
                s += "구";
                break;
            default:
                break;
        }

        return s;
    }

    private String Hour2Hangul(int hour) {
        String s1 = new String("");
        String s2 = new String("");
        String sip = new String("십");
        int num1, num2;

        num1 = hour;

        if (!is_clock) {
            return Num2Hangul_1(hour) + "시간 ";
        }

        if (is_24hour) {
            if (num1 >= 10) {
                num1 = hour / 10;
                num2 = hour % 10;

                if (num1 == 1 && num2 == 0) {
                    s1 = sip;
                } else {
                    if (num1 != 1) {
                        s1 = Num2Hangul_2(num1) + sip;
                    } else {
                        s1 = sip;
                    }
                    if (num2 != 0) {
                        s2 += Num2Hangul_2(num2);
                    }
                }
            } else {
                s1 += "공" + Num2Hangul_2(num1);
            }
        } else {
            if (hour == 0) {
                return "오전\n열두시 ";
            } else if (hour == 12) {
                return "오후\n열두시 ";
            } else if (hour > 12) {
                return "오후\n"+ Num2Hangul_1(hour - 12)
                        + "시 ";
            } else {
                return "오전\n" + Num2Hangul_1(hour)
                        + "시 ";
            }
        }

        return s1 + s2 + "시 ";
    }

    private String MinSec2Hangul(boolean is_min, int value) {
        String s1 = new String("");
        String s2 = new String("");
        String sip = new String("십");
        int num1, num2;

        num1 = value;

        if (num1 == 0) {
            if (is_min) {
                return "영분 ";
            } else {
                return "영초";
            }
        }

        if (num1 >= 10) {
            num1 = value / 10;
            num2 = value % 10;

            if (num1 == 1 && num2 == 0) {
                s1 = sip;
            } else {
                if (num1 != 1) {
                    s1 = Num2Hangul_2(num1) + sip;
                } else {
                    s1 = sip;
                }
                if (num2 != 0) {
                    s2 += Num2Hangul_2(num2);
                }
            }
        } else {
            s1 += Num2Hangul_2(num1);
        }

        if (is_min) {
            return s1 + s2 + "분 ";
        } else {
            return s1 + s2 + "초";
        }
    }

    public void UpdateClockTimer() {
        String sResult;

        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            sResult = Hour2Hangul(mHour)
                    + MinSec2Hangul(true, mMinute)
                    + MinSec2Hangul(false, mSecond);
        } else {
            sResult = Hour2Hangul(mHour) + "\n"
                    + MinSec2Hangul(true, mMinute) + "\n"
                    + MinSec2Hangul(false, mSecond);
        }

        text_clock_timer.setText(sResult);
    }

    private void PlayAlarm() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            return;
        }

        mMediaPlayer = MediaPlayer.create(this, R.raw.alarm);

        /* Volume calculations */
        AudioManager mgr = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = mgr
                .getStreamVolume(AudioManager.STREAM_ALARM);
        float streamVolumeMax = mgr
                .getStreamMaxVolume(AudioManager.STREAM_ALARM);
        float volume = streamVolumeCurrent / streamVolumeMax;

        mMediaPlayer.setVolume(volume, volume);
        mMediaPlayer.start();
    }

    public void CancelThreads() {
        if (ClockThread != null && ClockThread.isAlive()) {
            ClockThread.interrupt();
            mClockRefresher.cancel();
            mClockRefresher = null;
            ClockThread = null;
        }
        if (TimerThread != null && TimerThread.isAlive()) {
            TimerThread.interrupt();
            mTimerRefresher.cancel();
            mTimerRefresher = null;
            TimerThread = null;
        }
    }

    private final Handler UpdateClockTimer_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UpdateClockTimer();
        }
    };

    private class ClockRefresher extends Thread {

        private boolean is_thread_canceled = false;

        public void cancel() {
            is_thread_canceled = true;
        }

        @Override
        public void run() {
            while (!is_thread_canceled) {
                mSecond++;

                if (mSecond == 60) {
                    mSecond = 0;
                    mMinute++;
                }

                if (mMinute == 60) {
                    mMinute = 0;
                    mHour++;
                }

                if (mHour > 24) {
                    mHour = 0;
                }

                Message msg = UpdateClockTimer_Handler.obtainMessage();
                UpdateClockTimer_Handler.sendMessage(msg);

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final Handler Alarm_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            PlayAlarm();
            Toast.makeText(getApplicationContext(),
                    R.string.alarm_message,
                    Toast.LENGTH_LONG).show();
        }
    };

    private final Handler btn_start_pause_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            btn_start_pause.setText(R.string.start);
        }
    };

    private class TimerRefresher extends Thread {

        private boolean is_thread_canceled = false;

        public void cancel() {
            is_thread_canceled = true;
        }

        @Override
        public void run() {
            while (!is_thread_canceled) {
                if (mHour == 0 && mMinute == 0 && mSecond == 0) {
                    PlayAlarm();

                    is_timer_paused = true;
                    is_timer_cleared = true;

                    Message msg;
                    msg = Alarm_Handler.obtainMessage();
                    Alarm_Handler.sendMessage(msg);
                    msg = btn_start_pause_Handler.obtainMessage();
                    btn_start_pause_Handler.sendMessage(msg);

                    return;
                }

                if (is_timer_paused) {
                    return;
                }

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    continue;
                }

                mSecond--;

                if (mSecond == 0) {
                    if (mMinute != 0) {
                        mSecond = 59;
                        mMinute--;
                    } else {
                        mSecond = 0;
                    }
                }

                if (mMinute == 0) {
                    if (mHour != 0) {
                        mMinute = 59;
                        mHour--;
                    } else {
                        mMinute = 0;
                    }
                }

                if (mHour < 0) {
                    mHour = 0;
                }

                Message msg = UpdateClockTimer_Handler.obtainMessage();
                UpdateClockTimer_Handler.sendMessage(msg);
            }
        }
    }
}
