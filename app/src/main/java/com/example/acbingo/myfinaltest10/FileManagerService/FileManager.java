package com.example.acbingo.myfinaltest10.FileManagerService;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.acbingo.myfinaltest10.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FileManager {
	public static final String TAG = "FileManage";
	public static final String IS_DIR = "isDir";
	public static final String FILE = "FILE";
	public static final String IMAGE = "image";
	public static final String TITLE = "title";
	public static final String COUNT = "count";
	public static final String SIZE = "size";
	public static final String TIME = "time";
	public static final String IS_CHECKED = "isChecked";
	FileManagerMainActivity mFileManagerMainActivity;

	public FileManager(FileManagerMainActivity fileManagerMainActivity) {
		mFileManagerMainActivity = fileManagerMainActivity;
	}

	public void copy(ArrayList<HashMap<String, Object>> selectedItem,
			String targetFolder) {
		for (HashMap<String, Object> item : selectedItem) {
			File srcFile = (File) item.get(FileManager.FILE);
			File descFile = new File(targetFolder + File.separator
					+ srcFile.getName());
			copyFile(srcFile, descFile);
			Print("src:\t"+srcFile.getAbsolutePath()+"\tdesc:\t"+descFile.getAbsolutePath());
		}
	}
	
	
	
	
	
	
	
	public void cut(ArrayList<HashMap<String, Object>> selectedItem,
			String targetFolder){
		for (HashMap<String, Object> item : selectedItem) {
			File srcFile = (File) item.get(FileManager.FILE);
			File descFile = new File(targetFolder + File.separator
					+ srcFile.getName());
			srcFile.renameTo(descFile);
		}
	}
    /*
        copyFile 函数只用来判断源文件 和 目标文件copy 之间的合法性问题
        例如 ： 如果目标文件已经存在，那么需要让用户判断是否覆盖
    */
	public void copyFile(final File sourceFile, final File targetFile) {
		if (sourceFile == null || targetFile == null)
			return;
		if(sourceFile.equals(targetFile))
		    return;
		if(targetFile.exists()){
		    AlertDialog.Builder dialog = new AlertDialog.Builder(mFileManagerMainActivity);
            dialog.setIcon(android.R.drawable.ic_dialog_alert);
            dialog.setMessage(mFileManagerMainActivity.getResources().getString(R.string.copy_file_exist_info, targetFile.getName()));
            dialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            copy(sourceFile,targetFile);
                        }
                    });
            dialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            dialog.show();
			return ;
		}
		copy(sourceFile,targetFile);
	}
	/*
	    递归copy程序，每次copy之前先交由copyFile函数做安全判断。
	*/
	public void copy(File sourceFile, File targetFile){
	    if (sourceFile.isFile()) {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(sourceFile);
                out = new FileOutputStream(targetFile);
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = in.read(b)) != -1) {
                    out.write(b, 0, len);
                }
                out.flush();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    if (in != null)
                        in.close();
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            targetFile.mkdir();
            File[] files = sourceFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                copyFile(files[i], new File(targetFile+ File.separator+files[i].getName()));
            }
        }
	}

	/*
	   返回文件的大小：
	   如果是目录则返回文件
	   File#length 表示是文件的大小，与文件系统相关。
       而 InputSteam#available 表示是在输入流中读取数据直接阻塞时的大小。
	*/
	public long fileSize(File file) {
	if (file == null) {
		Print("fileSize(File file),file=null");
		return 0;
	}
	if (file.exists()) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			long size = 0;
			for (File f : files) {
				size += fileSize(f);
			}
			return size;
		} else {
			try {
				return new FileInputStream(file).available();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	return 0;
}

	public void delete(ArrayList<HashMap<String, Object>> selectedItem) {
		for (HashMap<String, Object> item : selectedItem) {
			delete((File) item.get(FileManager.FILE));
		}
	}

	public void delete(String path) {
		if (path == null) {
			Print("delete(String FILE),FILE=null");
			return;
		}
		delete(new File(path));
	}

	/*
	    递归删除文件夹，递归的边界都加上了弹窗判断提示。
	    先删除子文件，然后再次调用该函数来删除自己
	*/
	public void delete(File f) {
		if (f == null) {
			Print("delete(File f),file=null");
			return;
		}
		if (f.exists()) {
			if (f.isDirectory()) {
				if (f.listFiles().length == 0) {
					if (!f.delete()) {
						AlertDialog.Builder warningDialog = new AlertDialog.Builder(
								mFileManagerMainActivity);
						warningDialog
								.setIcon(android.R.drawable.ic_dialog_alert);
						warningDialog.setMessage(String.format(mFileManagerMainActivity
								.getResources()
								.getString(R.string.delete_folder_err),f.getName().toString()));
						warningDialog.setPositiveButton("OK", null);
						warningDialog.show();
					}
				} else {
					File delFile[] = f.listFiles();
					int i = f.listFiles().length;
					for (int j = 0; j < i; j++)
						delete(delFile[j]);
					delete(f);
				}
			} else {
				if (!f.delete()) {
					AlertDialog.Builder warningDialog = new AlertDialog.Builder(
							mFileManagerMainActivity);
					warningDialog.setIcon(android.R.drawable.ic_dialog_alert);
					warningDialog.setMessage(String.format(mFileManagerMainActivity
							.getResources().getString(R.string.delete_file_err),f.getName().toString()));
					warningDialog.setPositiveButton("OK", null);
					warningDialog.show();
				}
			}
		}
	}

	public ArrayList<HashMap<String, Object>> getFilesList(String FILE) {
		if (FILE == null) {
			Print("The FILE (" + FILE + ") in getFilesList is null");
			return null;
		}
		return getFilesList(new File(FILE));
	}

	// 获取路径path下的所有文件信息
	public ArrayList<HashMap<String, Object>> getFilesList(File file) {
		if (file == null) {
			Print("The file (" + file + ") is not exist!");
			return null;
		}
		File[] files = file.listFiles();
		if (files == null) {
			Print("The files under dir(" + file.getAbsolutePath()
					+ ") is not null!");
			return null;
		}
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < files.length; i++) {
			if (isDisplay(files[i])) {
				if (files[i].isDirectory()) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(IS_DIR, true);
					map.put(FILE, files[i]);
					map.put(IMAGE, R.drawable.folder);
					map.put(TITLE, files[i].getName());
					if (files[i].listFiles() == null)
						map.put(COUNT, "(" + 0 + ")");
					else
						map.put(COUNT, "(" + getDirectoryCount(files[i]) + ")");
					map.put(TIME, new Date(files[i].lastModified()));
					map.put(IS_CHECKED, false);
					listItem.add(map);
				} else {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(IS_DIR, false);
					map.put(FILE, files[i]);
					Bitmap bmp = getThumbnail(files[i].getAbsolutePath());
					if (bmp == null)
						map.put(IMAGE, R.drawable.file_icon_default);
					else
						map.put(IMAGE, bmp);
					map.put(TITLE, files[i].getName());
					map.put(COUNT, "");
					map.put(TIME, new Date(files[i].lastModified()));
					try {
						long size = new FileInputStream(files[i]).available();
						map.put(SIZE, size);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					map.put(IS_CHECKED, false);
					listItem.add(map);
				}
			}
		}
		return listItem;
	}

	public int getDirectoryCount(File file) {
		int result = 0;
		if (file == null)
			return result;
		File[] files = file.listFiles();
		if (files == null)
			return result;
		for (int i = 0; i < files.length; i++) {
			if (isDisplay(files[i]))
				result++;
		}
		return result;
	}

	public static void Print(Object obj) {
		FileManagerMainActivity.Print(TAG, obj);
	}

	public boolean isDisplay(File file) {
		if (!mFileManagerMainActivity.isDisplayAll()) {
			if (file.getName().startsWith(".") || file.isHidden())
				return false;
			return true;
		} else
			return true;
	}

	public boolean newFolder(String FILE) {
		File file = new File(FILE);
		return file.mkdir();
	}

	public boolean rename(String oldName, String newName) {
		File file1 = new File(oldName);
		File file2 = new File(newName);
		return file1.renameTo(file2);
	}

	public Bitmap getThumbnail(String filePath) {
		int width = 48;
		int height = 48;
		File file = new File(filePath);
		if (!file.exists())
			return null;
		String end = file
				.getName()
				.substring(file.getName().lastIndexOf(".") + 1,
						file.getName().length()).toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			return null;
		} else if (end.equals("3gp") || end.equals("mp4")) {
			return getVideoThumbnail(filePath, width, height,
					MediaStore.Images.Thumbnails.MICRO_KIND);
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			return getImageThumbnail(filePath, width, height);
			// }else if(end.equals("apk")){
			// return getApkFileIntent(filePath);
			// }else if(end.equals("ppt")){
			// return getPptFileIntent(filePath);
			// }else if(end.equals("xls")){
			// return getExcelFileIntent(filePath);
			// }else if(end.equals("doc")){
			// return getWordFileIntent(filePath);
			// }else if(end.equals("pdf")){
			// return getPdfFileIntent(filePath);
			// }else if(end.equals("chm")){
			// return getChmFileIntent(filePath);
			// }else if(end.equals("txt")){
			// return getTextFileIntent(filePath,false);
			// }else{
			// return getAllIntent(filePath);
		}
		return null;
	}

	/**
	 * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * 
	 * @param videoPath
	 *            视频的路径
	 * @param width
	 *            指定输出视频缩略图的宽度
	 * @param height
	 *            指定输出视频缩略图的高度度
	 * @param kind
	 *            参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	private Bitmap getVideoThumbnail(String videoPath, int width, int height,
									 int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		System.out.println("w" + bitmap.getWidth());
		System.out.println("h" + bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 1.
	 * 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 2.
	 * 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
	 * 
	 * @param imagePath
	 *            图像的路径
	 * @param width
	 *            指定输出图像的宽度
	 * @param height
	 *            指定输出图像的高度
	 * @return 生成的缩略图
	 */
	private Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	public Intent openFile(String filePath) {

		File file = new File(filePath);
		if (!file.exists())
			return null;
		String end = file
				.getName()
				.substring(file.getName().lastIndexOf(".") + 1,
						file.getName().length()).toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			return getAudioFileIntent(filePath);
		} else if (end.equals("3gp") || end.equals("mp4")) {
			return getAudioFileIntent(filePath);
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			return getImageFileIntent(filePath);
		} else if (end.equals("apk")) {
			return getApkFileIntent(filePath);
		} else if (end.equals("ppt")) {
			return getPptFileIntent(filePath);
		} else if (end.equals("xls")) {
			return getExcelFileIntent(filePath);
		} else if (end.equals("doc")) {
			return getWordFileIntent(filePath);
		} else if (end.equals("pdf")) {
			return getPdfFileIntent(filePath);
		} else if (end.equals("chm")) {
			return getChmFileIntent(filePath);
		} else if (end.equals("txt")) {
			return getTextFileIntent(filePath, false);
		} else {
			return getAllIntent(filePath);
		}
	}

	public static Intent getAllIntent(String param) {

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "*/*");
		return intent;
	}

	public static Intent getApkFileIntent(String param) {

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		return intent;
	}

	public static Intent getVideoFileIntent(String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "video/*");
		return intent;
	}

	public static Intent getAudioFileIntent(String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "audio/*");
		return intent;
	}

	public static Intent getHtmlFileIntent(String param) {

		Uri uri = Uri.parse(param).buildUpon()
				.encodedAuthority("com.android.htmlfileprovider")
				.scheme("content").encodedPath(param).build();
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setDataAndType(uri, "text/html");
		return intent;
	}

	public static Intent getImageFileIntent(String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "image/*");
		return intent;
	}

	public static Intent getPptFileIntent(String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		return intent;
	}

	public static Intent getExcelFileIntent(String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-excel");
		return intent;
	}

	public static Intent getWordFileIntent(String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/msword");
		return intent;
	}

	public static Intent getChmFileIntent(String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/x-chm");
		return intent;
	}

	public static Intent getTextFileIntent(String param, boolean paramBoolean) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (paramBoolean) {
			Uri uri1 = Uri.parse(param);
			intent.setDataAndType(uri1, "text/plain");
		} else {
			Uri uri2 = Uri.fromFile(new File(param));
			intent.setDataAndType(uri2, "text/plain");
		}
		return intent;
	}

	public static Intent getPdfFileIntent(String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}

}
