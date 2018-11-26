package PartitioningByInvertIndex;

import Global.Global;
import KMeansCluster.CatAsRow;
import KMeansCluster.CatAsRowList;
import ParaStructure.Partitioning.Partition;
import ParaStructure.Partitioning.PartitionList;
import Util.ArrayListUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @program: CtrForBigModel
 * @description: 通过反向索引来构造最佳划分，不用每次都构建AF矩阵了
 * @author: SongZhen
 * @create: 2018-11-21 20:02
 */
public class PartitioningByInvertIndex {

    public static PartitionList getBestPartitionListByInvertIndex(CatAsRowList catAsRowList,List<Integer> prunedSparseDim){
        // 初始化最佳分配
        if (prunedSparseDim.size() != 0) {
            PartitionList bestPartitionList = new PartitionList();
            Partition firstPartition = new Partition();
            firstPartition.partition.add(0);


            bestPartitionList.partitionList.add(firstPartition);

            for (int i = 1; i < catAsRowList.catAsRowList.size(); i++) {
                List<Integer> catAsRow = catAsRowList.catAsRowList.get(i).catAsRow;
                // 计算合并前single cat属性访问时间
                float costForwardOfSingle = getCostForwardOfSingle(catAsRow);
                bestPartitionList = getPartialBestPartitionList(i, costForwardOfSingle, catAsRow, bestPartitionList, catAsRowList);

            }
            return PartitionList.seqIndexToRealIndex(bestPartitionList,prunedSparseDim);
        }
        else {
            return  new PartitionList();
        }


    }

    public static float getCostForwardOfSingle(List<Integer> catAsRow){
        return catAsRow.size()*(Global.diskAccessTime.seekSingleTime+Global.diskAccessTime.readSingleDimTime*Global.paraKVSize);
    }

    public static float getCostForwardOfPartition(List<Integer> partition,CatAsRowList catAsRowList){
        // 如果partition的大小是1，那么是用ParaKV存的，大小是70个字节
        if(partition.size()==1){
            List<Integer> catAsRow=catAsRowList.catAsRowList.get(partition.get(0)).catAsRow;
            return getCostForwardOfSingle(catAsRow);
        }
        else {
            float cost=0;
            int partitionSize=partition.size();
            int partitionAccessNum=getPartitionAccessNum(partition,catAsRowList);
            return getPartitionCost(partitionSize,partitionAccessNum);
        }

    }


    public static float getPartitionCost(int partitionSize,int partitionAccessNum){

        float singleSampleAccessTime=(Global.diskAccessTime.readSingleDimTime)*(Global.paraKVPartitionBasicSize+Global.singleParaKVOfPartition*partitionSize);
        float totalTime=(Global.diskAccessTime.seekSingleTime+singleSampleAccessTime*partitionAccessNum);
        return totalTime;
    }

    public static int getPartitionAccessNum(List<Integer> partition ,CatAsRowList catAsRowList){
        /**
        *@Description: 用集合并集的方法，来求得partition的大小
        *@Param: [partition, catAsRowList]
        *@return: int
        *@Author: SongZhen
        *@date: 下午10:42 18-11-21
        */
        // 可以转换为求集合并集
        Set<Integer> set=new HashSet<Integer>();
        for(int partitionItem:partition){
            List<Integer> catAccessSample=catAsRowList.catAsRowList.get(partitionItem).catAsRow;
            for(int catAccessSampleItem:catAccessSample){
                set.add(catAccessSampleItem);
            }
        }
        return set.size();

    }

    public static float getCostReduce(float ostForward,float costForwardOfPartition, List<Integer> catAsRow, List<Integer> partition){
        return 0;
    }

    public static PartitionList getPartialBestPartitionList(int i,float costForwardOfSingle,List<Integer> catAsRow,PartitionList bestPartitionList,CatAsRowList catAsRowList){
        // 最大获益值、最大获益值对应的Partition
        float maxGain=0;
        int partitionIndexOfMaxGain=-1;

        for(int j=0;j<bestPartitionList.partitionList.size();j++){
            List<Integer> partition=bestPartitionList.partitionList.get(j).partition;
            List<Integer> partitionTemp=ArrayListUtil.intListClone(partition);
            // 计算合并前partition访问时间
            float costForwardOfPartition=getCostForwardOfPartition(partitionTemp,catAsRowList);
            float sumCostForward=costForwardOfPartition+costForwardOfSingle;

            // 计算合并后时间损失
            partitionTemp.add(i);
            float costForwardOfMergePartition=getCostForwardOfPartition(partitionTemp,catAsRowList);

            // 计算合并之后减少，以及最大减少
            float costReduce=sumCostForward-costForwardOfMergePartition;
            if(costReduce>maxGain){
                maxGain=costReduce;
                partitionIndexOfMaxGain=j;
            }


        }

        if (partitionIndexOfMaxGain == -1) {
            Partition partition = new Partition();
            partition.partition.add(i);
            bestPartitionList.partitionList.add(partition);
        }
        else {
            bestPartitionList.partitionList.get(partitionIndexOfMaxGain).partition.add(i);
        }

        return bestPartitionList;
    }
}