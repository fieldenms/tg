package ua.com.fielden.platform.pmodels;

import java.awt.Color;
import java.awt.Stroke;

public interface IPaintable {

    void fill(Color color);

    void stroke(Color color);

    void stroke(Stroke stroke);
}
