/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 QAware GmbH, Munich, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.qaware.cloud.nativ.kpad;

import org.junit.Test;

import javax.sound.midi.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Integration tests to check the midi system and a connected Novation Launchpad MK2.
 */
public class NovationLaunchPadIT {

    @Test
    public void testMidiDevicesForLaunchpadMK2() throws Exception {
        // get the list of all midi devices
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        assertThat(infos, is(notNullValue()));
        assertThat(infos.length, is(greaterThan(0)));

        // filter out the list of launch pads
        List<MidiDevice.Info> launchpads = Arrays.stream(infos)
                .filter(i -> "Launchpad MK2".equals(i.getName()))
                .collect(Collectors.toList());
        assertThat(launchpads, hasSize(2));
    }

    @Test
    public void testDefaultMidiSystemTransmitterReceiver() throws Exception {
        // these here do not seem to make any difference
        // maybe when there are multiple MIDI devices
        System.setProperty("javax.sound.midi.Transmitter", "com.sun.media.sound.MidiInDeviceProvider#Launchpad MK2");
        System.setProperty("javax.sound.midi.Receiver", "com.sun.media.sound.MidiOutDeviceProvider#Launchpad MK2");

        // check the default transmitter and receiver
        Transmitter transmitter = MidiSystem.getTransmitter();
        assertThat(transmitter, is(notNullValue()));
        transmitter.close();

        Receiver receiver = MidiSystem.getReceiver();
        assertThat(receiver, is(notNullValue()));
        receiver.close();
    }

    @Test
    public void testShortMessages() throws Exception {
        Receiver receiver = MidiSystem.getReceiver();

        receiver.send(new ShortMessage(144, 0, 11, 45), -1);
        receiver.send(new ShortMessage(144, 0, 18, 45), -1);
        receiver.send(new ShortMessage(144, 0, 81, 45), -1);
        receiver.send(new ShortMessage(144, 0, 88, 45), -1);

        TimeUnit.SECONDS.sleep(2);

        receiver.send(new ShortMessage(144, 0, 11, 0), -1);
        receiver.send(new ShortMessage(144, 0, 18, 0), -1);
        receiver.send(new ShortMessage(144, 0, 81, 0), -1);
        receiver.send(new ShortMessage(144, 0, 88, 0), -1);
    }

    @Test
    public void testPulsePurple() throws Exception {
        Receiver receiver = MidiSystem.getReceiver();

        receiver.send(new ShortMessage(146, 2, 11, 81), -1);

        TimeUnit.SECONDS.sleep(3);

        receiver.send(new ShortMessage(144, 0, 11, 0), -1);
    }

    @Test
    public void testBlinkPurple() throws Exception {
        Receiver receiver = MidiSystem.getReceiver();

        receiver.send(new ShortMessage(145, 1, 11, 81), -1);

        TimeUnit.SECONDS.sleep(3);

        receiver.send(new ShortMessage(144, 0, 11, 0), -1);
    }

    @Test
    public void testColors() throws Exception {
        System.setProperty("javax.sound.midi.Transmitter", "com.sun.media.sound.MidiInDeviceProvider#Launchpad MK2");
        System.setProperty("javax.sound.midi.Receiver", "com.sun.media.sound.MidiOutDeviceProvider#Launchpad MK2");
        Receiver receiver = MidiSystem.getReceiver();
        Transmitter transmitter = MidiSystem.getTransmitter();

        for(int b = 0; b < 2; b++) {
            final int bf = b;
            transmitter.setReceiver(simpleReceiver(id -> {
                int col = id / 10 - 1;
                int row = id - ((col + 1) * 10 + 1);
                int color = (row + (8 - col - 1) * 8) + bf * 64;
                System.out.println("Color " + color);
            }));
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    int id = 11 + i + 10 * j;
                    int command = 144;
                    receiver.send(new ShortMessage(command, 0, id, i + (8 - j - 1) * 8 + bf * 64), -1);
                }
            }
            TimeUnit.SECONDS.sleep(5);
        }

    }

    private Receiver simpleReceiver(IntConsumer receiver) {
        return new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
                if(!(message instanceof ShortMessage))
                    return;
                ShortMessage sm = (ShortMessage) message;
                if(sm.getData2() != 127)
                    return;
                receiver.accept(sm.getData1());
            }

            @Override
            public void close() {}
        };
    }

    @Test
    public void testSysexMessages() throws Exception {
        Receiver receiver = MidiSystem.getReceiver();

        byte[] data = new byte[]{(byte) 240, 0, 32, 41, 2, 24, 13, 0, 45, (byte) 247};
        SysexMessage on = new SysexMessage(data, data.length);
        receiver.send(on, -1);

        TimeUnit.SECONDS.sleep(2);

        data = new byte[]{(byte) 240, 0, 32, 41, 2, 24, 13, 0, 0, (byte) 247};
        SysexMessage off = new SysexMessage(data, data.length);
        receiver.send(off, -1);
    }
}
