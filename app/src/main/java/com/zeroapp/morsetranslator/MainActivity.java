package com.zeroapp.morsetranslator;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final Map<String,String> morseCodes = new HashMap<>();
    private Boolean isMorseCodeMode = true;
    private EditText firstEditText;
    private TextView secondTextView;
    private KeyListener storedKeyListener;
    private boolean canPlaySound = true;
    private boolean canUseVibration = true;
    private boolean canUseFlashlight = true;
    private Thread playThread = new Thread();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.placeholder);
        bottomNavigationView.getMenu().setGroupCheckable(0,false,true);
        bottomNavigationView.getMenu().getItem(1).setEnabled(false);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId()==R.id.action_space) {
                firstEditText.setText(firstEditText.getText().toString()+" ");
            } else if (item.getItemId()==R.id.action_backspace) {
                if (firstEditText.getText().length()!=0) {
                    firstEditText.setText(firstEditText.getText().toString().substring(0, firstEditText.getText().length() - 1));
                }
            }
            return true;
        });
        TextView firstTranslateModeTV = findViewById(R.id.firstTranslateModeTextView);
        TextView secondTranslateModeTV = findViewById(R.id.secondTranslateModeTextView);
        firstEditText = findViewById(R.id.firstTranslateEditText);
        storedKeyListener = firstEditText.getKeyListener();
        secondTextView = findViewById(R.id.secondTranslateTextView);
        firstEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                encrypt();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        FloatingActionButton morseInputFab = findViewById(R.id.morseInputFab);
        morseInputFab.setOnClickListener(v -> {
            firstEditText.setText(firstEditText.getText().toString()+".");
            decrypt();
        });
        morseInputFab.setOnLongClickListener(v -> {
            firstEditText.setText(firstEditText.getText().toString()+"-");
            encrypt();
            return true;
        });
        firstEditText.setKeyListener(null);
        firstEditText.setTextIsSelectable(true);
        firstEditText.setOnClickListener(null);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(firstEditText.getApplicationWindowToken(),0);
        morseCodes.put("A",".-");morseCodes.put("B","-...");morseCodes.put("C","-.-.");morseCodes.put("D","-..");morseCodes.put("E",".");morseCodes.put("F","..-.");morseCodes.put("G","--.");morseCodes.put("H","....");morseCodes.put("I","..");morseCodes.put("J",".---");morseCodes.put("K","-.-");morseCodes.put("L",".-..");morseCodes.put("M","--");morseCodes.put("N","-.");morseCodes.put("O","---");morseCodes.put("P",".--.");morseCodes.put("Q","--.-");morseCodes.put("R",".-.");morseCodes.put("S","...");morseCodes.put("T","-");morseCodes.put("U","..-");morseCodes.put("V","...-");morseCodes.put("W",".--");morseCodes.put("X","-..-");morseCodes.put("Y","-.--");morseCodes.put("Z","--..");morseCodes.put("1",".----");morseCodes.put("2","..---");morseCodes.put("3","...--");morseCodes.put("4","....-");morseCodes.put("5",".....");morseCodes.put("6","-....");morseCodes.put("7","--...");morseCodes.put("8","---..");morseCodes.put("9","----.");morseCodes.put("0","-----");morseCodes.put(", ","--..--");morseCodes.put(".",".-.-.-");morseCodes.put("?","..--..");morseCodes.put("/","-..-.");morseCodes.put("-","-....-");morseCodes.put("(","-.--.");morseCodes.put(")","-.--.-");
        CardView playCardView = findViewById(R.id.playCardView);
        CardView reverseModeCardView = findViewById(R.id.reverseModeCardView);
        reverseModeCardView.setOnClickListener(v -> {
            if (isMorseCodeMode) {
                playCardView.setVisibility(View.VISIBLE);
                morseInputFab.hide();
                bottomAppBar.setVisibility(View.GONE);
                firstTranslateModeTV.setText(getString(R.string.text));
                secondTranslateModeTV.setText(getString(R.string.morse_code));
                firstEditText.setKeyListener(storedKeyListener);
                firstEditText.setTextIsSelectable(true);
                firstEditText.setOnClickListener(v1 -> imm.showSoftInput(firstEditText,0));
                firstEditText.setSelection(firstEditText.getText().length());
                imm.showSoftInput(firstEditText, 0);
                firstEditText.setText(secondTextView.getText());
                isMorseCodeMode = false;
                encrypt();
            } else {
                playCardView.setVisibility(View.GONE);
                morseInputFab.show();
                bottomAppBar.setVisibility(View.VISIBLE);
                firstTranslateModeTV.setText(getString(R.string.morse_code));
                secondTranslateModeTV.setText(getString(R.string.text));
                firstEditText.setKeyListener(null);
                firstEditText.setTextIsSelectable(true);
                firstEditText.setOnClickListener(null);
                imm.hideSoftInputFromWindow(firstEditText.getApplicationWindowToken(),0);
                firstEditText.setText(secondTextView.getText());
                isMorseCodeMode = true;
                decrypt();
            }
        });
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        String camId = null;
        try {
            camId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC,100);
        ImageButton useFlashlightBtn = findViewById(R.id.useFlashlightBtn);
        useFlashlightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canUseFlashlight) {
                    useFlashlightBtn.setImageResource(R.drawable.ic_baseline_flashlight_off_24);
                } else {
                    useFlashlightBtn.setImageResource(R.drawable.ic_baseline_flashlight_on_24);
                }
                canUseFlashlight = !canUseFlashlight;
            }
        });
        ImageButton useVibrationBtn = findViewById(R.id.useVibrationBtn);
        useVibrationBtn.setOnClickListener(v -> {
            if (canUseVibration) {
                useVibrationBtn.setImageResource(R.drawable.ic_baseline_mobile_off_24);
            } else {
                useVibrationBtn.setImageResource(R.drawable.ic_baseline_vibration_24);
            }
            canUseVibration = !canUseVibration;
        });
        ImageButton useSoundBtn = findViewById(R.id.useSoundBtn);
        useSoundBtn.setOnClickListener(v -> {
            if (canPlaySound) {
                useSoundBtn.setImageResource(R.drawable.ic_baseline_volume_off_24);
            } else {
                useSoundBtn.setImageResource(R.drawable.ic_baseline_volume_up_24);
            }
            canPlaySound = !canPlaySound;
        });
        ImageButton playBtn = findViewById(R.id.playBtn);
        String finalCamId = camId;
        playBtn.setOnClickListener(v -> {
            if (playThread.isAlive()) {
                playThread.interrupt();
                playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_48);
            } else {
                playThread = new Thread(() -> {
                    String morseText = secondTextView.getText().toString();
                    if (!morseText.equals("Error")) {
                        for (int i = 0;i < morseText.length();i++) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canUseFlashlight) {
                                try {
                                    cameraManager.setTorchMode(finalCamId,false);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Thread.sleep(300L);
                            } catch (InterruptedException e) {
                                break;
                            }
                            String morseLetter = morseText.substring(i,i+1);
                            if (morseLetter.equals(".")) {
                                if (canUseVibration) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                                    } else {
                                        vibrator.vibrate(200);
                                    }
                                }
                                if (canPlaySound) {
                                    toneGen.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,200);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canUseFlashlight) {
                                    try {
                                        cameraManager.setTorchMode(finalCamId,true);
                                    } catch (CameraAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    Thread.sleep(200L);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            } else if (morseLetter.equals("-")) {
                                if (canUseVibration) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE));
                                    } else {
                                        vibrator.vibrate(800);
                                    }
                                }
                                if (canPlaySound) {
                                    toneGen.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,800);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canUseFlashlight) {
                                    try {
                                        cameraManager.setTorchMode(finalCamId,true);
                                    } catch (CameraAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    Thread.sleep(800L);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            cameraManager.setTorchMode(finalCamId,false);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_48);
                });
                playBtn.setImageResource(R.drawable.ic_baseline_stop_48);
                playThread.start();
            }
        });
    }
    private String getLetterByMorseCode(String morseCode) {
        for (int i = 0;i < morseCodes.size();i++) {
            if (morseCodes.get(morseCodes.keySet().toArray()[i]).equals(morseCode)) {
                return (String) morseCodes.keySet().toArray()[i];
            }
        }
        return null;
    }
    private void decrypt() {
        String[] morseList = firstEditText.getText().toString().split(" ");
        String translateResult = "";
        for (String morse : morseList) {
            String foundLetter = getLetterByMorseCode(morse);
            if (!morse.isEmpty()) {
                if (foundLetter != null) {
                    translateResult += foundLetter;
                } else {
                    translateResult = "Error";
                    break;
                }
            }
        }
        secondTextView.setText(translateResult);
    }
    private void encrypt() {
        if (!isMorseCodeMode) {
            String s = firstEditText.getText().toString();
            String morseResult = "";
            String inputText = s.toUpperCase();
            for (int i = 0; i < s.length(); i++) {
                String letter = String.valueOf(inputText.charAt(i));
                if (!letter.equals(" ")) {
                    if (!morseCodes.containsKey(letter)) {
                        morseResult = "Error";
                        break;
                    }
                    morseResult += morseCodes.get(letter)+" ";
                }
            }
            secondTextView.setText(morseResult);
        }
    }
}