/*
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
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

/*
  test
  @bug 4784285 4785920
  @summary check whether Print- and Page- dialogs are modal and correct window activated after their closing
  @author son@sparc.spb.su: area=PrinterJob.modality
  @run applet/manual=yesno PrinterDialogsModalityTest.html
*/

/**
 * PrinterDialogsModalityTest.java
 *
 * summary: check whether Print- and Page- dialogs are modal and correct window activated after their closing
 */

import java.applet.Applet;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.TextArea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

public class PrinterDialogsModalityTest extends Applet
{
    //Declare things used in the test, like buttons and labels here

    public void init()
    {
        //Create instructions for the user here, as well as set up
        // the environment -- set the layout manager, add buttons,
        // etc.
        this.setLayout (new BorderLayout ());

        String[] instructions =
        {
            "This is a Windows only test, for other platforms consider it passed",
            "After test start you will see frame titled \"test Frame\"",
            "with two buttons - \"Page Dialog\" and \"Print Dialog\"",
            "1. make the frame active by clicking on title",
            "2. press \"Page Dialog\" button, page dailog should popup",
            "3. make sure page dialog is modal (if not test is failed)",
            "4. close the dialog (either cancel it or press ok)",
            "5. make sure the frame is still active (if not test is failed)",
            "6. press \"Print Dialog\" button, print dialog should popup",
            "7. repeat steps 3.-5.",
            "",
            "If you are able to execute all steps successfully then test is passed, else failed."
        };
        Sysout.createDialogWithInstructions( instructions );

    }//End  init()

    public void start ()
    {
        //Get things going.  Request focus, set size, et cetera
        setSize (200,200);
        setVisible(true);
        validate();

        Button page = new Button("Page Dialog");
        page.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PrinterJob prnJob = PrinterJob.getPrinterJob();
                    prnJob.pageDialog(new PageFormat());
                }
            });
        Button print = new Button("Print Dialog");
        print.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PrinterJob prnJob = PrinterJob.getPrinterJob();
                    prnJob.printDialog();
                }
            });
        Frame frame = new Frame("Test Frame");
        frame.setLayout(new FlowLayout());
        frame.add(page);
        frame.add(print);
        frame.setLocation(200, 200);
        frame.pack();
        frame.setVisible(true);

    }// start()

    //The rest of this class is the actions which perform the test...

    //Use Sysout.println to communicate with the user NOT System.out!!
    //Sysout.println ("Something Happened!");

}// class PrinterDialogsModalityTest

/* Place other classes related to the test after this line */





/****************************************************
 Standard Test Machinery
 DO NOT modify anything below -- it's a standard
  chunk of code whose purpose is to make user
  interaction uniform, and thereby make it simpler
  to read and understand someone else's test.
 ****************************************************/

/**
 This is part of the standard test machinery.
 It creates a dialog (with the instructions), and is the interface
  for sending text messages to the user.
 To print the instructions, send an array of strings to Sysout.createDialog
  WithInstructions method.  Put one line of instructions per array entry.
 To display a message for the tester to see, simply call Sysout.println
  with the string to be displayed.
 This mimics System.out.println but works within the test harness as well
  as standalone.
 */

class Sysout
{
    private static TestDialog dialog;

    public static void createDialogWithInstructions( String[] instructions )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        dialog.printInstructions( instructions );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }

    public static void createDialog( )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        String[] defInstr = { "Instructions will appear here. ", "" } ;
        dialog.printInstructions( defInstr );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }


    public static void printInstructions( String[] instructions )
    {
        dialog.printInstructions( instructions );
    }


    public static void println( String messageIn )
    {
        dialog.displayMessage( messageIn );
    }

}// Sysout  class

/**
  This is part of the standard test machinery.  It provides a place for the
   test instructions to be displayed, and a place for interactive messages
   to the user to be displayed.
  To have the test instructions displayed, see Sysout.
  To have a message to the user be displayed, see Sysout.
  Do not call anything in this dialog directly.
  */
class TestDialog extends Dialog
{

    TextArea instructionsText;
    TextArea messageText;
    int maxStringLength = 80;

    //DO NOT call this directly, go through Sysout
    public TestDialog( Frame frame, String name )
    {
        super( frame, name );
        int scrollBoth = TextArea.SCROLLBARS_BOTH;
        instructionsText = new TextArea( "", 15, maxStringLength, scrollBoth );
        add( "North", instructionsText );

        messageText = new TextArea( "", 5, maxStringLength, scrollBoth );
        add("Center", messageText);

        pack();

        setVisible(true);
    }// TestDialog()

    //DO NOT call this directly, go through Sysout
    public void printInstructions( String[] instructions )
    {
        //Clear out any current instructions
        instructionsText.setText( "" );

        //Go down array of instruction strings

        String printStr, remainingStr;
        for( int i=0; i < instructions.length; i++ )
        {
            //chop up each into pieces maxSringLength long
            remainingStr = instructions[ i ];
            while( remainingStr.length() > 0 )
            {
                //if longer than max then chop off first max chars to print
                if( remainingStr.length() >= maxStringLength )
                {
                    //Try to chop on a word boundary
                    int posOfSpace = remainingStr.
                        lastIndexOf( ' ', maxStringLength - 1 );

                    if( posOfSpace <= 0 ) posOfSpace = maxStringLength - 1;

                    printStr = remainingStr.substring( 0, posOfSpace + 1 );
                    remainingStr = remainingStr.substring( posOfSpace + 1 );
                }
                //else just print
                else
                {
                    printStr = remainingStr;
                    remainingStr = "";
                }

                instructionsText.append( printStr + "\n" );

            }// while

        }// for

    }//printInstructions()

    //DO NOT call this directly, go through Sysout
    public void displayMessage( String messageIn )
    {
        messageText.append( messageIn + "\n" );
        System.out.println(messageIn);
    }

}// TestDialog  class
