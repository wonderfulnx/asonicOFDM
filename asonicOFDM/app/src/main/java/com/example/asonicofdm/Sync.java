package com.example.asonicofdm;

public class Sync {
    public static sync(OFDMConfig pre_con, OFDMConfig con, double[] rx_sound) {
        int preamble_len = pre_con.symbol_per_carrier * (pre_con.IFFT_length + pre_con.GI) + pre_con.GIP;
        int data_len = con.symbol_per_carrier * (con.IFFT_length * con.GI) + con.GIP;

        double[] rx_sum = get_sum(rx_sound, preamble_len);


    }

    private static double[] get_sum(double[] signal, int window_len) {
        double[] rx_sum = new double[signal.length - window_len];
        double tmp = 0;
        for (int i = 0; i < window_len; ++i) {
            tmp += Math.abs(signal[i]);
        }
        for (int i = 0; i < signal.length - window_len; ++i) {
            rx_sum[i] = tmp;
            tmp = tmp - Math.abs(signal[i]);
            tmp = tmp + Math.abs(signal[window_len + i]);
        }
        return rx_sum;
    }
}