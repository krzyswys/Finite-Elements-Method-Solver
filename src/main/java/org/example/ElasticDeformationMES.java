package org.example;

import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.linear.*;

public class ElasticDeformationMES {
    public double[] xi;
    public double[] yi;
    public static double domain;

    public ElasticDeformationMES(double dz) {
        domain = dz;
    }

    public double getIntegral(int i, int j, int n) {

        double integral2 =  new IterativeLegendreGaussIntegrator( //FIXME: fix gaussian integral
                30,
                1e-6,
                1e-6).integrate(
                Integer.MAX_VALUE,
                x -> E(x) * e_i_dx(i, x, n) * e_i_dx(j, x, n),
                0,
                2);



//        double p1=(1.0 / Math.sqrt(3.0));
//        double p2 =(-1.0 / Math.sqrt(3.0));
//        double l = domain / n;
//        double middle = l * i;
//        double left_edge = l * (i - 1)+1;
//        double right_edge = l * (i + 1)+1;

//        double o = (p1) * ((middle-left_edge) / 2.0) + (middle+left_edge / 2.0);
//        double s = (p2) * ((middle-left_edge) / 2.0) + (middle+left_edge / 2.0);
//        double integral_01 = ((middle-left_edge) / 2.0) * (E(o) * e_i_dx(i, o, n) * e_i_dx(j, o, n) + E(s) * e_i_dx(i, s, n) * e_i_dx(j, s, n));
//
//        double oo = (p1) * ((right_edge-middle) / 2.0) + ((right_edge+middle) / 2.0);
//        double ss = (p2) *((right_edge-middle) / 2.0) + ((right_edge+middle) / 2.0);
//        double integral_12 = ((right_edge-middle)) * (E(oo) * e_i_dx(i, oo, n) * e_i_dx(j, oo, n) + E(ss) * e_i_dx(i, ss, n) * e_i_dx(j, ss, n));



//        double o = (1.0 / Math.sqrt(3.0)) * (1.0 / 2.0) + (1.0 / 2.0);
//        double s = (-1.0 / Math.sqrt(3.0)) * (1.0 / 2.0) + (1.0 / 2.0);
//        double integral_01 = (1.0 / 2.0) * (E(o) * e_i_dx(i, o, n) * e_i_dx(j, o, n) + E(s) * e_i_dx(i, s, n) * e_i_dx(j, s, n));
//
//        double oo = (1.0 / Math.sqrt(3.0)) * (1.0 / 2.0) + (3.0 / 2.0);
//        double ss = (-1.0 / Math.sqrt(3.0)) * (1.0 / 2.0) + (3.0 / 2.0);
//        double integral_12 = (1.0 / 2.0) * (E(oo) * e_i_dx(i, oo, n) * e_i_dx(j, oo, n) + E(ss) * e_i_dx(i, ss, n) * e_i_dx(j, ss, n));
//
//        double integral = integral_01 + integral_12;
//
//
//        System.out.println("c1 " + integral2);
//        System.out.println("c2 " + integral + " " + integral_01 + " " + integral_12);

        return integral2;
    }

    public void calculate(int n) {
        RealMatrix B_uv = new Array2DRowRealMatrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                B_uv.setEntry(i, j, B_ei_ej_x(i, j, 0.0, n));
            }
        }

        RealVector L_v = new ArrayRealVector(n);
        for (int i = 0; i < n; i++) {
            L_v.setEntry(i, L_v_x(i, 0.0, n));
        }
        RealVector w = new QRDecomposition(B_uv).getSolver().solve(L_v);
        double[] ws = w.toArray();

        double acc = 0.1;
        yi = new double[(int) (domain / acc) + 2];
        xi = new double[(int) (domain / acc) + 2];
        double x = 0.0;
        int y = 0;
        while (x <= domain) {
            for (int i = 0; i < n; i++) {
                yi[y] += e_i(i, x, n) * ws[i];
            }
            xi[y] = x;
            y++;
            x += acc;
        }
    }

    private double L_v_x(int i, double x, int n) {
        return -10 * E(x) * e_i(i, x, n);
    }

    private double B_ei_ej_x(int i, int j, double x, int n) {
        double integral = 0.0;
        if (Math.abs(j - i) <= 1) {
            integral = getIntegral(i, j, n);
        }
        return integral - E(x) * e_i(i, x, n) * e_i(j, x, n);
    }

    private static double E(double x) {
        if (x >= 0 && x <= 1.0) {
            return 3.0;
        } else if (x >= 1 && x <= 2) {
            return 5.0;
        } else throw new IllegalArgumentException("out of domain, x= " + x);
    }

    private static double e_i(int i, double x, int n) {
        double l = domain / n;
        double middle = l * i;
        double left_edge = l * (i - 1);
        double right_edge = l * (i + 1);
        if (x < left_edge || x > right_edge) {
            return new LinearFunction().getValue(x);
        }
        if (x >= middle) {
            return new LinearFunction(1, 0, middle, right_edge).getValue(x);
        } else {
            return new LinearFunction(0, 1, left_edge, middle).getValue(x);
        }
    }

    private static double e_i_dx(int i, double x, int n) {
        double l = domain / n;
        double middle = l * i;
        double left_edge = l * (i - 1);
        double right_edge = l * (i + 1);
        if (x < left_edge || x > right_edge) {
            return new LinearFunction().getDerivative();
        }
        if (x >= middle) {
            return new LinearFunction(1, 0, middle, right_edge).getDerivative();
        } else {
            return new LinearFunction(0, 1, left_edge, middle).getDerivative();
        }
    }
}
