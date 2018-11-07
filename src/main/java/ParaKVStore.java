import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParaKVStore {

    public static DB initParaKVstore(FeatureParaList featureParaList, CatParaList catParaList,int sparseDimSize) throws IOException {
        File filePath=new File("data/");
        File deleteFilePath=new File("data/db");
        deleteFile(deleteFilePath);


        DB db= Iq80DBFactory.factory.open(new File(filePath,"db"),new Options().createIfMissing(true));
        /*对于密集维度直接整个list存下就可以了*/
        db.put(("f").getBytes(),TypeExchangeUtil.toByteArray(featureParaList));

        /*对于cat参数要一个partition一个partition的写*/
        for(int i=0;i<catParaList.catParaList.size();i++){
            ParaKVPartition paraKVPartition=catParaList.catParaList.get(i);
            db.put(("c"+i).getBytes(),TypeExchangeUtil.toByteArray(paraKVPartition));
        }


        /*初始化single category属性到KVStore*/
        List<Integer> catDimList=new ArrayList<Integer>();
        for(ParaKVPartition paraKVPartition:catParaList.catParaList){
            for(ParaKV paraKV:paraKVPartition.paraKVPartition){
                catDimList.add(paraKV.key);
            }
        }

        for(int i=0;i<sparseDimSize;i++){
            if(catDimList.contains(i)==false){
                float initValue=getRandomValue(-0.1f,0.1f);
                ParaKV paraKV=new ParaKV(i,initValue);
                db.put(("sc"+i).getBytes(),TypeExchangeUtil.toByteArray(paraKV));
            }
        }

        return db;

    }

    public static List<ParaKVPartition> initCatPara(PartitionList bestPartitionList){
        List<ParaKVPartition> paraKVPartitions=new ArrayList<ParaKVPartition>();

        /*最外层for循环用来循环所有的partition*/
        for(int i=0;i<bestPartitionList.partitionList.size();i++){
            ParaKVPartition paraKVPartition=new ParaKVPartition();
            List<Integer> partition=bestPartitionList.partitionList.get(i).partition;
            /*这个循环用来循环每个partition里面的元素*/
            for(int j=0;j<partition.size();j++){
                ParaKV paraKV=new ParaKV(partition.get(j),getRandomValue(-0.1f,0.1f));
                paraKVPartition.paraKVPartition.add(paraKV);
            }
            paraKVPartitions.add(paraKVPartition);

        }
        return  paraKVPartitions;
    }

    public static List<ParaKV> initFeaturePara(int featureSize){
        List<ParaKV> paraKVList=new ArrayList<ParaKV>();
        for(int i=0;i<featureSize;i++){
            ParaKV paraKV=new ParaKV(i,getRandomValue(-0.1f,0.1f));
            paraKVList.add(paraKV);
        }
        return paraKVList;

    }

    public static float getRandomValue(float minValue,float maxValue){
        Random random=new Random();
        /*最小的值加上(0,1)*distance，就相当于随机了一个minValue到maxValue之间的一个最小值*/
        return random.nextInt(10000)/10000.0f*(maxValue-minValue)+minValue;
    }

    public static void deleteFile(File deleteFilePath){
        if(deleteFilePath.isDirectory()){
            File[] files=deleteFilePath.listFiles();
            for(int i=0;i<files.length;i++){
                File file=files[i];
                deleteFile(file);
            }
        }
        else if(deleteFilePath.exists()){
            deleteFilePath.delete();
        }
    }
}
