package widgets.tab_control;

import core.App;
import widgets.FColor;
import widgets.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Tab extends JPanel {
    JTabbedPane tabControl;
    JLabel tabLabel = new JLabel();

    public Tab(JTabbedPane tabControl_, String tabName, boolean bCanBeClosed) {
        tabControl = tabControl_;
        setBackground(new FColor(new Color(255, 255, 255), 0));
        tabLabel.setText(tabName);
        add(tabLabel);

        if (bCanBeClosed) {
            JButton closeButton = App.createButtonWithIcon("icon_delete_text.png", UIConstants.Transparent, 16, 16, 8, 8);
            closeButton.addActionListener(e -> {
                int index = tabControl.indexOfTabComponent(this);
                tabControl.removeTabAt(index);
            });
            add(closeButton);
        }
    }

    public void rename(String tabName) {
        tabLabel.setText(tabName);
    }
}
