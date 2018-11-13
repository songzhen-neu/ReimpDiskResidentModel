package Global;

import Data.DiskAccessTime;

public interface Global {

    /** 磁盘access单个参数的时间、磁盘seek一个参数地址的时间 */
    DiskAccessTime diskAccessTime=new DiskAccessTime(0.0002f,0.1f);

    /** 连续特征维度*/
    int featureSize= 13;

    /** 离散特征维度*/
    int catSize= 26;

    /** 数据集路径*/
    String dataFilePath="data/10000.txt/";

    /** mini-batch SGD的batch大小*/
    int batchSize=50;

    /** 数据库创建路径*/
    String dbForSGDFilePath="data/DBForSGD/";
    String dbForMiniBGDFilePath="data/DBForMiniBGD/";
    String dbForNoVerticalPartitionPath="data/DBForNoVerticalPartitionPath/";

    /** 剪枝率*/
    float pruneRate=0.001f;
    Double freqThreshold=100.0;

    /** 如果是false代表不用sketch获取freqThreshold*/
    boolean usePruneRate=false;

    /** Partition包含元素个数的最小值*/
    int minPartitionSize=2;



}
