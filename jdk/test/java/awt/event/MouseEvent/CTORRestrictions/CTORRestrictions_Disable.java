/*
  test %I% %E%
  @bug 6315717
  @summary verifies that MouseEvent could be constructed correctly for mouse extra buttons in regard to sun.awt.enableExtraMouseButtons property
  @author Andrei Dmitriev : area=awt.event
  @run main/othervm -Dsun.awt.enableExtraMouseButtons=false CTORRestrictions_Disable
 */

/*
 * verify that user can't create the MouseEvent? with button4|5|... when property "sun.awt.enableExtraMouseButtons"=false
 * verify that user can create the MouseEvent? with button1|2|3 when property "sun.awt.enableExtraMouseButtons"=false
 */

import java.awt.*;
import java.awt.event.*;

public class CTORRestrictions_Disable {
    static Frame frame = new Frame("MouseEvent Test Frame");
    static Point mousePosition;
    static Point mousePositionOnScreen;

    public static void main(String []s){
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException("Test Failed", ex);
        }
        frame.setSize (200,200);
        frame.setLocation (300, 400);
        frame.setVisible(true);
        robot.delay(1000);
        System.out.println(Toolkit.getDefaultToolkit().getDesktopProperty("sun.awt.enableExtraMouseButtons"));
        mousePosition = new Point(100, 100);
        mousePositionOnScreen = new  Point(frame.getLocationOnScreen().x + mousePosition.x,
                                                 frame.getLocationOnScreen().y + mousePosition.y);

        System.out.println("Stage 1");
        for (int buttonId = 1; buttonId <= MouseInfo.getNumberOfButtons(); buttonId++){
           try {
               postMouseEventNewCtor(buttonId);
               if (buttonId > 3) {
                   throw new RuntimeException("Stage 1 FAILED: MouseEvent CTOR accepted the extra button " + (buttonId+1) + " instead of throwing an exception.");
               }
           } catch (IllegalArgumentException e){
                if (buttonId > 3) {
                    System.out.println("Passed: an exception caught for extra button.");
                } else {
                    throw new RuntimeException("Stage 1 FAILED : exception happen on standard button.", e);
                }
            }
        }

        System.out.println("Stage 2");
        for (int buttonId = 1; buttonId <= MouseInfo.getNumberOfButtons(); buttonId++){
           try {
               postMouseEventOldCtor(buttonId);
               if (buttonId > 3) {
                   throw new RuntimeException("Stage 2 FAILED: MouseEvent CTOR accepted the extra button " + (buttonId+1) + " instead of throwing an exception.");
               }
           } catch (IllegalArgumentException e){
                if (buttonId > 3) {
                    System.out.println("Passed: an exception caught for extra button.");
                } else {
                    throw new RuntimeException("Stage 2 FAILED : exception happen on standard button.", e);
                }
            }
        }
        System.out.println("Test passed.");
    }

    public static void postMouseEventNewCtor(int buttonId)    {
        MouseEvent me = new MouseEvent(frame,
                                       MouseEvent.MOUSE_PRESSED,
                                       System.currentTimeMillis(),
                                       MouseEvent.BUTTON1_DOWN_MASK,
                                       mousePosition.x, mousePosition.y,
                                       mousePositionOnScreen.x,
                                       mousePositionOnScreen.y,
                                       1,
                                       false,              //popupTrigger
                                       buttonId            //button
                                       );
        frame.dispatchEvent( ( AWTEvent )me );
    }

    public static void postMouseEventOldCtor(int buttonId)    {
        MouseEvent meOld = new MouseEvent(frame,
                                          MouseEvent.MOUSE_PRESSED,
                                          System.currentTimeMillis(),
                                          MouseEvent.BUTTON1_DOWN_MASK,
                                          mousePosition.x, mousePosition.y,
                                          1,
                                          false,              //popupTrigger
                                          buttonId //button
                                          );
        frame.dispatchEvent( ( AWTEvent )meOld );
    }
}


