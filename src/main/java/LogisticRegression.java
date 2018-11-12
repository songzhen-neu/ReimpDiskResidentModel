
import Global.Global;
import ParaPartition.BatchBasedModelPartition;
import ParaPartition.ModelPartition;
import ParaStructure.KVPara.CatParaList;
import ParaStructure.KVPara.FeatureParaList;
import KVStore.ParaKVStore;
import ParaStructure.Partitioning.PartitionList;
import ParaStructure.Sample.SampleList;
import Util.*;
import org.iq80.leveldb.DB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;




public class LogisticRegression {

    public static int sparseDimSize=0;
    public static int featureSize=Global.featureSize;


    public static void main(String[] args) throws IOException,ClassNotFoundException {

        String fileName=Global.dataFilePath;
        int batchSize=Global.batchSize;
        SampleList sampleList=new SampleList();

        // 定义feature和cat大小

        int catSize=Global.catSize;



        // 处理并读取训练集数据到sampleList中
        sampleList=DataProcessUtil.readData(fileName,featureSize,catSize);

        // 将训练集的连续特征标准化
        sampleList=DataProcessUtil.linerNormalization(sampleList);

        // sparseDimensionSize
        sparseDimSize=sampleList.sparseDimSize;

        // 论文中time测试，包含了bestPartitionList和最笨的测试
        reimplTimeTest(sampleList);

        // 不对参数进行垂直划分，直接读
        readWithoutPartition(sampleList);

        // batch-based划分方法
//        batchBasedMethodTest(sampleList,batchSize);


    }


    public static void reimplTimeTest(SampleList sampleList) throws IOException,ClassNotFoundException{

        /**
        *@Description: 实现论文中的方法，也就是针对SGD的，一条数据一条数据读
        *@Param: [sampleList]
        *@return: void
        *@Author: SongZhen
        *@date: 下午7:50 18-11-12
        */
        // 初始化模型,包括kvStore，离散和连续属性对应的参数列表
        ParaKVStore paraKVStore=new ParaKVStore();
        CatParaList catParaList=new CatParaList();
        FeatureParaList featureParaList=new FeatureParaList();

        // 维度剪枝
        float pruneRate=Global.pruneRate;
        List<Integer> prunedSparseDim=Prune.prune(sampleList,pruneRate);

        // 模型划分，得到最佳划分
        PartitionList bestPartitionList= ModelPartition.modelPartition(sampleList,prunedSparseDim);

        // 初始化离散和连续特征对应的参数列表（离散属性已对应上最佳划分）
        featureParaList.featureParaList=ParaKVStore.initFeaturePara(featureSize);
        catParaList.catParaList=ParaKVStore.initCatPara(bestPartitionList);


        DB db=ParaKVStore.initParaKVstore(featureParaList,catParaList,prunedSparseDim,Global.dbForSGDFilePath,sampleList.sparseDimSize);
        paraKVStore.db=db;


        // bestPartitionList,遍历所有的样本，一个一个样本的读取所有数据
        CurrentTimeUtil.setStartTime();
        AccessTimeTest.accessTimeTest(sampleList,bestPartitionList,paraKVStore);
        CurrentTimeUtil.setEndTime();
        System.out.println("bestPartitionListRead:"+CurrentTimeUtil.getExecuteTime());


        db.close();
    }


    public static void readWithoutPartition(SampleList sampleList) throws IOException,ClassNotFoundException{
        /*构建一个大小sparseDimSize的参数模型，然后一条数据一条数据读*/
        ParaKVStore paraKVStore=new ParaKVStore();
        DB db=ParaKVStore.buildLevelDB(Global.dbForNoVerticalPartitionPath);
        paraKVStore.db=db;
        AccessTimeTest.initPara(sparseDimSize,db);
        CurrentTimeUtil.setStartTime();
        AccessTimeTest.getPara(sampleList,db);
        CurrentTimeUtil.setEndTime();
        System.out.println("singleRead:"+CurrentTimeUtil.getExecuteTime());

        db.close();
    }



    public static void batchBasedMethodTest(SampleList sampleList,int batchSize)throws IOException,ClassNotFoundException{
        long startTime,endTime;


        ParaKVStore paraKVStore=new ParaKVStore();
        CatParaList catParaList=new CatParaList();
        CatParaList catParaListRead=new CatParaList();
        FeatureParaList featureParaList=new FeatureParaList();

        // 维度剪枝
        float pruneRate=Global.pruneRate;
        List<Integer> prunedSparseDim=Prune.prune(sampleList,pruneRate);

        // 这个partitionList是每个batch需要访问的维度
        PartitionList batchNeedAccessList=BatchBasedPartitionTimeTest.statisticBatchBasedModelPartition(sampleList,batchSize); //这是每个batch所需要访问的维度

        PartitionList bestCatPartition=BatchBasedModelPartition.getBestPartitionList(batchNeedAccessList,sampleList);  // 最佳划分


        featureParaList.featureParaList=ParaKVStore.initFeaturePara(featureSize);
        catParaList.catParaList=ParaKVStore.initCatPara(bestCatPartition);

        /*这块是有问题的，因为虽然是batchBased但是首先要保证，catParaList中catPartition之间的元素不能相同
        * 所以这个方法是有问题的
        * 所以还是按照人家的方法去找，但是这次找的是batch的
        * sampleList编程batchList
        * */
        DB db=ParaKVStore.initParaKVstore(featureParaList,catParaList,prunedSparseDim,Global.dbForMiniBGDFilePath,sparseDimSize);
        paraKVStore.db=db;



        SampleList batchList= BatchBasedPartitionTimeTest.batchToSampleList(batchNeedAccessList);
        CurrentTimeUtil.setStartTime();
        AccessTimeTest.accessTimeTest(batchList,bestCatPartition,paraKVStore);
        CurrentTimeUtil.setEndTime();

        endTime=System.currentTimeMillis();
        System.out.println("Batch-based Read:"+CurrentTimeUtil.getExecuteTime());






        db.close();
    }

    public static PartitionList getNewIndexOfNeedAccessList(PartitionList batchNeedAccessList,PartitionList bestCatPartition){

        for(int i=0;i<batchNeedAccessList.partitionList.size();i++){
            List<Integer> batch_i = batchNeedAccessList.partitionList.get(i).partition;
            for(int j=0;j<batch_i.size();j++){

            }
        }


        return null;
    }

}
