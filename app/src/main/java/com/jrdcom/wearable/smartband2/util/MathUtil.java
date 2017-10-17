package com.jrdcom.wearable.smartband2.util;

public class MathUtil {

	public static double getMax(double[] values){
		final int size = values.length;
		if(size <= 1){
			return -1;
		}
		double rtn = values[0];
		for (int i = 1; i < size; i++) {
			if(rtn < values[i]){
				rtn = values[i];
			}
		}
		return rtn;
	}
	
	public static  double convert(double num){
		return Math.round(num*10)/10.0;
	}

    public static  double convert(double num,int digit){
        double p = Math.pow(10,digit);
        return Math.round(num*p)/p;
    }

    public static double roundNumber(double num, int digit) {
        int p = (int) Math.pow(10, digit);
        double v = ((int) (num * p));
        v /= p;
        if (num != 0 && v <= 0.0) {
            v = Math.pow(0.1, digit);
        }
        return v;
    }

}
