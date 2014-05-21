package com.voicematch.bouger.play;

public class Trilateration {
    private double x1, y1, x2, y2, x3, y3;

    public void initPoints(float pt1x, float pt1y, float pt2x, float pt2y, float pt3x, float pt3y) {
        x1 = pt1x;
        y1 = pt1y;
        x2 = pt2x;
        y2 = pt2y;
        x3 = pt3x;
        y3 = pt3y;
    }
    
    public float getAngle_Deg(int mic1, int mic2, int mic3) {
        float ret = 0;
        
        double r1 = 231.8/Math.sqrt((double)mic1);
        double r2 = 231.8/Math.sqrt((double)mic2);
        double r3 = 231.8/Math.sqrt((double)mic3);

        double D = 0, X = 0, Y = 0;
        double T1 = 0, T2 = 0, T3 = 0, T4 = 0, T5 = 0, T6 = 0;
      
        X = x2 - x1;
        Y = y2 - y1;

        D = Math.sqrt((X * X) + (Y * Y));
        double tmpA = (r1 * r1 - r2 * r2 + D * D);
        double tmpB = (2 * r1 * D);
        T1 = Math.acos(tmpA / tmpB);
//        T1 = Math.acos((r1 * r1 - r2 * r2 + D * D) / (2 * r1 * D));
        T2 = Math.atan(Y / X);

        T3 = x1 + r1 * Math.cos(T2 + T1);
        T4 = y1 + r1 * Math.sin(T2 + T1);

        T5 = x1 + r1 * Math.cos(T2 - T1);
        T6 = y1 + r1 * Math.sin(T2 - T1);

        double pt1_diff = (T3-x3)*(T3-x3)+(T4-y3)*(T4-y3)-r3*r3;
        double pt2_diff = (T5-x3)*(T5-x3)+(T6-y3)*(T6-y3)-r3*r3;

        if( pt1_diff < pt2_diff )
            ret = (float) Math.tan(T3/T4);
        else
            ret = (float) Math.tan(T5/T6);
        
        return ret;
    }
}
