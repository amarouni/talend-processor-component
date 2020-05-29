package org.talend.datastreams.components;

public class Records {
    public String getCol0() {
        return col0;
    }

    private final String col0;

    public int getCol1() {
        return col1;
    }

    private final int col1;

    public Records(String col0, int col1) {
        this.col0 = col0;
        this.col1 = col1;
    }
}
