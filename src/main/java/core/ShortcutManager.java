package core;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

public class ShortcutManager { // https://stackoverflow.com/questions/22741215/how-to-use-key-bindings-instead-of-key-listeners
    HashSet<Integer> pressedKeys = new HashSet<Integer>();

    static final String  ActionF = "F";
    static final String  ActionCtrl = "ctrl";
    static final String  ActionAlt = "alt";

    public void init(JComponent component) {
        int type = JComponent.WHEN_IN_FOCUSED_WINDOW;

        component.getInputMap(type).put(KeyStroke.getKeyStroke("F"), ActionF);
        component.getInputMap(type).put(KeyStroke.getKeyStroke((char)KeyEvent.VK_CONTROL), ActionCtrl);
        component.getInputMap(type).put(KeyStroke.getKeyStroke("alt"), ActionAlt);


        component.getActionMap().put(ActionF, new ActionF());
        component.getActionMap().put(ActionCtrl, new ActionCtrl());
        component.getActionMap().put(ActionAlt, new ActionAlt());

    }

    private void checkCombination() {

    }

    public class ActionF extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("pressed F");
        }
    }

    public class ActionCtrl extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("pressed Ctrl");
        }
    }
    public class ActionAlt extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("pressed Alt");
        }
    }
}
