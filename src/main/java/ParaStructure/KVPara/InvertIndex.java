package ParaStructure.KVPara;

import ParaStructure.Partitioning.Partition;
import ParaStructure.Partitioning.PartitionList;
import ParaStructure.Sample.Sample;

import java.util.ArrayList;
import java.util.List;

public class InvertIndex {
    public List<String> invertIndex=new ArrayList<String>();

    public static InvertIndex buildInvertIndex(Sample sample, PartitionList partitionList){
        /**
        *@Description: 构建反向索引，表明某个维度sample（可能是一条data，也可能是一个batch）
         * 所需要访问的维度的索引（索引是基于最佳划分形成的）
        *@Param: [sample, partitionList]
        *@return: ParaStructure.KVPara.InvertIndex
        *@Author: SongZhen
        *@date: 下午8:55 18-11-12
        */
        int[] cat=sample.cat;
        InvertIndex invertIndex=new InvertIndex();
        for(int i=0;i<cat.length;i++){
            String index= getIndexOfDim(cat[i],partitionList);
            // 这里s-1说明这个维度是missing data，后面的判别式表示如果这条数据已经读了这个catPartition，就不要重复读取了
            if(index.equals("s-1")==false&&invertIndex.invertIndex.contains(index)==false){
                invertIndex.invertIndex.add(getIndexOfDim(cat[i],partitionList));
            }
        }

        // 下面是invertIndex相同维度去重

        return invertIndex;
    }

    public static String getIndexOfDim(int cat,PartitionList partitionList){
        /**
        *@Description: 给定某个cat维度，返回这个维度在新索引中key
        *@Param: [cat, partitionList]
        *@return: java.lang.String
        *@Author: SongZhen
        *@date: 下午8:51 18-11-12
        */
        for(int j=0;j<partitionList.partitionList.size();j++){
            Partition partition=partitionList.partitionList.get(j);
            if(partition.partition.contains(cat)){
                return "c"+j;
            }
        }

        return "s"+cat;

    }
}
