package com.example.usuario.personalassistant;

import android.os.Environment;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import br.ufc.great.loccamlib.LoccamListener;
import br.ufc.great.loccamlib.LoccamManager;
import br.ufc.great.syssu.base3.Provider;
import br.ufc.great.syssu.base.Tuple;
import br.ufc.great.syssu.base.interfaces.IClientReaction;
import br.ufc.great.syssu.base.interfaces.ISysSUService;

public class MainActivity extends AppCompatActivity implements LoccamListener {

    private LoccamManager loccam;

    private final static String CONTEXT_KEY1 = "context.watch.androidwear";

    private TextView tvHeartRate;
    private TextView tvStepCount;

    private double heartRate;
    private double stepCount;

    private SyssuManager mSyssu;

    private final static String MYID = "PersonalAssistant";
    private int messageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            createFileOnDevice(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSyssu = SyssuManager.getInstance(this);
        mSyssu.start();

        tvHeartRate = (TextView) findViewById(R.id.tv_watch_hr);
        tvStepCount = (TextView) findViewById(R.id.tv_watch_sc);

        loccam = new LoccamManager(this, MYID);
        loccam.connect(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loccam.disconnect();
    }

    @Override
    public void onServiceConnected(ISysSUService iSysSUService) {

        loccam.init(CONTEXT_KEY1);

        IClientReaction reaction1 = new IClientReaction.Stub(){

            @Override
            public void react(Tuple tuple) throws RemoteException {

                heartRate = -1;
                stepCount = -1;

                try {
                    String dataS = tuple.getField(2).getValue().toString();
                    JSONObject data = new JSONObject(dataS.substring(1, dataS.length() - 1));
                    heartRate = Double.valueOf(data.getJSONObject("HeartRate").getString("bpm"));
                    stepCount = Double.valueOf(data.getJSONObject("StepCount").getString("step_count"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                br.ufc.great.syssu.base3.Tuple t = new br.ufc.great.syssu.base3.Tuple();
                t.addField("id", MYID);
                t.addField("heartrate", "" + heartRate);
                t.addField("stepcount", "" + stepCount);
                t.addField("messageCount", "" + messageCount);

                if(out != null) {
                    log("id=" + MYID + ",heartrate=" + heartRate + ",stepcount=" + stepCount + ",timestamp=" + System.currentTimeMillis() + ",messagecount=" + messageCount);
                }

                mSyssu.put(t, Provider.ADHOC);
                messageCount++;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvHeartRate.setText("Heart Rate: " + heartRate);
                        tvStepCount.setText("Step Count: " + stepCount);
                    }
                });
            }
        };

        loccam.getASync(reaction1, CONTEXT_KEY1);
    }

    private void log(final String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeToFile(s);
            }
        }).start();
    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onLoccamException(Exception e) {

    }

    public static BufferedWriter out;

    private void createFileOnDevice(Boolean append) throws IOException {
                /*
                 * Function to initially create the log file and it also writes the time of creation to file.
                 */
        File Root = Environment.getExternalStorageDirectory();
            File  LogFile = new File(Root, "Log.txt");
            FileWriter LogWriter = new FileWriter(LogFile, append);
            out = new BufferedWriter(LogWriter);
            Date date = new Date();
            out.write("Logged at" + String.valueOf(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "\n"));
            out.flush();

        Log.d("aaaa", "toooo aquiiiii");
    }

    public void writeToFile(String message) {
        try {
            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
