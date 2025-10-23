package widgets;

import core.App;
import core.StringHelper;
import reader.SupportedFileType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class FileItemListItem extends JPanel {
    static final int ICON_HEIGHT = 16;
    static final int ICON_WIDTH = 16;
    static final int HEIGHT = 16;
    static final int WIDTH = 16;

    public FileItemListItem(String fileNameWithExtension) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(new EmptyBorder(1, 2, 1, 2));
        String fileName = StringHelper.getFileName(fileNameWithExtension);
        String extension = StringHelper.getFileExtension(fileNameWithExtension);
        if (extension != null) {
            String iconFileName = "icon_file_type_" + extension + ".png";
            BufferedImage url = App.loadResource(iconFileName);
            JButton button;
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image newImg = icon.getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH);
                button = new JButton(new ImageIcon(newImg));
            } else {
                button = new JButton("Img not found");
            }
            button.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            button.setHorizontalAlignment(JButton.CENTER);
            button.setVerticalAlignment(JButton.CENTER);
            add(button, BorderLayout.WEST);
        }
        add(new JLabel(fileName));

    }
}
