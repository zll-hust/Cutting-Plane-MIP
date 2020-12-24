import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * @author： zll-hust
 * @date： 2020/12/23 10:56
 * @description： 存放问题的相关数据
 */
public class Data {
    public String fileName;
    public int variableNr; // 变量个数
    public int constraintNr; // 约束方程个数

    public ArrayList<ArrayList<Double>> A; // 约束方程系数
    public ArrayList<Double> b; // 自由变量
    public ArrayList<Double> c; // 目标函数系数

    public ArrayList<Integer> xB; // 基变量
    public ArrayList<Double> theta; // 单纯形表右侧theta
    public ArrayList<Double> dualTheta; // 对偶单纯形表下侧theta
    public ArrayList<Double> o; // 检验数

    public ArrayList<Boolean> isInteger; // 是否是整数

    public int solution; // 0代表已为最优解且最优解唯一；1代表继续迭代；2代表不存在最优解；3代表最优解不唯一


    public Data(String fileName) {
        this.fileName = fileName;
    }

    public void readFile() {
        try {
            String data;
            Scanner in = new Scanner(new FileReader("./" + fileName + ".txt"));

            data = in.nextLine();
            variableNr = Integer.parseInt(data.split("\\s+")[0]);
            constraintNr = Integer.parseInt(data.split("\\s+")[1]);

            A = new ArrayList<>();
            for(int i = 0; i < constraintNr; i++){
                A.add(new ArrayList<>());
            }
            b = new ArrayList<>();
            c = new ArrayList<>();
            isInteger = new ArrayList<>();

            data = in.nextLine();
            for(int i = 0; i < variableNr; i++){
                if(data.split("\\s+")[i].equals("0"))
                    isInteger.add(false);
                else
                    isInteger.add(true);
            }

            data = in.nextLine();
            for(int i = 0; i < variableNr; i++){
                c.add(Double.parseDouble(data.split("\\s+")[i]));
            }

            for(int i = 0; i < constraintNr; i++){
                data = in.nextLine();
                if(data.equals("END")) break;
                for(int j = 0; j < variableNr; j++){
                    A.get(i).add(Double.parseDouble(data.split("\\s+")[j]));
                }
                b.add(Double.parseDouble(data.split("\\s+")[variableNr]));
            }

            System.out.println("Instance File input success!");

        } catch (FileNotFoundException e) {
            // File not found
            System.out.println("Instance File not found!");
            System.exit(-1);
        }
    }

    public void printResult(){
        if(solution == 0){
            System.out.println("one optimal solution is found:");
        }else if(solution == 1){
            System.out.println("continue...");
        }else if(solution == 2){
            System.out.println("no optimal solution.");
        }else if(solution == 3){
            System.out.println("not only one optimal solution.");
        }

        if(solution == 0 || solution == 3){
            StringBuilder str = new StringBuilder();
            double[] x = new double[variableNr];
            Arrays.fill(x, 0);
            for(int i = 0; i < constraintNr; i++){
                x[xB.get(i)] = b.get(i);
            }

            double obj = 0;
            for(int i = 0; i < variableNr; i++){
                obj += x[i] * c.get(i);
                str.append("x" + (i + 1) + " = " + String.format("%.3f", x[i]) + "\n");
            }
            str.append("\nobjective = " + String.format("%.3f", obj) + "\n");
            System.out.println(str.toString());
        }
    }
}