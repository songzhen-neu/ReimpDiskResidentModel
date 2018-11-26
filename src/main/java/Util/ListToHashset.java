package Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @program: CtrForBigModel
 * @description: 将List结构转换成hashset结构
 * @author: SongZhen
 * @create: 2018-11-19 21:37
 */
public class ListToHashset {

    public static Set<Integer> listToHashSetInt(List<Integer> list){
        /**
        *@Description: 给定一个list<Integer>转化成HashSet<Integer>
        *@Param: [list]
        *@return: java.util.Set<java.lang.Integer>
        *@Author: SongZhen
        *@date: 下午9:41 18-11-19
        */
        Set<Integer> hashSet=new HashSet<Integer>();
        for(int i=0;i<list.size();i++){
            hashSet.add(list.get(i));
        }
        return hashSet;
    }
}