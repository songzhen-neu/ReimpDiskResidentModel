package KMeansCluster;

import Global.Global;
import ParaStructure.Partitioning.Partition;
import ParaStructure.Partitioning.PartitionList;
import Util.ListToHashset;
import Util.RandomUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @program: CtrForBigModel
 * @description: 这是一个partition划分类，这里采用的是kmeans方法划分
 * @author: SongZhen
 * @create: 2018-11-19 20:01
 */
public class PartitioningByKMeans {



    public static PartitionList getBestPLByKMeans(int k, CatAsRowList catAsRowList, int samplePrunedSize, List<Integer> prunedSparseDim){
        /**
        *@Description: 使用kmeans的方法得到最佳划分
        *@Param: [k,
         * catAsRowList:行是未映射的cat特征，列是该cat特征出现的sample的index,
         * samplePrunedSize]
        *@return: ParaStructure.Partitioning.PartitionList
        *@Author: SongZhen
        *@date: 下午8:09 18-11-19
        */
        // 初始化k个中心
        float[][] kCentre=initKCentre(k,samplePrunedSize);

        PartitionList partitionList=getClusterResult(k,catAsRowList,kCentre,samplePrunedSize);
        PartitionList bestPartitionList=kMeans(k,partitionList,catAsRowList,samplePrunedSize,kCentre);
        bestPartitionList=PartitionList.seqIndexToRealIndex(bestPartitionList,prunedSparseDim);


        return bestPartitionList;
    }

    public static float[][] initKCentre(int k,int samplePrunedSize){
        /**
        *@Description: 初始化k个中心
        *@Param: [k, samplePrunedSize]
        *@return: int[][]
        *@Author: SongZhen
        *@date: 下午8:32 18-11-19
        */
        float[][] kCentre=new float[k][samplePrunedSize];
        for(int i=0;i<k;i++){
            for(int j=0;j<samplePrunedSize;j++){
                kCentre[i][j]=RandomUtil.getRandomZeroOrOne();
            }
        }
        return kCentre;
    }




    public static double getDist(List<Integer> catAsRow,float[] centre){
        /**
        *@Description: 计算欧式距离,这里的distance应该考虑到数据稀疏度不同，还得除以一个分母（两个非零元素之和）
        *@Param: [catAsRow, centre]
        *@return: double
        *@Author: SongZhen
        *@date: 下午9:41 18-11-19
        */
        double sum=0;
        Set<Integer> catAsRowHashSet=ListToHashset.listToHashSetInt(catAsRow);
        int catAsRowNonZeroNum=0;
        for(int i=0;i<catAsRow.size();i++){
            if(catAsRow.get(i)!=0){
                catAsRowNonZeroNum++;
            }
        }


        for(int i=0;i<centre.length;i++){
            if(catAsRowHashSet.contains(i)){
                sum=sum+Math.pow((1-centre[i]),2);
            }
            else {
                sum=sum+Math.pow((0-centre[i]),2);
            }
        }

        return Math.sqrt(sum)/(catAsRowNonZeroNum+centre.length);
    }

    public  static PartitionList kMeans(int k,PartitionList partitionList,CatAsRowList catAsRowList,int samplePrunedSize,float[][] kCentreLast){
        float[][] kCentre=getNewKCentre(k,partitionList,catAsRowList,samplePrunedSize);
        PartitionList clusterPartitionList=getClusterResult(k,catAsRowList,kCentre,samplePrunedSize);
        float loss=getLossSumOfCentre(kCentre,kCentreLast);

        if(loss==0){
            return clusterPartitionList;
        }
        else {
            return kMeans(k,clusterPartitionList,catAsRowList,samplePrunedSize,kCentre);
        }



    }

    public static PartitionList getClusterResult(int k,CatAsRowList catAsRowList,float[][] kCentre,int samplePrunedSize){
        PartitionList bestPartitionList=new PartitionList();
        // 构造k个partition
        for(int i=0;i<k;i++){
            Partition partition=new Partition();
            bestPartitionList.partitionList.add(partition);
        }

        // 遍历每一条数据，获取每条数据所属分类
        for(int i=0;i<catAsRowList.catAsRowList.size();i++){
            List<Integer> catAsRow=catAsRowList.catAsRowList.get(i).catAsRow;
            double minDist=Double.MAX_VALUE;
            int indexOfMinDist=0;
            for(int j=0;j<kCentre.length;j++){
                double tempDist=getDist(catAsRow,kCentre[j]);
                if( tempDist<minDist){
                    minDist=tempDist;
                    indexOfMinDist=j;
                }
            }
            bestPartitionList.partitionList.get(indexOfMinDist).partition.add(i);
        }
        return bestPartitionList;
    }


    public static float[][] getNewKCentre(int k,PartitionList partitionList,CatAsRowList catAsRowList,int samplePrunedSize){
        /**
        *@Description: 用来计算新的KCentre的值
        *@Param: [k, partitionList, catAsRowList, samplePrunedSize]
        *@return: float[][]
        *@Author: SongZhen
        *@date: 下午10:24 18-11-19
        */
        float[][] sumKCentre=new float[k][samplePrunedSize];
        for(int i=0;i<partitionList.partitionList.size();i++){
            Partition partition=partitionList.partitionList.get(i);
            for(int j=0;j<partition.partition.size();j++){
                int indexRow=partition.partition.get(j);   // 这里表示第10条数据
                for(int l=0;l<catAsRowList.catAsRowList.get(indexRow).catAsRow.size();l++){
                    int indexCol=catAsRowList.catAsRowList.get(indexRow).catAsRow.get(l);
                    sumKCentre[i][indexCol]+=1;
                }
            }
        }

        // 上面是求和，下面是求平均
        for(int i=0;i<sumKCentre.length;i++){
            for(int j=0;j<samplePrunedSize;j++){
                if(partitionList.partitionList.get(i).partition.size()!=0){
                    sumKCentre[i][j]=sumKCentre[i][j]/partitionList.partitionList.get(i).partition.size();
                }

            }
        }
        return sumKCentre;
    }

    public static float getLossSumOfCentre(float[][] kCentre,float[][] kCentreLast){
        float sum=0;
        for(int i=0;i<kCentre.length;i++){
            for(int j=0;j<kCentre[i].length;j++){
                sum+=Math.abs((kCentre[i][j]-kCentreLast[i][j]));
            }
        }
        return sum;
    }


}