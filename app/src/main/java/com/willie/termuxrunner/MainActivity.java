package com.willie.termuxrunner;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView outputText;
    private EditText inputText;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TermuxRunner::WakeLock");

        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        Button runCommandButton = findViewById(R.id.runCommandButton);

        IntentFilter filter = new IntentFilter("com.termux.RUN_COMMAND_OUTPUT");

        runCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = inputText.getText().toString().trim();
                inputText.setText("");
                outputText.append(command + "\n");
                if ("Exit".equals(command)) finish();
                else if (!command.isEmpty()) runCommandInTermux(command);
                else outputText.append("Please enter a command, type 'Exit' to exit the app\n");
            }
        });

        outputText.append("Please enter a command, type 'Exit' to exit the app\n");
    }

    private void runCommandInTermux(String command) {
        Intent intent = new Intent();
        intent.setClassName("com.termux", "com.termux.app.RunCommandService");
        intent.setAction("com.termux.RUN_COMMAND");

        intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash");
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", command});
        intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home");
        intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);

        try {
            startService(intent);
            outputText.append("Command sent to Termux\n");
        } catch (Exception e) {
            outputText.append("Error sending command: " + e.getMessage() + "\n");
        }
    }
}