package rpc;
public class Client {
  public static void main(String [] arg) throws Exception {
    
    IMatlab m = new Matlab();
    Result res = m.calcul(3);
    System.out.println("->" + res);

  }
}
 

