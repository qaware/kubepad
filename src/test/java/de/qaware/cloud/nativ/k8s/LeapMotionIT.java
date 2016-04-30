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
package de.qaware.cloud.nativ.k8s;

import com.leapmotion.leap.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Basic tests for the Leap Motion Java API.
 * <p>
 * Make sure you have set the native code library path correctly in your IDE.
 * <p>
 * -Djava.library.path=/Users/mario-leander.reimer/Projekte/Move2Cloud/part-3/lib
 */
public class LeapMotionIT {

    private Controller controller;
    private Listener listener;

    @Before
    public void setUp() throws Exception {
        controller = new Controller();
        listener = new TestListener();
    }

    @After
    public void tearDown() throws Exception {
        controller.removeListener(listener);
    }

    @Test
    public void testConnected() throws Exception {
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);

        controller.addListener(listener);
        assertThat(controller.isConnected(), is(true));

        consoleIn();
    }

    private void consoleIn() {
        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TestListener extends Listener {
        @Override
        public void onConnect(Controller controller) {
            System.out.println("Connected");
        }

        @Override
        public void onFrame(Controller controller) {
            // Get the most recent frame and report some basic information
            Frame frame = controller.frame();
            if (frame.gestures().count() > 0) {
                System.out.println("Frame id: " + frame.id()
                        + ", timestamp: " + frame.timestamp()
                        + ", hands: " + frame.hands().count()
                        + ", fingers: " + frame.fingers().count()
                        + ", tools: " + frame.tools().count()
                        + ", gestures " + frame.gestures().count());
            }

            GestureList gestures = frame.gestures();
            for (int i = 0; i < gestures.count(); i++) {
                Gesture gesture = gestures.get(i);

                switch (gesture.type()) {
                    case TYPE_SWIPE:
                        SwipeGesture swipe = new SwipeGesture(gesture);
                        System.out.println("  Swipe id: " + swipe.id()
                                + ", " + swipe.state()
                                + ", position: " + swipe.position()
                                + ", direction: " + swipe.direction()
                                + ", speed: " + swipe.speed());
                        break;
                    case TYPE_SCREEN_TAP:
                        ScreenTapGesture screenTap = new ScreenTapGesture(gesture);
                        System.out.println("  Screen Tap id: " + screenTap.id()
                                + ", " + screenTap.state()
                                + ", position: " + screenTap.position()
                                + ", direction: " + screenTap.direction());
                        break;
                    case TYPE_KEY_TAP:
                        KeyTapGesture keyTap = new KeyTapGesture(gesture);
                        System.out.println("  Key Tap id: " + keyTap.id()
                                + ", " + keyTap.state()
                                + ", position: " + keyTap.position()
                                + ", direction: " + keyTap.direction());
                        break;
                    default:
                        System.out.println("Unknown gesture type.");
                        break;
                }
            }
        }
    }
}
