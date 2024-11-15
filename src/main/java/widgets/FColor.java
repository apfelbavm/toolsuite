package widgets;

import java.awt.*;

public class FColor extends Color {
    public FColor(Color color, int alpha)
    {
        super(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
