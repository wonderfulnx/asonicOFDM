package com.example.asonicofdm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    protected static final String TAG = "RecordActivity";
    private Button generate_btn;
    private Button play_btn;
    private Button record_btn;
    private Button analyze_btn;
    private Button clear_btn;
    private EditText editText;
    private Recorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();

        // editText init
        this.editText = this.findViewById(R.id.editText);
        this.editText.setKeyListener(null);

        // generate signal init
        this.generate_btn = this.findViewById(R.id.generate);
        this.generate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // generate signal and show text
                OFDMConfig con = new OFDMConfig(false);
                OFDMConfig con_pre = new OFDMConfig(true);
                double[] Preamble = OFDM.modulate(con_pre, con_pre.preamble);
                int[] bits = Utils.generate_rand(con.baseband_length);
                MainActivity.this.logToDisplay("Generate Binary: " + Utils.bin2str(bits));
                double[] Tx_data = OFDM.modulate(con, bits);
                Utils.writeMessage(Preamble, Tx_data);
            }
        });

        // play signal init
        this.play_btn = this.findViewById(R.id.play);
        this.play_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // play signal
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(Utils.messageFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.setLooping(false);
                    mediaPlayer.start();

                    MainActivity.this.generate_btn.setEnabled(false);
                    MainActivity.this.play_btn.setEnabled(false);
                    MainActivity.this.record_btn.setEnabled(false);
                    MainActivity.this.analyze_btn.setEnabled(false);
                    MainActivity.this.clear_btn.setEnabled(false);

                    while (mediaPlayer.isPlaying());
                    mediaPlayer.stop();
                    mediaPlayer.release();

                    MainActivity.this.generate_btn.setEnabled(true);
                    MainActivity.this.play_btn.setEnabled(true);
                    MainActivity.this.record_btn.setEnabled(true);
                    MainActivity.this.analyze_btn.setEnabled(true);
                    MainActivity.this.clear_btn.setEnabled(true);
                } catch (IOException e) {
                    Log.e(TAG, "unable to play source");
                }
            }
        });

        // Record btn init
        this.record_btn = this.findViewById(R.id.record);
        this.record_btn.setOnClickListener(new View.OnClickListener() {
            boolean recording = false;
            @Override
            public void onClick(View view) {
                if (this.recording) {
                    this.recording = false;
                    //结束录音

                    MainActivity.this.recorder.recording = false;
                    try {
                        MainActivity.this.recorder.join();
                    } catch (InterruptedException e){
                        Log.e(TAG, "record thread Interrupted;");
                    }

                    MainActivity.this.logToDisplay("end the record.");

                    MainActivity.this.generate_btn.setEnabled(true);
                    MainActivity.this.play_btn.setEnabled(true);
                    MainActivity.this.record_btn.setText("Start Record");
                    MainActivity.this.analyze_btn.setEnabled(true);
                }
                else {
                    this.recording = true;
                    // 开始录音
                    MainActivity.this.logToDisplay("start the record.");
                    recorder = new Recorder();
                    recorder.start();
                    MainActivity.this.generate_btn.setEnabled(false);
                    MainActivity.this.play_btn.setEnabled(false);
                    MainActivity.this.record_btn.setText("Stop Record");
                    MainActivity.this.analyze_btn.setEnabled(false);
                }
            }
        });

        // analyze btn init
        this.analyze_btn = this.findViewById(R.id.analyze);
        this.analyze_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // analyze the signal
                OFDMConfig con = new OFDMConfig(false);
                OFDMConfig con_pre = new OFDMConfig(true);
                double[] Rx_sound = Utils.readRaw();
                double[] Rx_data = Sync.sync(con_pre, con, Rx_sound);
                if (Rx_data.length == 0)
                    logToDisplay("No Preamble Found...");
                else {
                    int[] bits = OFDM.demodulate(con, Rx_data);
                    logToDisplay("Received Bytes: " + Utils.bin2str(bits));
                }
            }
        });

        // clear btn init
        this.clear_btn = this.findViewById(R.id.clear_btn);
        this.clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.editText.setText("");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!=
                PackageManager.PERMISSION_GRANTED||
        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    private void logToDisplay(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.editText.append(msg + "\n");
            }
        });
    }
}
