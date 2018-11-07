import Data.DataProcess;
import ParaStructure.KVPara.CatParaList;
import ParaStructure.KVPara.FeatureParaList;
import KVStore.ParaKVStore;
import ParaPartition.ModelPartition;
import ParaStructure.Partitioning.PartitionList;
import ParaStructure.Sample.SampleList;
import Util.AccessTimeTest;
import org.iq80.leveldb.DB;

import java.io.IOException;

public class LogisticRegression {
    static int featureSize= 13;
    static int catSize= 26;
    static int sparseDimSize=0;


    public static void main(String[] args) throws IOException,ClassNotFoundException {
        long startTime,endTime;
        String fileName="data/10000.txt";
        SampleList sampleList=new SampleList();

        /*处理并读取训练集数据到sampleList中*/
        sampleList=DataProcess.readData(fileName,featureSize,catSize); //用来读取train.csv的数据

        /*获取最佳划分*/
        PartitionList bestPartitionList= ModelPartition.modelPartition(sampleList);

        /*将训练集的连续特征标准化*/
        sampleList=DataProcess.linerNormalization(sampleList);

        /*初始化模型*/
        ParaKVStore paraKVStore=new ParaKVStore();
        CatParaList catParaList=new CatParaList();
        FeatureParaList featureParaList=new FeatureParaList();

        featureParaList.featureParaList=paraKVStore.initFeaturePara(featureSize);
        catParaList.catParaList=paraKVStore.initCatPara(bestPartitionList);
        DB db=paraKVStore.initParaKVstore(featureParaList,catParaList,sampleList.sparseDimSize);
        paraKVStore.db=db;
        sparseDimSize=sampleList.sparseDimSize;


        /*遍历所有的样本，一个一个样本的读取所有数据*/
        startTime=System.currentTimeMillis();
        AccessTimeTest.accessTimeTest(sampleList,bestPartitionList,paraKVStore);
        endTime=System.currentTimeMillis();
        System.out.println(endTime-startTime);

        /*构建一个大小sparseDimSize的参数模型，然后一条数据一条数据读*/
        AccessTimeTest.initPara(sparseDimSize,db);
        startTime=System.currentTimeMillis();
        AccessTimeTest.getPara(sampleList,db);
        endTime=System.currentTimeMillis();
        System.out.println(endTime-startTime);

        db.close();
    }




}
