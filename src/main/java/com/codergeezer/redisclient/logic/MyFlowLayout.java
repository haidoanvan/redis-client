package com.codergeezer.redisclient.logic;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * @author haidv
 * @version 1.0
 */
public class MyFlowLayout extends FlowLayout {

  public static final int TOP = 0;

  public static final int MIDDLE = 1;

  public static final int BOTTOM = 2;

  private static final long serialVersionUID = 1L;

  int hgap;

  int vgap;

  boolean hfill;

  boolean vfill;

  public MyFlowLayout() {
    this(TOP, 5, 5, true, false);
  }

  public MyFlowLayout(boolean hfill, boolean vfill) {
    this(TOP, 5, 5, hfill, vfill);
  }

  public MyFlowLayout(int align) {
    this(align, 5, 5, true, false);
  }

  public MyFlowLayout(int align, boolean hfill, boolean vfill) {
    this(align, 5, 5, hfill, vfill);
  }

  public MyFlowLayout(int align, int hgap, int vgap, boolean hfill, boolean vfill) {
    setAlignment(align);
    this.hgap = hgap;
    this.vgap = vgap;
    this.hfill = hfill;
    this.vfill = vfill;
  }

  @Override
  public Dimension preferredLayoutSize(Container target) {
    Dimension tarsiz = new Dimension(0, 0);

    for (int i = 0; i < target.getComponentCount(); i++) {
      Component m = target.getComponent(i);

      if (m.isVisible()) {
        Dimension d = m.getPreferredSize();
        tarsiz.width = Math.max(tarsiz.width, d.width);

        if (i > 0) {
          tarsiz.height += hgap;
        }

        tarsiz.height += d.height;
      }
    }

    Insets insets = target.getInsets();
    tarsiz.width += insets.left + insets.right + hgap * 2;
    tarsiz.height += insets.top + insets.bottom + vgap * 2;

    return tarsiz;
  }

  @Override
  public Dimension minimumLayoutSize(Container target) {
    Dimension tarsiz = new Dimension(0, 0);

    for (int i = 0; i < target.getComponentCount(); i++) {
      Component m = target.getComponent(i);

      if (m.isVisible()) {
        Dimension d = m.getMinimumSize();
        tarsiz.width = Math.max(tarsiz.width, d.width);

        if (i > 0) {
          tarsiz.height += vgap;
        }

        tarsiz.height += d.height;
      }
    }

    Insets insets = target.getInsets();
    tarsiz.width += insets.left + insets.right + hgap * 2;
    tarsiz.height += insets.top + insets.bottom + vgap * 2;

    return tarsiz;
  }

  public boolean getVerticalFill() {
    return vfill;
  }

  public void setVerticalFill(boolean vfill) {
    this.vfill = vfill;
  }

  public boolean getHorizontalFill() {
    return hfill;
  }

  public void setHorizontalFill(boolean hfill) {
    this.hfill = hfill;
  }

  private void placethem(
      Container target, int x, int y, int width, int height, int first, int last) {
    int align = getAlignment();

    if (align == MIDDLE) {
      y += height / 2;
    }

    if (align == BOTTOM) {
      y += height;
    }

    for (int i = first; i < last; i++) {
      Component m = target.getComponent(i);
      Dimension md = m.getSize();

      if (m.isVisible()) {
        int px = x + (width - md.width) / 2;
        m.setLocation(px, y);
        y += vgap + md.height;
      }
    }
  }

  @Override
  public void layoutContainer(Container target) {
    Insets insets = target.getInsets();
    int maxheight = target.getSize().height - (insets.top + insets.bottom + vgap * 2);
    int maxwidth = target.getSize().width - (insets.left + insets.right + hgap * 2);
    int numcomp = target.getComponentCount();
    int x = insets.left + hgap, y = 0;
    int colw = 0, start = 0;

    for (int i = 0; i < numcomp; i++) {
      Component m = target.getComponent(i);

      if (m.isVisible()) {
        Dimension d = m.getPreferredSize();

        if ((this.vfill) && (i == (numcomp - 1))) {
          d.height = Math.max((maxheight - y), m.getPreferredSize().height);
        }

        if (this.hfill) {
          m.setSize(maxwidth, d.height);
          d.width = maxwidth;
        } else {
          m.setSize(d.width, d.height);
        }

        if (y + d.height > maxheight) {
          placethem(target, x, insets.top + vgap, colw, maxheight - y, start, i);
          y = d.height;
          x += hgap + colw;
          colw = d.width;
          start = i;
        } else {
          if (y > 0) {
            y += vgap;
          }

          y += d.height;
          colw = Math.max(colw, d.width);
        }
      }
    }

    placethem(target, x, insets.top + vgap, colw, maxheight - y, start, numcomp);
  }
}
