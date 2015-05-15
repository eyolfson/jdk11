/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/* @test
   @bug 6921687 8079428
   @summary Mnemonic disappears after repeated attempts to open menu items using
   mnemonics
   @author Semyon Sadetsky
   @library /lib/testlibrary
   @build jdk.testlibrary.OSInfo
   @run main bug6921687
  */


import jdk.testlibrary.OSInfo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;


public class bug6921687 {

    private static Class lafClass;
    private static JFrame frame;

    public static void main(String[] args) throws Exception {
        if (OSInfo.getOSType() != OSInfo.OSType.WINDOWS) {
            System.out.println("Only Windows platform test. Test is skipped.");
            System.out.println("ok");
            return;
        }
        lafClass = Class.forName(UIManager.getSystemLookAndFeelClassName());
        UIManager.setLookAndFeel((LookAndFeel) lafClass.newInstance());
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    frame = new JFrame();
                    frame.setUndecorated(true);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    setup(frame);
                }
            });

            final Robot robot = new Robot();
            robot.setAutoDelay(20);
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_F);
            robot.keyRelease(KeyEvent.VK_F);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.waitForIdle();
            checkMnemonics();

            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_S);
            robot.keyRelease(KeyEvent.VK_S);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.waitForIdle();
            checkMnemonics();
            System.out.println("ok");
        } finally {
            frame.dispose();
        }

    }

    private static void checkMnemonics() throws Exception {
        if ((Boolean) lafClass.getMethod("isMnemonicHidden").invoke(lafClass)) {
            throw new RuntimeException("Mnemonics are hidden");
        }
    }

    private static void setup(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // First Menu, F - Mnemonic
        JMenu firstMenu = new JMenu("First Menu");
        firstMenu.setMnemonic(KeyEvent.VK_F);
        firstMenu.add(new JMenuItem("One", KeyEvent.VK_O));
        firstMenu.add(new JMenuItem("Two", KeyEvent.VK_T));
        menuBar.add(firstMenu);

        // Second Menu, S - Mnemonic
        JMenu secondMenu = new JMenu("Second Menu");
        secondMenu.setMnemonic(KeyEvent.VK_S);
        secondMenu.add(new JMenuItem("A Menu Item", KeyEvent.VK_A));
        menuBar.add(secondMenu);

        frame.setSize(350, 250);
        frame.setVisible(true);

    }
}
