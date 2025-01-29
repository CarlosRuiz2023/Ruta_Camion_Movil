package com.itsmarts.SmartRouteTruckApp.modelos;

public class ChooserItem {
    private String icon;
    private String opcion;
    private boolean chooser;

    public ChooserItem() {
    }

    public ChooserItem(String icon, String opcion, boolean chooser) {
        this.icon = icon;
        this.opcion = opcion;
        this.chooser = chooser;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getOpcion() {
        return opcion;
    }

    public void setOpcion(String opcion) {
        this.opcion = opcion;
    }

    public boolean isChooser() {
        return chooser;
    }

    public void setChooser(boolean chooser) {
        this.chooser = chooser;
    }
}
