package com.example.asonicofdm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Sync {
    public static sync(OFDMConfig pre_con, OFDMConfig con, double[] rx_sound) {
        int preamble_len = pre_con.symbol_per_carrier * (pre_con.IFFT_length + pre_con.GI) + pre_con.GIP;
        int data_len = con.symbol_per_carrier * (con.IFFT_length * con.GI) + con.GIP;

        double[] rx_sum = get_sum(rx_sound, preamble_len);

        int[] rx_sum_range = new int[rx_sum.length];
        for (int i = 0; i < rx_sum.length; ++i) {
            rx_sum_range[i] = i;
        }

        ArrayList<ArrayList<Integer>> peaks_1 = find_all_peaks(rx_sum_range, rx_sum, 3, preamble_len / 10);

        int[] locs = new int[peaks_1.get(1).size()];
        double[] rx_sum_locs = new double[locs.length];
        for (int i = 0; i < locs.length; ++i) {
            locs[i] = peaks_1.get(1).get(i);
            rx_sum_locs[i] = rx_sum[locs[i]];
        }

        ArrayList<ArrayList<Integer>> peaks_2 = find_all_peaks(locs, rx_sum_locs, 3, 0);

        int target = peaks_2.get(0).get(0) - preamble_len / 2;

        ArrayList<Integer> pre_inds = new ArrayList<>();

        for (int i = Math.max(0, target - preamble_len / 2); i < target + preamble_len / 2; ++i) {
            bits = OFDM_dmod
        }
//
//        double[] rx_sum_locs = new double[peaks_1.get(1).size()];
//        for (int i = 0; i < rx_sum_locs.length; ++i) {
//            rx_sum_locs[i] = rx_sum[peaks_1.get(1).get(i)];
//        }
//        ArrayList<ArrayList<Integer>> peaks_2 = find_all_peaks((Integer[])peaks_1.get(1).toArray(), rx_sum_locs, 3, 0);

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

    private static ArrayList<ArrayList<Integer>> find_all_peaks(int[] x, double[] y, double threshold, double peakdistance) {
        ArrayList<Integer> locs = new ArrayList<>();
        double[] dy = dif(y);
        double[] dy_sign = new double[dy.length];
        for (int i = 0; i < dy_sign.length; ++i) {
            if (dy[i] > 0.0) {
                dy_sign[i] = 1;
            } else if (dy[i] < 0.0) {
                dy_sign[i] = -1;
            } else {
                dy_sign[i] = 0;
            }
        }
        double[] mark_peaks = dif(dy_sign);

        for (int i = 0; i < x.length; ++i) {
            if (y[i] <= threshold) {
                y[i] = Float.NaN;
            }
        }

        int P = 1;

        for (int i = 1; i < x.length; ++i) {
            if (!Double.isNaN(y[i]) && Math.abs(mark_peaks[i - 1] - (-2.0)) < 1e-3) {
                P += 1;
            }
        }

        double Peak = threshold;
        locs.add(0);
        for (int i = 1; i < x.length; ++i) {
            if (!Double.isNaN(y[i]) && y[i] > Peak && Math.abs(mark_peaks[i - 1] - (-2.0)) < 1e-3) {
                Peak = y[i];
                locs.set(0, i);
            }
        }

        int M = (int)(peakdistance / Math.abs(x[2] - x[1]));

        for (int i = 0; i < P; ++i) {
            if (locs.get(i) >= 0) {
                if (locs.get(i) - M >= 0 && locs.get(i) + M < x.length) {
                    for (int j = locs.get(i) - M; j < locs.get(i) + M; ++j) {
                        y[j] = Float.NaN;
                    }
                } else if (locs.get(i) - M < 0 && locs.get(i) + M < x.length) {
                    for (int j = 0; j < locs.get(i) + M; ++j) {
                        y[j] = Float.NaN;
                    }
                } else if (locs.get(i) + M >= x.length && locs.get(i) - M >= 0) {
                    for (int j = locs.get(i) - M; j < x.length; ++j) {
                        y[j] = Float.NaN;
                    }
                } else {
                    for (int j = 0; j < x.length; ++j) {
                        y[j] = Float.NaN;
                    }
                }
            }

            Peak = threshold;
            locs.add(-1);
            for (int j = 1; j < x.length; ++j) {
                if (y[j] > Peak && (mark_peaks[j - 1] - (-2.0) < 1e-3) {
                    Peak = y[j];
                    locs.set(i + 1, j);
                }
            }
        }

        int Q = 0;
        for (int i = 0; i < P; ++i) {
            if (locs.get(i) != -1) {
                Q += 1;
            }
        }

        locs.sort(new MyComparator());
        locs = (ArrayList)locs.subList(0, Q);

        ArrayList<Integer> xpeaks = new ArrayList<>(locs.size());
        for (int i = 0; i < locs.size(); ++i) {
            xpeaks.set(i, x[locs.get(i)]);
        }

        ArrayList<ArrayList<Integer>> res = new ArrayList<>();
        res.add(locs);
        res.add(xpeaks);
        return res;
    }

    private static double[] dif(double[] y) {
        double[] dy = new double[y.length];
        Arrays.fill(dy, 0);
        for (int i = 0; i < dy.length - 1; ++i) {
            dy[i] = y[i + 1] - y[i];
        }
        dy[dy.length - 1] = (y[y.length - 3] + 2 * y[y.length - 2] - 3 * y[y.length - 1]) / 6;
        return dy;
    }
}

class MyComparator implements Comparator<Integer>{
    @Override
    public int compare(Integer o1, Integer o2) {
        if(o1 < o2) {
            return 1;
        }else if(o1 > o2) {
            return -1;
        }else {
            return 0;
        }
    }
}