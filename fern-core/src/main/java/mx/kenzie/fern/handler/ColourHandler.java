package mx.kenzie.fern.handler;

import java.awt.*;

public class ColourHandler implements ValueHandler<Color> {
    @Override
    public boolean matches(String string) {
        return string.matches("#(?:[a-fA-F0-9]{6}|[a-fA-F0-9]{3})");
    }
    
    @Override
    public Color parse(String string) {
        return Color.decode(string);
    }
}
