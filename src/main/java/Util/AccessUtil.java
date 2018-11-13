package Util;

import KVStore.ParaKVStore;
import ParaStructure.KVPara.*;
import ParaStructure.Partitioning.PartitionList;
import ParaStructure.Sample.Sample;
import ParaStructure.Sample.SampleList;
import org.iq80.leveldb.DB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AccessUtil {

    public static List<InvertIndex> getInvertIndices(SampleList sampleList, PartitionList bestPartitionList) throws IOException, ClassNotFoundException {
        /**
        *@Description: 这个读取就是按照sampleList去读取的
         * 对于mini-batch的方法，sampleList中一个sample就是一个“batch”所需要的访问的维度
         * 对于SGD的方法，sampleList中一个sample就是一条“data”需要访问的维度
        *@Param:
         * sampleList：对于batch的方法，sample里只需要包含int[] cat，也就是这个batch需要访问的离散属性就可以了
         * bestPartitionList：PartitionList，是最佳划分,
         * paraKVStore：就是KVStore
        *@return: void
        *@Author: SongZhen
        *@date: 下午8:14 18-11-12
        */

        List<InvertIndex> invertIndices=new ArrayList<InvertIndex>();
        for (int i = 0; i < sampleList.sampleListSize; i++) {
            // 先建立一个invertIndex来确定每条数据需要哪些维度,相同维度要去重
            InvertIndex invertIndex = InvertIndex.buildInvertIndex(sampleList.sampleList.get(i), bestPartitionList);
            invertIndices.add(invertIndex);

        }

        return invertIndices;
    }

    public static void accessParaByInvertIndices(List<InvertIndex> invertIndices,ParaKVStore paraKVStore) throws IOException,ClassNotFoundException{
        // 按照生成的反向索引，一个一个sample（对于SGD是一个data，对于MiniBGD是一个batch）去读取
        for(InvertIndex invertIndex:invertIndices){
            ParaKVStore.readParaByInvertIndex(invertIndex,paraKVStore);
        }

    }



    public static void initPara(int sparseDimSize,DB db) throws IOException{
        for(int i=0;i<sparseDimSize;i++){
            ParaKV paraKV=new ParaKV(i,(RandomUtil.getRandomValue(-0.1f,0.1f)));
            db.put(("s"+i).getBytes(),TypeExchangeUtil.toByteArray(paraKV));
        }
    }

    public static void getPara(SampleList sampleList,ParaKVStore paraKVStore)throws ClassNotFoundException,IOException{
        ParaKVPartition paraKVPartition=new ParaKVPartition();
        PartitionList bestPartitionList=new PartitionList();
        DB db=paraKVStore.db;

        List<InvertIndex> invertIndices=getInvertIndices(sampleList,bestPartitionList);
        accessParaByInvertIndices(invertIndices,paraKVStore);


//        for(int i=0;i<sampleList.sampleListSize;i++){
//            int[] cats=sampleList.sampleList.get(i).cat;
//            for(int cat:cats){
//                ParaKV paraKV= (ParaKV) TypeExchangeUtil.toObject(db.get(("c"+cat).getBytes()));
//                paraKVPartition.paraKVPartition.add(paraKV);
//            }
//
//        }


    }






}
