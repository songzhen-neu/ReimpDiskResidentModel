package ParaStructure.KVPara;

import java.io.Serializable;

public class ParaKV implements Serializable {
    /*
    * 这个类是用来存储模型参数的
    * */

    public int key;
    public float value;

    public ParaKV(int key,float value){
        this.key=key;
        this.value=value;
    }

}
