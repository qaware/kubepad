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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import java.util.Arrays;

import static de.qaware.cloud.nativ.kpad.launchpad.LaunchpadMK2.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration tests to display the DC/OS logo in the launchpad.
 */
public class DcosLogoIT {

    private Transmitter transmitter;
    private Receiver receiver;

    @Before
    public void setUp() throws Exception {
        // these here do not seem to make any difference
        // maybe when there are multiple MIDI devices
        System.setProperty("javax.sound.midi.Transmitter", "com.sun.media.sound.MidiInDeviceProvider#Launchpad MK2");
        System.setProperty("javax.sound.midi.Receiver", "com.sun.media.sound.MidiOutDeviceProvider#Launchpad MK2");

        transmitter = MidiSystem.getTransmitter();
        assertThat(transmitter, is(notNullValue()));

        receiver = MidiSystem.getReceiver();
        assertThat(receiver, is(notNullValue()));
    }

    @After
    public void tearDown() throws Exception {
        transmitter.close();
        receiver.close();
    }

    @Test
    public void testDisplayDcosLogo() throws Exception {
        reset();
        top();
        right();
        dcos();
    }

    private void dcos() {
        // D
        new Square(7, 0).on(receiver, Color.CYAN);
        new Square(7, 1).on(receiver, Color.CYAN);
        new Square(6, 0).on(receiver, Color.CYAN);
        new Square(6, 2).on(receiver, Color.CYAN);
        new Square(5, 0).on(receiver, Color.CYAN);
        new Square(5, 2).on(receiver, Color.CYAN);
        new Square(4, 0).on(receiver, Color.CYAN);
        new Square(4, 1).on(receiver, Color.CYAN);

        // C
        new Square(7, 4).on(receiver, Color.ORANGE);
        new Square(7, 5).on(receiver, Color.ORANGE);
        new Square(6, 3).on(receiver, Color.ORANGE);
        new Square(5, 3).on(receiver, Color.ORANGE);
        new Square(4, 4).on(receiver, Color.ORANGE);
        new Square(4, 5).on(receiver, Color.ORANGE);

        // O
        new Square(1, 0).on(receiver, Color.PURPLE);
        new Square(2, 0).on(receiver, Color.PURPLE);
        new Square(0, 1).on(receiver, Color.PURPLE);
        new Square(3, 1).on(receiver, Color.PURPLE);
        new Square(0, 2).on(receiver, Color.PURPLE);
        new Square(3, 2).on(receiver, Color.PURPLE);
        new Square(1, 3).on(receiver, Color.PURPLE);
        new Square(2, 3).on(receiver, Color.PURPLE);

        // S
        new Square(0, 5).on(receiver, Color.LIGHT_GREEN);
        new Square(0, 6).on(receiver, Color.LIGHT_GREEN);
        new Square(1, 7).on(receiver, Color.LIGHT_GREEN);
        new Square(2, 6).on(receiver, Color.LIGHT_GREEN);
        new Square(3, 5).on(receiver, Color.LIGHT_GREEN);
        new Square(4, 7).on(receiver, Color.LIGHT_GREEN);
        new Square(4, 6).on(receiver, Color.LIGHT_GREEN);

    }

    private void right() {
        Button.Companion.right(0).on(receiver, Color.PURPLE);
        Button.Companion.right(1).on(receiver, Color.PURPLE);
        Button.Companion.right(2).on(receiver, Color.PURPLE);
        Button.Companion.right(3).on(receiver, Color.PURPLE);
        Button.Companion.right(4).on(receiver, Color.PURPLE);
        Button.Companion.right(5).on(receiver, Color.PURPLE);
        Button.Companion.right(6).on(receiver, Color.PURPLE);
        Button.Companion.right(7).on(receiver, Color.PURPLE);
    }

    private void top() {
        Button.CURSOR_UP.on(receiver, Color.BLUE);
        Button.CURSOR_DOWN.on(receiver, Color.BLUE);
        Button.CURSOR_LEFT.on(receiver, Color.BLUE);
        Button.CURSOR_RIGHT.on(receiver, Color.BLUE);

        Button.SESSION.on(receiver, Color.RED);
        Button.USER_1.on(receiver, Color.LIGHT_GREEN);
        Button.USER_2.on(receiver, Color.YELLOW);
        Button.MIXER.on(receiver, Color.LIGHT_BLUE);
    }

    private void reset() {
        Arrays.stream(Button.values()).forEach(button -> button.off(receiver));

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                new Square(r, c).off(receiver);
            }
        }
    }
}
