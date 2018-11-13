package Util;

import ParaStructure.KVPara.ParaKV;
import ParaStructure.KVPara.ParaKVPartition;

import java.io.IOException;

/**
 * @program: CtrForBigModel
 * @description: 这是一个测试类，可用来测各种时间、空间占用等
 * @author: SongZhen
 * @create: 2018-11-13 14:58
 */
public class TestUtil {
    public static void spaceCostTest() throws IOException {
        /**
        *@Description: 测试一个ParaKV和一个ParaKVPartition所占用的空间
         * 结果是一个ParaKV占70字节
         * 一个ParaKVPartition的基础是200字节，每添加一个元素增加14个字节
        *@Param: []
        *@return: void
        *@Author: SongZhen
        *@date: 下午3:10 18-11-13
        */
        int paraKVPartitionSize=20;
        ParaKVPartition paraKVPartition=new ParaKVPartition();
        for(int i=0;i<paraKVPartitionSize;i++){
            ParaKV paraKV=new ParaKV(i,Float.parseFloat(""+i));
            paraKVPartition.paraKVPartition.add(paraKV);
        }



        byte[] bytesOfParaKVPartition=TypeExchangeUtil.toByteArray(paraKVPartition);

        System.out.println("SpaceCostTest");
    }
}