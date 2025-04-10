package widgets.tab_control;

import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import widgets.FColor;
import widgets.UIConstants;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.geom.Path2D;

/*https://github.com/JFormDesigner/FlatLaf/issues/541*/

public class TabbedPaneUI extends FlatTabbedPaneUI {

    static final int CORNER_RADIUS = 12;
    static final int SELECTED_PADDING = 2;
//    public TabbedPaneUI() {
//        focusColor = UIConstants.Goblin;
//        selectedBackground = UIConstants.Goblin;
//        hoverColor = UIConstants.Goblin;
//    }

    public static ComponentUI createUI(JComponent c) {
        TabbedPaneUI pane = new TabbedPaneUI();
        pane.hoverColor = UIConstants.BitterSweet;
        return pane;
    }

    @Override
    protected void paintCardTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h) {
        float lineWidth = UIScale.scale(1f);
        float arc = UIScale.scale(CORNER_RADIUS);


        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
//        path.append(FlatUIUtils.createRoundRectanglePath(x, y, w, h, arc, arc, 0, 0), false);
//        path.append(FlatUIUtils.createRoundRectanglePath(x + lineWidth, y + lineWidth, w - (lineWidth * 2), h - lineWidth,
//                arc - lineWidth, arc - lineWidth, 0, 0), false);

        path.append(FlatUIUtils.createRoundRectangle(x, y, w, h, 100, arc, arc, 0, 0), false);

        Color color = (Color) UIManager.get("Table.background");
        g.setColor(color);
//        g.setColor(UIConstants.Red);
        ((Graphics2D) g).fill(path);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Color col = getTabBackground(tabPlacement, tabIndex, isSelected);

        float arc = UIScale.scale(CORNER_RADIUS);
        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        path.append(FlatUIUtils.createRoundRectangle(x + SELECTED_PADDING / 2, y + SELECTED_PADDING / 2, w - SELECTED_PADDING, h - SELECTED_PADDING, 100, arc, arc, arc, arc), false);
        g.setColor(col);
        ((Graphics2D) g).fill(path);
    }
}
