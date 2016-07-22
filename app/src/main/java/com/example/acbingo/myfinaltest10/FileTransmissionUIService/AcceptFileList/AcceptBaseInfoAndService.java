package com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList;

import java.util.ArrayList;
import java.util.List;

/*
   启用时间 ： SendFileActivity的oncreate
   作用 ： 用来保存当前 需要传输的用户个数 ， 传输文件列表 ， 传输文件列表长度列表 ， 接受文件布尔矩阵（
           ma[i][j] = 0 ,代表用户i没有选择接受j号文件,若为1则意义相反）
           并提供一些供SendFileActivity获取这些信息的 get方法 和 一些 特殊 的转换函数，
           例如 ： 确认第J个文件是第i个用户要接受的第几个文件
 */
public class AcceptBaseInfoAndService {
      static  int N = 101;
      boolean maze[][];          // 接受文件布尔数组
      ArrayList<ArrayList<Integer>> reflect = new ArrayList<ArrayList<Integer>>();
                                  // reflect[i][j] 代表 第i个用户的第j个文件的对应于显示页面listview的第几个item
      ArrayList<String> allFileNameList;   // 传输文件名列表
      int AcceptCount;          // 用户个数 对应于 SendfileActivity里面的fragment的个数
      int FileCount;
      long len[];                // 所用文件的长度数组
      ArrayList<String> AccepterNameList;  //所用接受着的名字列表
      ArrayList<ArrayList<Long>> AccLenMaze = new ArrayList<ArrayList<Long>>();
                                  // 所有用户对所有文件已接受的长度存储矩阵
      ArrayList<ArrayList<Boolean>>  isCompletedMaze = new ArrayList<>();
                                  // isCompletedMazep[i][j] 代表用户i对文件j是否完成接收
      long ToatalAccLen;        //总共已经接受的长度
      long TotalLen;            // 总共需要接受的长度 = AccpterCount * allFileLen
      /*
          构造函数 ， 必须顺序传参 ， 传输文件列列表 ， 文件长度数组 ， 接受文件布尔数组 , 接受文件的用户的个数
      */
      public AcceptBaseInfoAndService(List<String> filelist , long[] filelen , boolean[][] ma , int acceptCountnt) {
          this.maze = ma;
          allFileNameList = new ArrayList<>();
          for(String te : filelist){
                allFileNameList.add(te);
          }
          this.len = filelen;
          this.AccepterNameList = new ArrayList<>();
          this.AcceptCount = acceptCountnt;
          this.ToatalAccLen = 0;
          this.FileCount = allFileNameList.size();
          this.TotalLen = 0;


          // 下面的代码用来构造映射列表 , 并初始化AccLenMaze矩阵 和 isCompletedMaze ， 并获取所有用户所有选中要接受的文件的长度总和
          for(int i = 0 ; i< acceptCountnt ; i++){

                 ArrayList<Integer> te = new ArrayList<>();
                 ArrayList<Long> te2 = new ArrayList<>();
                 ArrayList<Boolean> te3 = new ArrayList<>();
                 int cnt = 0;
                 for(int j = 0;  j < FileCount ; j++){

                      // maze[i][j] = true;
                       //  这里设置当前的设置模式为用户没有拒绝权利所以maze矩阵全设置为真

                       if(maze[i][j] == false){   //用户i为选择文件j
                            te.add(0);
                       }   else   {
                           te.add(cnt++);
                           TotalLen += len[j];
                       }
                       te2.add(Long.valueOf(0));
                       te3.add(false);
                 }
                 reflect.add(te);
                 AccLenMaze.add(te2);
                 isCompletedMaze.add(te3);
          }
      }

      // 设置接受用户名字列表
      public void setAccepterNameList(ArrayList<String> nameList){
           this.AccepterNameList = nameList;
      }

      /*
          获取第position个Accepter的name
      */
      public String getAccepterNameAt(int pos) {
          if (pos < AccepterNameList.size()) {
              return AccepterNameList.get(pos);
          }
          return "hehe";
      }
      /*
         reflect[i][j] 代表 第i个用户的第j个文件的对应于显示页面listview的第几个item
         参数 i 代表接受用户的Id , 参数 j 代表第几个文件
       */
      public int getFileShowPositionByAccepterIdAndFileId(int i,int j) {
          return reflect.get(i).get(j);
      }

      /*
          j 为文件在文件列表中的位置 同上
          acclen 代表这个文件已经接受了多长
          该函数 完成两个任务 第一 更新用户i 的文件j的接受进度 , 从而更新总进度
          第二 ： 返回该文件的接受进度
      */
      public int getPercentByFileAcceptLengthAndUpdateAccLen(int i , int j , long acclen){
          ToatalAccLen  -= this.AccLenMaze.get(i).get(j);
          this.AccLenMaze.get(i).set(j , acclen);
          ToatalAccLen  += acclen;
          return (int)(1.0 * acclen / len[j] * 100);
      }
      /*
          获取总共接受的进度  TotalAcceptLength = TotalAcclen / ToTal
      */
      public int getTotalAcceptPercent(){
          //由于存在重传机制，接收到的字节数可能大于总文件长度，所以要调整一下
          return Math.min((int)(1.0 * ToatalAccLen  / TotalLen * 100) , 100);
      }
      /*
          获取发送者拟发送的文件总个数
      */
      public int getFileCount(){
            return allFileNameList.size();
      }
      /*
          通过用户名来获取这个用户到底选择接受的文件个数
      */
      public int getAcceptFileCountByAccepterId(int pos){
            int cou = 0;
            for(boolean x : maze[pos]){
                 if(x == true){
                     cou++;
                 }
            }
            return  0;
      }
     /*
          判断Accepter i 是否选择接受文件 j
     */
    public boolean isAccepterChooseFile(int i, int j){
            return maze[i][j];
    }

    public String getFileNameAt(int pos){
            return this.allFileNameList.get(pos);
    }

    public long getFileLength(int pos){return this.len[pos];}

    /*
       设置用户i的对文件j完成了接受
    */
    public void setFileTransCompletedByAccepterIdAndFileId(int i,int j){
           isCompletedMaze.get(i).set(j,true);
    }
    public boolean isAccpterCompleteAcceptFile(int i,int j){
             return isCompletedMaze.get(i).get(j);
    }
}
