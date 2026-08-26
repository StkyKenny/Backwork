package me.stky.backwork.service;

import me.stky.backwork.gui.MainFrame;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class SfxService {

    private MainFrame mainFrame;

    public SfxService(MainFrame mainframe) {
        this.mainFrame = mainframe;
    }

    public void playSound(String sfxFilepath) throws Exception {

        AudioInputStream audioInputStream =
                AudioSystem.getAudioInputStream(new File(sfxFilepath));
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();

    }
}
