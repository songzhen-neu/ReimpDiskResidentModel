package ParaPartition;

import Data.DiskAccessTime;
import Global.Global;
import ParaStructure.Partitioning.AFMatrix;
import ParaStructure.Partitioning.Partition;
import ParaStructure.Partitioning.PartitionList;
import ParaStructure.Sample.SampleList;


import java.util.ArrayList;
import java.util.List;

public class ModelPartition {
    public static PartitionList modelPartition(SampleList sampleList,List<Integer> prunedSparseDim){
        int sparseDimSize= sampleList.sparseDimSize;

        int prunedSparseDimSize=prunedSparseDim.size();
        PartitionList partitionList=  initPartition(prunedSparseDim);


        /*遍历数据集建立AF矩阵,返回最佳划分*/

        PartitionList bestPartitionList=new PartitionList();

        bestPartitionList=bestModelPartition(sampleList,prunedSparseDim,partitionList);


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

    public static PartitionList bestModelPartition(SampleList sampleList, List<Integer> prunedSparseDim, PartitionList partitionList){
        int sampleListSize=sampleList.sampleListSize;
        int partitionListSize=partitionList.partitionList.size();
        float[][] AF=new float[partitionListSize][partitionListSize];
        PartitionList bestPartitionList=new PartitionList();
        AFMatrix afMatrix=new AFMatrix();
        float[][] cost=new float[partitionListSize][partitionListSize];


        /*这里为剪枝后的维度构建map*/



        AF=buildAF(partitionList,sampleList,prunedSparseDim);


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
        bestPartitionList=computeBestMerge(afMatrix,sampleList,prunedSparseDim);



        return bestPartitionList;
    }

    private static float[][] buildAF(PartitionList partitionList, SampleList sampleList, List<Integer> prunedSparseDim) {
        int sampleListSize = sampleList.sampleListSize;
        int catSize ;
        int partitionListSize = partitionList.partitionList.size();
        float[][] AF = new float[partitionListSize][partitionListSize];

        for (int i = 0; i < sampleListSize; i++) {  //这是个大循环，在循环所有的数据集

            List<Integer> catNonzeroList = new ArrayList<Integer>();
            catSize=sampleList.sampleList.get(i).cat.length;
            for (int j = 0; j < catSize; j++) {  //这个两层循环是遍历所有数据的所有cat维度
                int[] cat = sampleList.sampleList.get(i).cat;
                if (cat[j] != -1 && prunedSparseDim.contains(cat[j])) { //如果cat的属性不为0,且该维度在剪枝后的统计范围内
//                    AF[prunedSparseDimMap.get(cat[j])][prunedSparseDimMap.get(cat[j])]--;   //这个不定义变量了，cat[j]就是不为0的稀疏维度
                    catNonzeroList.add(cat[j]);
                }
            }
            /*下面是两两Partition组合，构建AF矩阵*/
//            for (int l = 0; l < catNonzeroList.size(); l++) {
//                for (int m = 0; m < catNonzeroList.size(); m++) {
//                    int lMapped = catNonzeroList.get(l);
//                    int mMapped = catNonzeroList.get(m);
//                    AF[prunedSparseDimMap.get(lMapped)][prunedSparseDimMap.get(mMapped)]++;
//                    AF[prunedSparseDimMap.get(mMapped)][prunedSparseDimMap.get(lMapped)]++;
//                }
//            }

            /*如果这一条数据的cat属性能够组合出来Partition，就说明这个partition在这条数据中出现了*/
            List<Integer> catContainsPartition=new ArrayList<Integer>();
            for(int l=0;l<partitionListSize;l++){
                Partition partition=partitionList.partitionList.get(l);
                for(int m=0;m<partition.partition.size();m++){
                    if(catNonzeroList.contains(partition.partition.get(m))){
                        if(m==(partition.partition.size()-1)){
                            catContainsPartition.add(l);
                        }
                    }
                    else {
                        break;
                    }
                }
            }


            for(int l:catContainsPartition){
                for(int m:catContainsPartition){
                    AF[l][m]++;
                }
            }

        }
        return AF;
    }



    private static PartitionList computeBestMerge(AFMatrix afMatrix, SampleList sampleList, List<Integer> prunedSparseDim){
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


        afMatrix.AF=buildAF(afMatrix.partitionList,sampleList,prunedSparseDim);

        return computeBestMerge(afMatrix,sampleList,prunedSparseDim);

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
