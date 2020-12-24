import javafx.util.Pair;

import java.util.ArrayList;

/**
 * @author： zll-hust
 * @date： 2020/12/23 10:56
 * @description： 算法部分，包括单纯形法，对偶单纯形法，割平面法。
 */
public class CuttingPlaneAlgorithm {
    private Data data;
    private Pair p; // 每次单纯形法中入基、出基的变量

    public CuttingPlaneAlgorithm(Data data) {
        this.data = data;
    }

    // 割平面法
    public void gomoriAlgorithm() {
        simplexAlgorithm();
        while (!checkIsOver()) {
            int y = findMaxFractionalPart();
            addRestriction(y);
            dualSimplexAlgorithm();
        }
    }

    private void initialSimplex() {
        data.xB = new ArrayList<>();
        data.theta = new ArrayList<>();
        for (int i = 0; i < data.constraintNr; i++) {
            data.xB.add(-1);
            data.theta.add(-1.0);
        }
        data.o = new ArrayList<>();
        data.dualTheta = new ArrayList<>();
        for (int i = 0; i < data.variableNr; i++) {
            data.o.add(0.0);
            data.dualTheta.add(0.0);
        }

        // 找基变量
        for (int i = 0; i < data.constraintNr; i++) {
            for (int j = 0; j < data.variableNr; j++) {
                boolean isBase = true;
                if (data.A.get(i).get(j) != 1)
                    isBase = false;
                for (int k = 0; k < data.constraintNr; k++) {
                    if (i != k && data.A.get(k).get(j) != 0)
                        isBase = false;
                }
                if (isBase)
                    data.xB.set(i, j);
            }
        }
    }

    private boolean pivot() {
        // 计算检验数, 并记录最大检验数的下标
        double maxOj = Double.NEGATIVE_INFINITY;
        int x = -1;
        for (int i = 0; i < data.variableNr; i++) {
            if (data.xB.contains(i)) {
                data.o.set(i, 0.0);
                continue;
            }
            double oj = data.c.get(i);
            for (int j = 0; j < data.constraintNr; j++) {
                oj -= data.c.get(data.xB.get(j)) * data.A.get(j).get(i);
            }
            data.o.set(i, oj);
            if (oj > maxOj) {
                maxOj = oj;
                x = i;
            }
        }

        // 所有检验数小于等于0, 则单纯形法已达到最优解
        if (maxOj < 0) {
            printSimplexTable();
            data.solution = 0;
            return true;
        } else if (maxOj == 0) {
            printSimplexTable();
            data.solution = 3;
            return true;
        }


        // 计算theta, 并记录最小theta的下标
        double minTheta = Double.POSITIVE_INFINITY;
        int y = -1;
        for (int i = 0; i < data.constraintNr; i++) {
            double theta;
            if (data.A.get(i).get(x) == 0)
                theta = Double.POSITIVE_INFINITY;
            else
                theta = data.b.get(i) / data.A.get(i).get(x);
            data.theta.set(i, theta);
            if (theta < minTheta && theta >= 0 && data.A.get(i).get(x) > 0) {
                minTheta = theta;
                y = i;
            }
        }

        if (minTheta == Double.POSITIVE_INFINITY) {
            printSimplexTable();
            data.solution = 2;
            return true;
        }

        p = new Pair(x, y);
        data.solution = 1;
        return false;
    }

    private void Gaussian() {
        int x = (Integer) p.getKey();
        int y = (Integer) p.getValue();
        // 行归一化
        double norm = data.A.get(y).get(x);
        for (int i = 0; i < data.variableNr; i++) {
            data.A.get(y).set(i, data.A.get(y).get(i) / norm);
        }
        data.b.set(y, data.b.get(y) / norm);

        // 其他行置0
        for (int i = 0; i < data.constraintNr; i++) {
            if (i == y) continue;
            double multi = data.A.get(i).get(x);
            for (int j = 0; j < data.variableNr; j++) {
                data.A.get(i).set(j, data.A.get(i).get(j) - multi * data.A.get(y).get(j));
            }
            data.b.set(i, data.b.get(i) - multi * data.b.get(y));
        }

        // 出基
        data.xB.set(y, x);
    }

    // 单纯形法
    private void simplexAlgorithm() {
        initialSimplex();

        while (!pivot()) {
            printSimplexTable();
            Gaussian();
        }
    }

    // 判断是否都是整数
    private boolean checkIsOver() {
        for (int i = 0; i < data.constraintNr; i++) {
            if (!data.isInteger.get(data.xB.get(i))) continue;
            double Bb = data.b.get(i);
            // 判断Bb是否为整数 0.0001防止机器误差
            if (Math.ceil(Bb) - Bb > 0.0001 && Bb - Math.floor(Bb) > 0.0001) {
                return false;
            }
        }
        return true;
    }

    private void addRestriction(int y) {
        ArrayList<Double> nRestriction = new ArrayList<>();
        double nb = -(data.b.get(y) - Math.floor(data.b.get(y)));
        for (int i = 0; i < data.variableNr; i++) {
            nRestriction.add(-(data.A.get(y).get(i) - Math.floor(data.A.get(y).get(i))));
        }
        nRestriction.add(1.0);
        for (int i = 0; i < data.constraintNr; i++) {
            data.A.get(i).add(0.0);
        }
        data.variableNr++;
        data.constraintNr++;
        data.A.add(nRestriction);
        data.b.add(nb);
        data.c.add(0.0);
        data.o.add(0.0);
        data.xB.add(data.variableNr - 1);
        data.dualTheta.add(0.0);
        data.theta.add(null);
        data.isInteger.add(false);
    }

    // 找小数部分最大的非整数部分
    private int findMaxFractionalPart() {
        double maxFraction = Double.NEGATIVE_INFINITY;
        int y = -1;
        for (int i = 0; i < data.constraintNr; i++) {
            double Bb = data.b.get(i);
            if (Math.ceil(Bb) - Bb < 0.0001 || Bb - Math.floor(Bb) < 0.0001) continue;
            double fraction = Bb - Math.floor(Bb);
            if (fraction > maxFraction) {
                y = i;
                maxFraction = fraction;
            }
        }
        return y;
    }

    // 对偶单纯形法
    private void dualSimplexAlgorithm() {
        while (!dualPivot()) {
            printSimplexTable();
            Gaussian();
        }
    }

    private boolean dualPivot() {
        // 取B逆b为负值绝对值较大的行
        double minB = Double.POSITIVE_INFINITY;
        int y = -1;
        for (int i = 0; i < data.constraintNr; i++) {
            if (data.b.get(i) < minB) {
                minB = data.b.get(i);
                y = i;
            }
        }

        // 所有B都大于等于0, 则对偶单纯形法已达到最优解
        if (minB > 0) {
            printSimplexTable();
            data.solution = 0;
            return true;
        }

        // 计算检验数
        for (int i = 0; i < data.variableNr; i++) {
            if (data.xB.contains(i)) {
                data.o.set(i, 0.0);
                continue;
            }
            double oj = data.c.get(i);
            for (int j = 0; j < data.constraintNr; j++) {
                oj -= data.c.get(data.xB.get(j)) * data.A.get(j).get(i);
            }
            data.o.set(i, oj);
        }

        // 计算theta值, 取theta小于0中较大的行
        double minTheta = Double.POSITIVE_INFINITY;
        int x = -1;
        for (int i = 0; i < data.variableNr; i++) {
            if (data.A.get(y).get(i) >= 0) {
                data.dualTheta.set(i, 0.0);
                continue;
            }
            double theta = data.o.get(i) / data.A.get(y).get(i);
            data.dualTheta.set(i, theta);
            if (theta < minTheta) {
                minTheta = theta;
                x = i;
            }
        }

        System.out.println(x + " ; " + y);

        if (x == -1) {
            printSimplexTable();
            data.solution = 2;
            return true;
        }

        p = new Pair(x, y);
        data.solution = 1;
        return false;
    }

    private void printSimplexTable() {
        StringBuilder str = new StringBuilder();
        str.append("\tc_j\t\t");
        for (Double cj : data.c) {
            str.append(String.format("%.3f", cj) + "\t\t");
        }
        str.append("B^-1b\t\ttheta");
        str.append("\n");
        str.append("c_B\tx_B\t\t");
        for (int i = 1; i <= data.variableNr; i++) {
            str.append("x" + String.valueOf(i) + "\t\t\t");
        }
        str.append("\n");

        for (int i = 0; i < data.constraintNr; i++) {
            str.append(String.valueOf(data.c.get(data.xB.get(i))) + "\t");
            str.append("x" + String.valueOf(data.xB.get(i) + 1) + "\t\t");
            for (int j = 0; j < data.variableNr; j++) {
                str.append(String.format("%.3f", data.A.get(i).get(j)) + "\t\t");
            }
            str.append(String.format("%.3f", data.b.get(i)) + "\t\t");
            str.append(String.format("%.3f", data.theta.get(i)));
            str.append("\n");
        }

        str.append("\to_j\t\t");
        for (Double oj : data.o) {
            str.append(String.format("%.3f", oj) + "\t\t");
        }
        str.append("\n");

        str.append("dualTheta\t\t");
        for (Double dt : data.dualTheta) {
            str.append(String.format("%.3f", dt) + "\t\t");
        }

        str.append("\n\n");

        System.out.println(str.toString());
    }
}