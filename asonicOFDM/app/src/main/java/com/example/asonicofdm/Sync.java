package com.example.asonicofdm;

public class Sync {
    public static sync(OFDMConfig pre_con, OFDMConfig con, byte[] rx_sound) {
        int preamble_len = pre_con.symbol_per_carrier * (pre_con.IFFT_length + pre_con.GI) + pre_con.GIP;
        int data_len = con.symbol_per_carrier * (con.IFFT_length * con.GI) + con.GIP;

        
    }
}