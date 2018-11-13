package Util;

import ParaStructure.KVPara.CatParaList;
import ParaStructure.KVPara.ParaKV;
import ParaStructure.KVPara.ParaKVPartition;
import ParaStructure.Partitioning.Partition;
import ParaStructure.Partitioning.PartitionList;
import ParaStructure.Sample.Sample;
import ParaStructure.Sample.SampleList;
import org.iq80.leveldb.DB;

import java.io.IOException;
import java.util.List;

public class BatchUtil {
    /*
    * 这个类主要是针对基于batch的划分方法
    * */


    /*
    * 统计每一个batch所需要的model参数
    * */

    public static PartitionList getBatchNeedPara(SampleList sampleList, int batchSize) {
        PartitionList partitionList = new PartitionList();
        for (int i = 0; i < sampleList.sampleListSize; i = i + batchSize) {  //遍历整个数据集
            Partition partition = new Partition();
            for (int j = i; j < i + batchSize&&j<sampleList.sampleListSize; j++) { // 遍历一个batch的数据集
                int[] cat = sampleList.sampleList.get(j).cat;
//                System.out.println(j);
                for (int k = 0; k < cat.length; k++) { //遍历一个数据的cat
                    if (cat[k] != -1 && partition.partition.contains(cat[k]) == false) {
                        partition.partition.add(cat[k]);
                    }
                }
            }
            partitionList.partitionList.add(partition);
        }
        return partitionList;
    }





    public static SampleList batchToSampleList(PartitionList batchNeedAccessList){
        SampleList sampleList=new SampleList();
        for(Partition partition:batchNeedAccessList.partitionList){
            int[] cats= new int[partition.partition.size()];
            for(int i=0;i<cats.length;i++){
                cats[i]=partition.partition.get(i);
            }
            Sample sample=new Sample(cats);
            sampleList.sampleList.add(sample);
        }
        sampleList.sampleListSize=sampleList.sampleList.size();




        return sampleList;
    }
}
