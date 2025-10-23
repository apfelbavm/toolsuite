package widgets;

import javax.swing.*;
import java.awt.*;

public class FileList<E extends Object> extends JList<E> {
    @Override
    public ListCellRenderer getCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object object, int index, boolean selected, boolean focused) {
                FileItemListItem item = new FileItemListItem(object + ""); // what a stupid hack to conver to string
                return item;
//                return super.getListCellRendererComponent(list, object, index, selected, focused);
            }
        };
    }
}
