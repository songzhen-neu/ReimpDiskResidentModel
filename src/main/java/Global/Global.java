package Global;

import Data.DiskAccessTime;

public interface Global {

    /** 磁盘access单个参数的时间、磁盘seek一个参数地址的时间 */
    DiskAccessTime diskAccessTime=new DiskAccessTime(0.0000046f,0.0029f);

    /** 连续特征维度*/
    int featureSize= 10;

    /** 离散特征维度*/
    int catSize= 12;

    /** 数据集路径*/
    String dataFilePath="data/train.csv/";

    /** mini-batch SGD的batch大小*/
    int batchSize=20;

    /** 数据库创建路径*/
    String dbForSGDFilePath="data/DBForSGD/";
    String dbForMiniBGDFilePath="data/DBForMiniBGD/";
    String dbForNoVerticalPartitionPath="data/DBForNoVerticalPartitionPath/";
    String dbForKMeans="data/DBForKMeans/";
    String dbForPartitioningByInvertIndex="data/DBForPartitioningByInvertIndex";

    /** 剪枝率,而剪枝频率下限，一个是针对batch的一个是针对single的*/
    float pruneRate=0.00001f;
    Double freqThresholdForBatch=50.0;
    Double freqThresholdForSingle=10.0;

    /** 如果是false代表不用sketch获取freqThreshold*/
    boolean usePruneRate=false;

    /** Partition包含元素个数的最小值*/
    int minPartitionSize=2;

    /** 离散特征在前，连续特征在后*/
    boolean isCatForwardFeature=true;

    /** 读取的最大训练集个数*/
    int maxSampleListSize=300000;

    /** 构建Partition的时候样本采样个数,注意剪枝大小要大于batch大小*/
    int samplePrunedSize=50000;

    /** 一个ParaKV的字节树*/
    int paraKVSize=70;

    /** 一个ParaKVPartition对象在没有元素的情况下基本存储字节数*/
    int paraKVPartitionBasicSize=200;

    /** ParaKVPartition里的一个ParaKV的字节数*/
    int singleParaKVOfPartition=18;

    /** k-means聚类的k的值*/
    int k=20;

    /** 收益下限，当最大收益值小于了这个下限，就不再进行下面的对比合并*/
    float minGain=0;

}
