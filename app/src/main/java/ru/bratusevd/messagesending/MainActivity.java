package ru.bratusevd.messagesending;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private final String phoneNumber = "+7(***)**-**-***";
    private final int minLength = 1;
    private final int maxLength = 20;
    private final int REQUEST_CODE_PERMISSION = 8;
    private ArrayList<CustomCommand> commandList = new ArrayList<>();
    private int position = 0;

    private Button sendBtn;
    private EditText messageText;
    private LinearLayout commandLayout;
    private ImageView image;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        findViews();
        fillList();
        layoutChange(position);
        layoutSwipe();
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            sendBtn.setOnClickListener(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_CODE_PERMISSION);
        }
    }

    private void layoutSwipe(){
        commandLayout.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeRight() {
                layoutChange(--position);
            }
            public void onSwipeLeft() {
                layoutChange(++position);
            }
        });
    }

    private void layoutChange(int newPosition){
        if(newPosition == commandList.size()){ newPosition = 0; position = 0;}
        if(newPosition == -1){ newPosition = commandList.size()-1; position = newPosition;}
        image.setImageResource(commandList.get(newPosition).getResId());
        textView.setText(commandList.get(newPosition).getNameSign());
    }

    private void fillList() {
        commandList.add(new CustomCommand("/deti", R.drawable.ic_deti, "Дети"));
        commandList.add(new CustomCommand("/veter", R.drawable.ic_veter, "Боковой ветер"));
        commandList.add(new CustomCommand("/ogr_skor20", R.drawable.ogr20, "Максималь-\nная скорость 20 км/ч"));
        commandList.add(new CustomCommand("/ogr_skor40", R.drawable.ogr40, "Максималь-\nная скорость 40 км/ч"));
        commandList.add(new CustomCommand("/dor_raboty", R.drawable.dorojnie_raboti, "Дорожные работы"));
        commandList.add(new CustomCommand("/perehod", R.drawable.perehod, "Пешеходный переход"));
    }

    private void findViews() {
        sendBtn = findViewById(R.id.sendBtn);
        messageText = findViewById(R.id.textMessage);
        commandLayout = findViewById(R.id.myLayout);
        image = findViewById(R.id.commandImage);
        textView = findViewById(R.id.commandText);
    }

    private void sendClick() {
        String message = messageText.getText().toString();
        sendSms(commandList.get(position).getCommandText());
        /*int messageSize = message.length();
        if (messageSize == 0) makeToast(getResources().getString(R.string.zero_length));
        else if (messageSize < minLength)
            makeToast(getResources().getString(R.string.min_length) + minLength);
        else if (messageSize > maxLength)
            makeToast(getResources().getString(R.string.max_length) + ": " + maxLength);
        else sendSms(message);*/
    }

    private void sendSms(String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendBtn.setOnClickListener(this);
                } else {
                    makeToast(getResources().getString(R.string.permission_deny));
                }
                return;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendBtn:
                sendClick();
                break;
            default:
                break;
        }
    }
}

class CustomCommand {
    private String commandText;
    private int resId;
    private String nameSign;

    public CustomCommand(String commandText, int resId, String nameSign) {
        this.commandText = commandText;
        this.resId = resId;
        this.nameSign = nameSign;
    }

    public String getCommandText() {
        return commandText;
    }

    public int getResId() {
        return resId;
    }

    public String getNameSign() {
        return nameSign;
    }
}


class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener (Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }
}