package ParaPartition;

import Data.DiskAccessTime;
import Global.Global;
import ParaStructure.Partitioning.AFMatrix;
import ParaStructure.Partitioning.Partition;
import ParaStructure.Partitioning.PartitionList;
import ParaStructure.Sample.SampleList;
import Util.AccessTimeTest;
import Util.Prune;

import java.util.ArrayList;
import java.util.List;



public class BatchBasedModelPartition {
    public static PartitionList getBestPartitionList(PartitionList batchPartitionList, SampleList sampleList){
        int batchDimSize=batchPartitionList.partitionList.size();
        int sparseDimSize=sampleList.sparseDimSize;
        float pruneRate=0.007f;
        List<Integer> prunedSparseDim=new ArrayList<Integer>();

        int prunedSparseDimSize=prunedSparseDim.size();
        PartitionList partitionList=  initPartition(prunedSparseDim);


        /*遍历数据集建立AF矩阵,返回最佳划分*/

        PartitionList bestPartitionList=new PartitionList();

        bestPartitionList=bestModelPartition(batchPartitionList,prunedSparseDim,partitionList);


        for(Partition partition: bestPartitionList.partitionList){
            if(partition.partition.size()>1){
                System.out.println(partition.partition);
            }
        }

        return bestPartitionList;

    }

    public static PartitionList initPartition(List<Integer> prunedSparseDim){
        /*初始化partitionList，让稀疏维度的每一维都划分成一个Partition*/
        PartitionList partitionList = new PartitionList();
        for(int i=0;i<prunedSparseDim.size();i++){
            Partition p=new Partition();
            p.partition.add(prunedSparseDim.get(i));
            partitionList.partitionList.add(p);
        }
        return partitionList;
    }

    public static PartitionList bestModelPartition(PartitionList batchPartitionList,List<Integer> prunedSparseDim,PartitionList partitionList){
        int partitionListSize=partitionList.partitionList.size();
        float[][] AF=new float[partitionListSize][partitionListSize];
        PartitionList bestPartitionList=new PartitionList();
        AFMatrix afMatrix=new AFMatrix();
        float[][] cost=new float[partitionListSize][partitionListSize];


        /*这里为剪枝后的维度构建map*/



        AF=buildAF(partitionList,batchPartitionList,prunedSparseDim);


        /*构建第一个AFMatrix*/

        afMatrix.AF=AF;
        afMatrix.partitionList=partitionList;
        afMatrix.costTime=new float[partitionListSize][partitionListSize];
        afMatrix.costTimeReduce=new float[partitionListSize][partitionListSize];

        /*
         * 开始计算最佳合并
         * 前面已经把初始的AF矩阵计算好了，后面就不需要遍历数据了，只需要遍历AF矩阵即可
         * 毫无疑问这个计算最佳划分函数肯定是一个递归函数
         * */
        bestPartitionList=computeBestMerge(afMatrix,batchPartitionList,prunedSparseDim);



        return bestPartitionList;
    }


    public static float[][] buildAF(PartitionList partitionList, PartitionList batchPartitionList, List<Integer> prunedSparseDim) {
        int partitionListSize = partitionList.partitionList.size();
        float[][] AF = new float[partitionListSize][partitionListSize];
        int batchPartitionListSize = batchPartitionList.partitionList.size();
        for (int i = 0; i < batchPartitionListSize; i++) {  //这是个大循环，在循环所有的数据集
            List<Integer> catNonzeroList = new ArrayList<Integer>();
            Partition batchPartition = batchPartitionList.partitionList.get(i);

            /*下面得到了剪枝后的每一个batch包含的要访问的维度*/
            for (int j = 0; j < batchPartition.partition.size(); j++) {  //catNonzeroList存储的是剪枝后每条数据包含的维度
                if (prunedSparseDim.contains(batchPartition.partition.get(j))) { //如果cat的属性不为0,且该维度在剪枝后的统计范围内
                    catNonzeroList.add(batchPartition.partition.get(j));
                }
            }

            /*下面计算batch包含了哪些partition*/
            List<Integer> catContainsPartition=new ArrayList<Integer>();
            for(int j=0;j<partitionListSize;j++){
                Partition partition=partitionList.partitionList.get(j);  // 判断是否该条batch数据包含partitionj
                for(int k=0;k<partition.partition.size();k++){  /*这是对于某个partition的所有元素进行遍历*/
                    if(catNonzeroList.contains(partition.partition.get(k))==false){
                        break;
                    }
                    else if(k==partition.partition.size()-1){
                        catContainsPartition.add(j);
                        AF[j][j]++;
                    }
                }
            }

            /*下面两两组合这些partition，得到AF*/
            for(int l=0;l<catContainsPartition.size();l++){
                for(int m=l+1;m<catContainsPartition.size();m++){
                    AF[catContainsPartition.get(l)][catContainsPartition.get(m)]++;
                    AF[catContainsPartition.get(m)][catContainsPartition.get(l)]++;
                }
            }



        }
        return AF;

    }


    public static PartitionList computeBestMerge(AFMatrix afMatrix, PartitionList batchPartitionList, List<Integer> prunedSparseDim){
        /*先进行第一次计算和合并*/
        float minGain=0f;
        int partitionListSize=afMatrix.partitionList.partitionList.size();
        for(int i=0;i<partitionListSize;i++){
            for(int j=0;j<partitionListSize;j++){
                if(i==j){
                    //前面的参数是磁盘访问的两个时间（seek和read），后面是partition[i]包含的Dim个数
                    afMatrix.costTime[i][i]=afMatrix.AF[i][i]*cost(afMatrix.partitionList.partitionList.get(i).partition.size());
                }
                else {
                    int mergePartitionSize=afMatrix.partitionList.partitionList.get(i).partition.size()+ afMatrix.partitionList.partitionList.get(j).partition.size();
                    afMatrix.costTime[i][j]=(afMatrix.AF[i][i]+afMatrix.AF[j][j]-afMatrix.AF[i][j])*cost(mergePartitionSize);
                }
            }

        }

        float maxTimeReduce=0;
        int pi=0;
        int pj=0;

        /*计算最大的时间成本Reduce，也就是最佳合并pi，pj*/
        for(int i=0;i<partitionListSize;i++){
            for(int j=0;j<partitionListSize;j++){
                if(i==j){
                    afMatrix.costTimeReduce[i][j]=0f;
                }
                else {
                    afMatrix.costTimeReduce[i][j]=afMatrix.costTime[i][i]+afMatrix.costTime[j][j]-afMatrix.costTime[i][j];
                    if(afMatrix.costTimeReduce[i][j]>maxTimeReduce){
                        maxTimeReduce=afMatrix.costTimeReduce[i][j];
                        pi=i;
                        pj=j;
                    }
                }
            }
        }

        /*重新构建partitionList，也就是合并之后的partitionList*/
        if(maxTimeReduce>minGain){
            int pjSize=getPiSize(afMatrix.partitionList.partitionList,pj);
            for(int i=0;i<pjSize;i++){
                afMatrix.partitionList.partitionList.get(pi).partition.add(afMatrix.partitionList.partitionList.get(pj).partition.get(i));
            }
            afMatrix.partitionList.partitionList.remove(pj);

        }
        else {
            return afMatrix.partitionList;
        }


        afMatrix.AF=buildAF(afMatrix.partitionList,batchPartitionList,prunedSparseDim);

        return computeBestMerge(afMatrix,batchPartitionList,prunedSparseDim);

    }




    /*获取划分i的大小*/
    public static int getPiSize(List<Partition> partitionList, int pi){
        return partitionList.get(pi).partition.size();
    }


    /*
     * 计算某个划分Pi的访问时间（seek and read）
     * @para diskAccessTime是磁盘访问的两个时间（seek和read），singlePartitionSize是partition[i]包含的Dim个数
     * @return 返回访问这个Pi的时间成本
     * */

    public static float cost( int singlePartitionSize){

        return (Global.diskAccessTime.seekSingleTime+singlePartitionSize*Global.diskAccessTime.readSingleDimTime);
    }
}
