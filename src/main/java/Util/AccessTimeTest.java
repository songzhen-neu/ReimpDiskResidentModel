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


public class AccessTimeTest {

    public static void accessTimeTest(SampleList sampleList, PartitionList bestPartitionList, ParaKVStore paraKVStore) throws IOException,ClassNotFoundException{
        DB db=paraKVStore.db;
        InvertIndex invertIndexes=new InvertIndex();
        for(int i=0;i<sampleList.sampleListSize;i++){
            /*readCatIndex和readSingleCIndex分别表示读进内存的catPartition和读进内存的Cat*/
            FeatureParaList readFeatureParaList=new FeatureParaList();
            CatParaList readCatParaList=new CatParaList();
            List<String> readCatIndex=new ArrayList<String>();
            List<String> readSingleCIndex=new ArrayList<String>();
            /*先建立一个invertIndex来确定每条数据需要哪些维度*/
            InvertIndex invertIndex= InvertIndex.buildIvertIndex(sampleList.sampleList.get(i),bestPartitionList);
            for(int j=0;j<invertIndex.invertIndex.size();j++){
                invertIndexes.invertIndex.add(invertIndex.invertIndex.get(j));
            }


            /*测试一条一条读数据，然后读取model*/

            if(i%100==0){
                for(String index:invertIndex.invertIndex){
                    if(index.indexOf("c")!=-1&&readCatIndex.contains(index)==false){
                        ParaKVPartition paraKVPartition=(ParaKVPartition) paraKVStore.getParameter(index,db);
                        readCatParaList.catParaList.add(paraKVPartition);
                        readCatIndex.add(index);

                    }
                    else if(index.indexOf("s")!=-1&&readSingleCIndex.contains(index)==false){
                        ParaKV paraKV=(ParaKV) paraKVStore.getParameter(index,db);
                        readFeatureParaList.featureParaList.add(paraKV);
                    }
                }
                invertIndexes.invertIndex.clear();
            }


        }
    }



    public static void initPara(int sparseDimSize,DB db) throws IOException{
        for(int i=0;i<sparseDimSize;i++){
            ParaKV paraKV=new ParaKV(i,(RandomUtil.getRandomValue(-0.1f,0.1f)));
            db.put(("t"+i).getBytes(),TypeExchangeUtil.toByteArray(paraKV));
        }
    }

    public static void getPara(SampleList sampleList,DB db){
        /*构建反向索引*/

        InvertIndex invertIndexes=new InvertIndex();
        for(int i=0;i<sampleList.sampleListSize;i++){
            int[] cat=sampleList.sampleList.get(i).cat;
            /*一条数据的inverIndex*/
            InvertIndex invertIndex=new InvertIndex();
            for(int j=0;j<cat.length;j++){
                if (invertIndex.invertIndex.contains(cat[j])==false){
                    invertIndex.invertIndex.add("t"+cat[j]);
                }
            }
            for(int l=0;l<invertIndex.invertIndex.size();l++){
                invertIndexes.invertIndex.add(invertIndex.invertIndex.get(l));
            }

            if(i%100==0){
                for(int m=0;m<invertIndexes.invertIndex.size();m++){
                    db.get((invertIndexes.invertIndex.get(m)).getBytes());
                }
                invertIndexes.invertIndex.clear();
            }
        }


    }


}
