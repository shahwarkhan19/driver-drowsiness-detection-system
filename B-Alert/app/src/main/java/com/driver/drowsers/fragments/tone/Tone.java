package com.driver.drowsers.fragments.tone;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.driver.drowsers.R;
import com.driver.drowsers.databinding.FragmentToneBinding;
import com.driver.drowsers.helper.AlertPref;

import java.io.IOException;

public class Tone extends Fragment {
    private LinearLayout linearLayout;
    private RelativeLayout relativeLayout;
    private ImageView menu, backPress;
    private TextView tone, homeText, profile, mapTop;

    MediaPlayer mp;

    private ToneViewModel mViewModel;

    private FragmentToneBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentToneBinding.inflate(getLayoutInflater(), container, false);

        initMediaPlayer();
        return binding.getRoot();
    }

    private void initMediaPlayer() {
        mp = new MediaPlayer();
        mp.setVolume(1f, 1f);
        mp.setLooping(false);

        mp.setOnCompletionListener(currentMP -> {
            currentMP.reset();
//            currentMP.release();
            currentMP = null;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearLayout = requireActivity().findViewById(R.id.linearLayoutBottom);
        relativeLayout = requireActivity().findViewById(R.id.relativeLayout2Top);
        menu = requireActivity().findViewById(R.id.menu_top);
        backPress = requireActivity().findViewById(R.id.back_press);
        homeText = requireActivity().findViewById(R.id.face_alert_top);
        tone = requireActivity().findViewById(R.id.tone_top);
        profile = requireActivity().findViewById(R.id.profile_top);
        mapTop = requireActivity().findViewById(R.id.map_top);

        mapTop.setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.INVISIBLE);
        menu.setVisibility(View.INVISIBLE);
        backPress.setVisibility(View.VISIBLE);
        profile.setVisibility(View.INVISIBLE);
        homeText.setVisibility(View.INVISIBLE);
        tone.setVisibility(View.VISIBLE);
        relativeLayout.setVisibility(View.VISIBLE);

        backPress.setOnClickListener(view1 -> requireActivity().onBackPressed());


        binding.radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {

            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.alert:
                    play("alarm.wav");
                    break;
                case R.id.car:
                    play("Car.mp3");
                    break;
                case R.id.dandelions:
                    play("Dandelions - English song.mp3");
                    break;

                case R.id.no_sleep:
                    play("No Sleep No Sleep No Sleep - Talk Song ! English.mp3");
                    break;
                case R.id.oppo_tone:
                    play("Oppo Tone - Alert.mp3");
                    break;
                case R.id.sleep:
                    play("Sleep.mp3");
                    break;
                case R.id.war:
                    play("War.mp3");
                    break;
                case R.id.war_alarm:
                    play("War Alarm.mp3");
                    break;
                case R.id.war_alert_tone:
                    play("War Alert Tone.mp3");
                    break;
                case R.id.war_siren:
                    play("War Siren.mp3");
                    break;
                case R.id.zig_alert:
                    play("Zig Alert Tone.mp3");
                    break;
            }
        });

    }

    private void play(String soundPath) {

        AlertPref.putValue(AlertPref.CURRENT_TUNE, soundPath);

        if (mp != null && isPlaying()) {
            mp.reset();
        }

        Log.i("MyPath", "play: " + soundPath);

        AssetFileDescriptor descriptor = null;
        try {
            descriptor = requireActivity().getAssets().openFd(soundPath);
            mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mp.start();
    }

    private Boolean isPlaying() {
        try {
            return mp.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp != null && isPlaying())
            mp.stop();
    }
}