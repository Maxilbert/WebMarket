package com.example.yuan.classes;

/**
 * 参见电子计算机算法手册，Page 115
 * 快速傅里叶变换算法(2)
 *
 * Can be used for doing FFT of n = 2 ^ m numbers (real or complex)
 *
 * @author
 */

public class FastFourier  {

    //反序运算
    private static int bitRev(int j, int m) {
        int i, j1 = j, k = 0, j2 = j;
        for (i = 0; i < m; i++) {
            j1 >>= 1;
            k <<= 1;
            k += (j1 - j2);
            j1 = j2;
        }
        return k;
    }

    //反序运算对应的Sin,Cos表
    private static float[][] calcuW(int m) {
        float pi2 = 6.2831853f;
        int m1 = m - 1, n = 1 << m;
        float[] cos = new float[n];
        float[] sin = new float[n];
        for (int k = 0; k < n; k++) {
            int p = bitRev(k / (2 << m1), m);
            float arg = pi2 * p / n;
            cos[k] = (float) Math.cos(arg);
            sin[k] = (float) Math.sin(arg);
        }
        float[][]W = new float [2][n];
        W[0] = cos;
        W[1] = sin;
        return W;
    }

    //快速傅里叶变换算法
    //输入：
    //    IsForword =true正变换；=false反变换
    //    a 实部
    //    b 虚部
    //输出：
    //    a 实部
    //    b 虚部
    public static void FFT(boolean IsForword, float[] a, float[] b) {
        if ((a == null) || (b == null)) {
            return; //throw Exception("变换数组不能为空。");
        }
        float[][] W; float[] cos, sin;
        int i, k = 0, l, n = a.length, n2 = n >> 1, m = (int) log(n, 2);
        if (n != (int) Math.pow(2, m)) {
            return;//throw new Exception("变换数组应该满足2的整数幂关系。");
        }
        float re, im;
        W = calcuW(m);
        cos = W[0]; sin = W[1];
        for (l = 0; l < m; l++) {
            while (k < n) {
                for (i = 0; i < n2; i++) {
                    int ii = k + n2;
                    re = a[ii] * cos[k] + b[ii] * sin[k];
                    im = b[ii] * cos[k] - a[ii] * sin[k];
                    a[ii] = a[k] - re;
                    b[ii] = b[k] - im;
                    a[k] += re;
                    b[k] += im;
                    k++;
                }
                k += n2;
            }
            k = 0;
            n2 >>= 1;
        }
        for (k = 0; k < n; k++) {
            i = bitRev(k, m);
            if (i > k) {
                re = a[k];
                a[k] = a[i];
                a[i] = re;
                im = b[k];
                b[k] = b[i];
                b[i] = im;
            }
            if (!IsForword) {//反变换时使用
                a[k] /= n;
                b[k] /= n;
            }
        }
    }

//    //实数快速傅里叶变换算法
//    //输入:
//    //X[]
//    //输出:
//    //X[] 实部
//    //return 虚部
//    public static float[] RFFT(boolean IsForward, float[] x) {
//        int n = x.length / 2;
//        float[] a = new float[n];
//        float[] b = new float[n];
//        for (int i = 0; i < n; i++) {
//            a[i] = x[2 * i];
//            b[i] = x[2 * i + 1];
//        }
//        if (IsForward) {
//            FFT(IsForward, a, b);
//        }
//        float sd = (float) Math.sin(Math.PI / n);
//        float cd = (float) Math.cos(Math.PI / n);
//        float cn = 1.0f, sn = 0.0f;
//        float aa, ab, ba, bb, re, r, im;
//        for (int j = 0; j < n; j++) {
//            int k = n - j;
//            if (k == n) {
//                //continue;
//                k = 0;
//            }
//            aa = (a[j] + a[k]) / 2;
//            ab = (a[j] - a[k]) / 2;
//            ba = (b[j] + b[k]) / 2;
//            bb = (b[j] - b[k]) / 2;
//            re = aa + cn * ba;
//            r = sn * ab;
//            a[j] = re - r;
//            a[k] = re + r;
//            im = bb - cn * ab;
//            r = - sn * ba;
//            b[j] = r + im;
//            b[k] = r - im;
//            r = cd * cn - sn * sd;
//            sn = cn * sd + sn * cd;
//            cn = r;
//        }
//        if (!IsForward) {
//            FFT(IsForward, a, b);
//        }
//        for (int i = 0; i < n; i++) {
//            x[i] = a[i];
//            x[n + i] = b[i];
//        }
//    }

    //二维实数快速傅里叶变换算法
    public static void RFFT2(boolean IsForward, float[][] mat) {
        float[] vct1, vct2;
        for (int c = 0; c < mat[0].length; c += 2) {
            vct1 = getVector(true, c, mat);
            vct2 = getVector(true, c + 1, mat);
            FFT(IsForward, vct1, vct2);
            setVector(true, c, vct1, mat);
            setVector(true, c + 1, vct2, mat);
        }
        for (int r = 0; r < mat.length; r += 2) {
            vct1 = getVector(false, r, mat);
            vct2 = getVector(false, r + 1, mat);
            FFT(IsForward, vct1, vct2);
            setVector(false, r, vct1, mat);
            setVector(false, r + 1, vct2, mat);
        }
    }

    //获取矩阵mat中行(isRow)或列向量
    private static float[] getVector(boolean isRow, int idx, float[][] mat) {
        int numV = (isRow) ? mat.length : mat[0].length;
        float[] vct = new float[numV];
        for (int i = 0; i < numV; i++) {
            vct[i] = (isRow) ? mat[i][idx] : mat[idx][i];
        }
        return vct;
    }

    //设置矩阵mat中行(isRow)或列向量
    private static void setVector(boolean isRow, int idx, float[] vct, float[][] mat) {
        int numV = (isRow) ? mat.length : mat[0].length;
        for (int i = 0; i < numV; i++) {
            if (isRow) {
                mat[i][idx] = vct[i];
            } else {
                mat[idx][i] = vct[i];
            }
        }
    }

    static public double log(double value, double base) {
        return Math.log(value) / Math.log(base);
    }
}