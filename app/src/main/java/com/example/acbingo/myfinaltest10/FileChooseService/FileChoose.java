package com.example.acbingo.myfinaltest10.FileChooseService;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by tfuty on 2016-05-28.
 * 这是一个单例类，在choosefileactivity里第一次创建并实例化。其它类通过访问filechooseinstance获取实例
 */
public class FileChoose {
    private static ArrayList<File> fileChooseArrayList = new ArrayList<File>();
    private static FileChoose fileChooseInstance;

    private FileChoose(){

    }

    /**
     * Get instance file choose.
     *
     * @return the file choose
     */
    public static FileChoose getInstance(){
        if (fileChooseInstance == null){
            fileChooseInstance = new FileChoose();
        }
        return fileChooseInstance;
    }

    /**
     * Get file choose array list array list.
     *
     * @return the array list
     */
    public static ArrayList<File> getFileChooseArrayList(){
        return fileChooseArrayList;
    }

    /**
     * Get file choose count int.
     *
     * @return the int
     */
    public static int getFileChooseCount(){
        return fileChooseArrayList.size();
    }

    /**
     * Add file.
     *
     * @param file the add file
     */
    public void addFile(File file){
        fileChooseArrayList.add(file);
    }

    /**
     * Add file.
     *
     * @param path the add file's absolutely path
     */
    public void addFile(String path){
        fileChooseArrayList.add(new File(path));
    }

    /**
     * Delete file.
     *
     * @param file the deletefile
     */
    public void deleteFile(File file){
        for (int i =0 ;i<fileChooseArrayList.size();i++){
            File f = fileChooseArrayList.get(i);
            if (f.equals(file)){
                fileChooseArrayList.remove(i);
                break;
            }
        }
    }
}
