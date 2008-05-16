/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
  @test
  @bug      4782886
  @summary  FocusManager consumes wrong KEY_TYPED events
  @author   Oleg.Sukhodolsky: area=awt.focus
  @library  ../../regtesthelpers
  @build    Util
  @run      main WrongKeyTypedConsumedTest
*/

import java.applet.Applet;
import java.awt.AWTException;
import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Robot;
import java.awt.TextArea;

import java.awt.event.KeyEvent;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import test.java.awt.regtesthelpers.Util;

public class WrongKeyTypedConsumedTest extends Applet
{
    Robot robot = Util.createRobot();

    public static void main(String[] args) {
        WrongKeyTypedConsumedTest test = new WrongKeyTypedConsumedTest();
        test.start();
    }

    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();

        JFrame frame = new JFrame("The Frame");
        Set ftk = new HashSet();
        ftk.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_DOWN, 0));
        frame.getContentPane().
            setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                  ftk);

        JCheckBox checkbox = new JCheckBox("test");
        frame.getContentPane().add(checkbox, BorderLayout.NORTH);

        JTextArea textarea = new JTextArea(40, 10);
        frame.getContentPane().add(textarea);

        frame.pack();
        frame.setVisible(true);
        Util.waitForIdle(robot);

        if (!frame.isActive()) {
            throw new RuntimeException("Test Fialed: frame isn't active");
        }

        // verify if checkbox has focus
        if (!checkbox.isFocusOwner()) {
            checkbox.requestFocusInWindow();
            Util.waitForIdle(robot);
            if (!checkbox.isFocusOwner()) {
                throw new RuntimeException("Test Failed: checkbox doesn't have focus");
            }
        }

        // press VK_DOWN
        robot.keyPress(KeyEvent.VK_DOWN);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_DOWN);
        robot.delay(50);

        Util.waitForIdle(robot);

        // verify if text area has focus
        if (!textarea.isFocusOwner()) {
            throw new RuntimeException("Test Failed: focus wasn't transfered to text area");
        }
        // press '1'
        robot.keyPress(KeyEvent.VK_1);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_1);
        robot.delay(50);

        Util.waitForIdle(robot);

        // verify if KEY_TYPED arrived
        if (!"1".equals(textarea.getText())) {
            throw new RuntimeException("Test Failed: text area text is \"" + textarea.getText() + "\", not \"1\"");
        }
        System.out.println("Test Passed");
    }
}
