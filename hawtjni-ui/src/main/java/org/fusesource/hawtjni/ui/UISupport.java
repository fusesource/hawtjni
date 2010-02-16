package org.fusesource.hawtjni.ui;

import net.miginfocom.layout.CC;
import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Widget;

public class UISupport {
    
    public static Color color(Widget widget, int color) {
        return widget.getDisplay().getSystemColor(color);
    }

    public static Composite composite(Composite parent) {
        return new Composite(parent, SWT.NONE);
    }

    static public <T extends Composite> T fill(T composite) {
        composite.setLayout(new FillLayout());
        return composite;
    }
    static public FillLayout fill() {
        return new FillLayout();
    }

    static public MigLayout mig(String insets, String cols, String rows) {
        return new MigLayout(insets, cols, rows);
    }
    static public MigLayout mig(String cols, String rows) {
        return new MigLayout("", cols, rows);
    }
    public static <T extends Control> T mig(T control, String constraints) {
        control.setLayoutData(constraints);
        return control;
    }
    public static <T extends Control> T mig(T control, CC constraints) {
        control.setLayoutData(constraints);
        return control;
    }

    static public Composite[] splitHorizontal(final Composite parent) {

        final Composite panel1 = new Composite(parent, 0);
        final Sash sash = new Sash(parent, SWT.VERTICAL);
        final Composite panel2 = new Composite(parent, 0);

        final FormLayout form = new FormLayout();
        parent.setLayout(form);

        FormData panel1Data = new FormData();
        panel1Data.left = new FormAttachment(0, 0);
        panel1Data.right = new FormAttachment(sash, 0);
        panel1Data.top = new FormAttachment(0, 0);
        panel1Data.bottom = new FormAttachment(100, 0);
        panel1.setLayoutData(panel1Data);

        final int percent = 20;
        final FormData sashData = new FormData();
        sashData.left = new FormAttachment(percent, 0);
        sashData.top = new FormAttachment(0, 0);
        sashData.bottom = new FormAttachment(100, 0);
        sashData.width = 1;
        sash.setLayoutData(sashData);
        sash.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        sash.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                setHorizontalSashPosition(parent, e.x);
            }
        });

        FormData panel2Data = new FormData();
        panel2Data.left = new FormAttachment(sash, 0);
        panel2Data.right = new FormAttachment(100, 0);
        panel2Data.top = new FormAttachment(0, 0);
        panel2Data.bottom = new FormAttachment(100, 0);
        panel2.setLayoutData(panel2Data);
        setHorizontalSashPosition(parent, 0);
        
        return new Composite[] { panel1, panel2 };
    }
    
    static public void setHorizontalSashPosition(Composite parent) {
        setHorizontalSashPosition(parent, -1);
    }
    
    static public void setHorizontalSashPosition(Composite parent, int pos) {
        Control[] children = parent.getChildren();
        Control left = children[0]; 
        Control sash = children[1]; 
        Control right = children[2]; 
        Rectangle sashRect = sash.getBounds();
        if( pos < 0 ) {
            pos = sashRect.x;
        }
        Rectangle parentRect = sash.getParent().getClientArea();
        Point preferredSize1 = left.computeSize(SWT.DEFAULT, parentRect.height, true);
        Point preferredSize2 = right.computeSize(SWT.DEFAULT, parentRect.height);
        int min = preferredSize1.x;
        int max = parentRect.width - preferredSize2.x;
        int x = Math.max(Math.min(pos, max), min);
        if (x == sashRect.x) {
            ((FormData)sash.getLayoutData()).left = new FormAttachment(0, x);
            sash.getParent().layout();
        }
    }

}
