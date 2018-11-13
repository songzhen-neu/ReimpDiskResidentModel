package ParaStructure.Partitioning;

import Global.Global;

import java.util.ArrayList;
import java.util.List;

public class PartitionList{
    public List<Partition> partitionList=new ArrayList<Partition>();

    public static PartitionList getBestPartitionListWithNoOnceItem(PartitionList bestPartitionList){
        /**
        *@Description: 如果划分中只有一个元素，那么没有必要按照KVParaPartition读
        *@Param: [bestPartitionList]
        *@return: ParaStructure.Partitioning.PartitionList
        *@Author: SongZhen
        *@date: 上午9:53 18-11-13
        */
        PartitionList bestPartitionListWithNoOnceItem=new PartitionList();
        for(int i=0;i<bestPartitionList.partitionList.size();i++){
            Partition partition=bestPartitionList.partitionList.get(i);
            if(partition.partition.size()>(Global.minPartitionSize-1)){
                bestPartitionListWithNoOnceItem.partitionList.add(partition);
            }
        }

        return bestPartitionListWithNoOnceItem;
    }

    public static void showBestPartitionList(PartitionList bestPartitionList){
        for(Partition partition: bestPartitionList.partitionList){
                System.out.println(partition.partition);
        }
    }

}
