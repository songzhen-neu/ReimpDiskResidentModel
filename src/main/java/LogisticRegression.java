import org.iq80.leveldb.DB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogisticRegression {
    static int featureSize= 13;
    static int catSize= 26;
    static int sparseDimSize=0;


    public static void main(String[] args) throws IOException,ClassNotFoundException {
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

        /*获取并转换feature*/
        byte[] bytes=db.get(("f").getBytes());
        FeatureParaList readFeatParaList=(FeatureParaList) TypeExchangeUtil.toObject(bytes);

        /*获取并转换cat属性*/
        CatParaList readCatParaList=new CatParaList();
        bytes=db.get(("c1").getBytes());
        readCatParaList.catParaList.add((ParaKVPartition)TypeExchangeUtil.toObject(bytes));

        /*获取single category，并转换*/
        ParaKV singleC;
        bytes=db.get(("sc100").getBytes());
        singleC=(ParaKV) TypeExchangeUtil.toObject(bytes);


        System.out.println("ss");
    }
}
