package KVStore;

import Global.Global;
import ParaStructure.KVPara.*;
import ParaStructure.Partitioning.PartitionList;
import Util.RandomUtil;
import Util.TypeExchangeUtil;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParaKVStore {

    public DB db;

    public static DB initParaKVstore(FeatureParaList featureParaList, CatParaList catParaList, List<Integer> prunedSparseDim,String path,int sparseDimSize) throws IOException,ClassNotFoundException {
        /**
        *@Description: 初始化参数及levelDB
        *@Param:
         * featureParaList:连续特征的参数列表,
         * catParaList：离散特征的参数列表,这里已经按照bestPartition划分完毕
         * sparseDimSize:用离散特征One-hot编码，构造出的稀疏维度的大小,
         * path：放leveldb数据的地址
        *@return: org.iq80.leveldb.DB
        *@Author: SongZhen
        *@date: 18-11-12
        */

        DB db=buildLevelDB(path);

        // 对于密集维度直接整个list存下就可以了,f表示feature
        db.put(("f").getBytes(),TypeExchangeUtil.toByteArray(featureParaList));

        // 对于cat参数要一个partition一个partition的写,c表示按照最佳划分，存储的cat离散属性
        for(int i=0;i<catParaList.catParaList.size();i++){
            ParaKVPartition paraKVPartition=catParaList.catParaList.get(i);
            db.put(("c"+i).getBytes(),TypeExchangeUtil.toByteArray(paraKVPartition));
        }





        for(int i=0;i<sparseDimSize;i++){
            if(prunedSparseDim.contains(i)==false){
                float initValue=RandomUtil.getRandomValue(-0.1f,0.1f);
                ParaKV paraKV=new ParaKV(i,initValue);
                db.put(("s"+i).getBytes(),TypeExchangeUtil.toByteArray(paraKV));
            }
        }

        return db;

    }

    public static List<ParaKVPartition> initCatPara(PartitionList bestPartitionList){
        /**
        *@Description: 给定最佳划分，返回初始化后的离散属性对应的参数CatParaList
        *@Param: [bestPartitionList]
        *@return: java.util.List<ParaStructure.KVPara.ParaKVPartition>
        *@Author: SongZhen
        *@date: 下午7:57 18-11-12
        */
        List<ParaKVPartition> paraKVPartitions=new ArrayList<ParaKVPartition>();

        // 最外层for循环用来循环所有的partition
        for(int i=0;i<bestPartitionList.partitionList.size();i++){
            ParaKVPartition paraKVPartition=new ParaKVPartition();
            List<Integer> partition=bestPartitionList.partitionList.get(i).partition;
            // 这个循环用来循环每个partition里面的元素
            for(int j=0;j<partition.size();j++){
                ParaKV paraKV=new ParaKV(partition.get(j),RandomUtil.getRandomValue(-0.1f,0.1f));
                paraKVPartition.paraKVPartition.add(paraKV);
            }
            paraKVPartitions.add(paraKVPartition);

        }
        return  paraKVPartitions;
    }

    public static List<ParaKV> initFeaturePara(int featureSize){
        /**
        *@Description: 给定featureSize，返回初始化后的连续feature对应的参数
        *@Param: [featureSize]
        *@return: java.util.List<ParaStructure.KVPara.ParaKV>
        *@Author: SongZhen
        *@date: 下午9:02 18-11-12
        */
        List<ParaKV> paraKVList=new ArrayList<ParaKV>();
        for(int i=0;i<featureSize;i++){
            ParaKV paraKV=new ParaKV(i,RandomUtil.getRandomValue(-0.1f,0.1f));
            paraKVList.add(paraKV);
        }
        return paraKVList;

    }



    public static void deleteFile(File deleteFilePath){
        /**
        *@Description: 删除文件，其实用于删除数据库，因为在测试的时候每次都要重新初始化写数据库，
         * 不删除的话数据会越来越多
        *@Param: [deleteFilePath]
        *@return: void
        *@Author: SongZhen
        *@date: 下午9:16 18-11-12
        */
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

    public static Object getParameter(String index,DB db)throws IOException,ClassNotFoundException{
        byte[] bytes=db.get(index.getBytes());
        return TypeExchangeUtil.toObject(bytes);
    }

    public static void readParaByInvertIndex(InvertIndex invertIndex,ParaKVStore paraKVStore)throws ClassNotFoundException,IOException{
        /**
        *@Description: 通过反向索引来读取参数
        *@Param: [invertIndex, paraKVStore]
        *@return: void
        *@Author: SongZhen
        *@date: 下午9:15 18-11-12
        */
        // readCatIndex和readSingleCIndex分别表示读进内存的catPartition和读进内存的Cat
        // 方便以后判断（如果内存中存在这个参数就不需要再读，如果不存在才要读）
        List<String> readCatIndex = new ArrayList<String>();
        List<String> readSingleCIndex = new ArrayList<String>();

        InvertIndex invertIndexes = new InvertIndex();

        // 读取的Cat划分参数和Cat独立参数
        List<ParaKV> readSingleCatParaList = new ArrayList<ParaKV>();
        CatParaList readCatParaList = new CatParaList();

        DB db = paraKVStore.db;

        for (int j = 0; j < invertIndex.invertIndex.size(); j++) {
            String index = invertIndex.invertIndex.get(j);
            invertIndexes.invertIndex.add(index);
            if (index.contains("c") && !readCatIndex.contains(index) ) {
                ParaKVPartition paraKVPartition = (ParaKVPartition) ParaKVStore.getParameter(index, db);
                readCatParaList.catParaList.add(paraKVPartition);
                readCatIndex.add(index);

            } else if (index.contains("s") && !readSingleCIndex.contains(index)) {
                ParaKV paraKV = (ParaKV) ParaKVStore.getParameter(index, db);
                readSingleCatParaList.add(paraKV);
                readSingleCIndex.add(index);
            }

        }
    }

    public static DB buildLevelDB(String path) throws IOException,ClassNotFoundException{
        File filePath=new File(path);
        File deleteFilePath=new File(path+"db/");
        deleteFile(deleteFilePath);
        DB db= Iq80DBFactory.factory.open(new File(filePath,"db"),new Options().createIfMissing(true));
        return db;
    }

}
