/**
 * @author： zll-hust
 * @date： 2020/12/23 10:55
 * @description： 主函数
 */
public class Main {
    public static void main(String[] args) {
        Data d = new Data("example2");
        d.readFile();
        CuttingPlaneAlgorithm c = new CuttingPlaneAlgorithm(d);
        c.gomoriAlgorithm();
        d.printResult();
    }
}