package com.timepath.audio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author TimePath
 */
public class AudioTest {
    private static final int BUFFER_SIZE = 1024 * 8;

    public static void main(String... args) {
        try {
            new AudioTest();
        } catch (Exception ex) {
            Logger.getLogger(AudioTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    float volume = 8000;
    float sampleRate = 44100;
    int sampleSizeInBits = 16;
    int channels = 2;
    boolean signed = true;
    boolean bigEndian = false;

    public AudioTest() throws Exception {
        AudioFormat af = new AudioFormat(
                sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian);
        Info info = new Info(SourceDataLine.class, af);
        SourceDataLine l = (SourceDataLine) AudioSystem.getLine(info);
        l.open(af, BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        ArrayList<InputStream> iss = new ArrayList<InputStream>();
        for (int c = 0; c < 1; c++) {
            for (int i = 0; i < 12 * 5; i++) {
                iss.add(piano(i, 3, 0.001f));
            }
            for (int i = 12 * 5; i > 0; i--) {
                iss.add(piano(i, 3, 0.01f));
            }
        }
//        iss.add(new BufferedInputStream(new FileInputStream("/home/timepath/Dropbox/Scripts/audio/trial (copy).wav")));
        long start = System.currentTimeMillis();
        System.out.println("Playing");
        l.start();
        for (InputStream is : iss) {
            AudioInputStream ais = new AudioInputStream(is, af, is.available() / af.getFrameSize());
//            try {
//                ais = AudioSystem.getAudioInputStream(is);
//                AudioFormat af2 = ais.getFormat();
//                System.out.println(af2);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            File f = new File("dump/out" + i + ".wav");
//            f.createNewFile();
//            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new FileOutputStream(f));
//            ais.reset();
            while ((read = ais.read(buffer, 0, buffer.length)) != -1) {
                l.write(buffer, 0, read);
            }
        }
        l.drain();
        l.close();
        System.out.println(System.currentTimeMillis() - start);
    }

    private InputStream tone(float duration, float freq) {
        return tone(duration, freq, freq);
    }

    private InputStream tone(float duration, float startFreq, float endFreq) {
        return tone(duration, startFreq, endFreq, 0);
    }

    private InputStream tone(float duration, float startFreq, float endFreq, float timeOff) {
        byte[] buf = new byte[(int) (duration * channels * sampleRate * (sampleSizeInBits / 8))];
        ByteBuffer bb = ByteBuffer.wrap(buf);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer b = bb.asShortBuffer();
        float sampleLength = buf.length / (sampleSizeInBits / 8);
        float freq = startFreq;
        for (int i = 0; i < sampleLength; i += channels) {
            float time = (i / sampleRate) + timeOff; // seconds
            double seek = time / (duration + timeOff); // between 0 and 1
//            freq = (float) (startFreq + ((endFreq - startFreq) * seek)); // lerp
            double sin = (Math.sin(2 * Math.PI * time * freq));
            switch (channels) {
                case 1:
                    b.put((short) (volume * sin));
                    break;
                case 2:
                    double rightGain = volume * seek;
                    rightGain = volume;
                    double leftGain = volume - rightGain;
                    b.put((short) (rightGain * sin));
                    b.put((short) (leftGain * sin));
                    break;
            }
        }
        InputStream is = new ByteArrayInputStream(buf);
        return is;
    }

    private InputStream piano(int i) {
        return piano(i, 4, 0.1f); // 4 = default
    }

    /**
     * 440 = A in octave 4
     *
     * pow(2, (i-9)/12.0) * pow(2, o) * 27.5
     * http://en.wikipedia.org/wiki/Scientific_pitch_notation 0, 4 = middle C
     *
     * @param i Places from C: 0-12
     * @param octave 13 places per octave
     * @return
     */
    private InputStream piano(int i, int octave, float len) {
        return tone(len, (float) (Math.pow(2, (i - 9) / 12.0) * Math.pow(2, octave) * 27.5f));
    }
}
