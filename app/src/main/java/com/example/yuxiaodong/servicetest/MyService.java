package com.example.yuxiaodong.servicetest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;


public class MyService extends Service{

    SensorManager mySensorManager;
    final float[] mValuesMagnet = new float[3]     ;
    final float[] mValuesAccel  = new float[3]    ;
    final float[] mValuesOrientation  = new float[3];
    final float[] mRotationMatrix   = new float[9] ;
    int num_update=0;
    long time_last, time_cur;
    long time_passed = 1000*5;
    final float toDegree= (float) (180.0/3.14159265);
    float threshold = (float) 58.0;


    private void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendNotificationMessage(String text){
        NotificationManagerCompat notificationManager = (NotificationManagerCompat) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification  notification = new NotificationCompat.Builder(this)
                .setContentTitle("MyService")
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        notificationManager.notify(1, notification);
    }

    public MyService() {
        Log.d("MyService","in MyService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate(){
        super.onCreate();
        //Toast.makeText(getApplicationContext(),"service is created",
         //       Toast.LENGTH_LONG).show();
        Log.d("MyService","onCreateService");
        mySensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Intent intent[] = {new Intent(this, MainActivity.class)};
        PendingIntent pi = PendingIntent.getActivities(this, 0, intent, 0);
        Notification  notification = new NotificationCompat.Builder(this, "Channel_ID")
                .setContentTitle("RaiseHead")
                .setContentText("RaiseHead")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round))
                .setContentIntent(pi)
                .build();
        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(1, notification);
        startForeground(1,notification);


        Log.d("MyService","new SensorManager done");
        time_last = System.currentTimeMillis();


    }

    private SensorEventListener mySensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            int update = 0;
            String Roll, Pitch, Yaw;
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER: // static member
                    System.arraycopy(event.values, 0, mValuesAccel, 0, 3);
                    update =1;
                    Log.d("MyService","ACC update");
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
                    update =1 ;
                    Log.d("MyService","Magnetic update");
                    break;
            }
            if(update ==1) {
                SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel, mValuesMagnet);// static methord
                SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);// static method
                final CharSequence test;
                test = "results: " + mValuesOrientation[0]+" "+mValuesOrientation[1]+ " "+ mValuesOrientation[2];

                time_cur  = System.currentTimeMillis();
                if( time_cur - time_last > time_passed){
                    time_last = time_cur;
                    float f_pitch = mValuesOrientation[1]*toDegree;
                    float f_roll = mValuesOrientation[2]*toDegree;
                    Yaw=String.format("%4d:Y=%3.0f,", num_update++,mValuesOrientation[0]*toDegree);
                    Pitch=String.format("P=%3.0f,", mValuesOrientation[1]*toDegree);
                    Roll=String.format("R=%3.0f", mValuesOrientation[2]*toDegree);
                    Log.d("MyService",Yaw+Pitch+Roll);
                    //sendNotificationMessage(Yaw+Pitch+Roll);
                    if (Math.abs(f_pitch) < threshold && Math.abs(f_roll)< threshold) {
                        Toast.makeText(getApplicationContext(), "Raise Head\n"+Yaw + Pitch + Roll,
                                Toast.LENGTH_SHORT).show();
                    }
                }

                //myTextView1.setText(Yaw);
                //myTextView2.setText(Pitch);
                //myTextView3.setText(Roll);
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
/*
     new Thread(new Runnable(){
            @Override
            public void run(){
        for(int i =0; i<5;i++) {
            delay(1000);
              Toast.makeText(getApplicationContext(), "service is created",
                      Toast.LENGTH_LONG).show();
            Log.d("MyService","loopi:"+i);
        }
            }
        }).start();
*/
        mySensorManager.registerListener(mySensorEventListener,
                mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);//200000);//
        mySensorManager.registerListener(mySensorEventListener,
                mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);//200000);//
        Log.d("MyService","OnStartCommand: sensorEventListener registered");
            return super.onStartCommand(intent,flags, startId);
    }

    @Override
    public void onDestroy(){
        mySensorManager.unregisterListener(mySensorEventListener);
        stopForeground(true);
        super.onDestroy();
        Log.d("MyService","OnDestroy unregister listener");
    }


}
