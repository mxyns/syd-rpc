package rpc;
import java.io.Serializable;

public interface IMatlab extends Serializable {
  public Result calcul(Integer i) throws Exception;
}


