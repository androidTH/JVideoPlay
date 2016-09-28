package com.example.min.jvideoplay.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    /**
     * sd卡的根目录
     */
    private static String mSdRootPath = Environment.getExternalStorageDirectory().getPath();
    /**
     * 手机的缓存根目录
     */
    private static String mDataRootPath = null;
    /**
     * 保存Image的目录名
     */
    private final static String FOLDER_NAME = "/Xingliao";

    private final static String IMAGE_NAME = "/cache";

    public static final String SDPATH = Environment.getExternalStorageDirectory() + FOLDER_NAME;
    public static File updateDir;
    public static File updateFile;
    public static String videoPath;
    public static String[]  videoPaths=new String[1];

    public FileUtils(Context context) {
        mDataRootPath = context.getCacheDir().getPath();
    }

    public String makeAppDir() {
        String path = getStorageDirectory();
        File folderFile = new File(path);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        path = path + IMAGE_NAME;
        folderFile = new File(path);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        return path;
    }

    public static boolean isSDCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取视频录制缓存路径
     * @param context
     * @return
     */
    public static String getRecorderDirectory(Context context){
        File appCacheDir = null;
        if(isSDCardMounted()) {
            appCacheDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        }

        if(appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if(appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/Movies/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir.getAbsolutePath();
    }

    /**
     * 获取视频缓存路径
     * @param context
     * @return
     */
    public static String getCacheDirectory(Context context) {
        File appCacheDir = null;

        if(isSDCardMounted()) {
            appCacheDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if(appCacheDir == null){
                appCacheDir = getExternalCacheDir(context);
            }
        }

        if(appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if(appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir.getAbsolutePath();
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "Download");
        if(!appCacheDir.exists() && !appCacheDir.mkdirs()) {
            return null;
        } else {
            return appCacheDir;
        }
    }


    /**
     * 获取储存Image的目录
     *
     * @return
     */
    private String getStorageDirectory() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED) ? mSdRootPath + FOLDER_NAME
                : mDataRootPath + FOLDER_NAME;
    }

    /**
     * 将String数据存为文件
     */
    public static File writeToFileFromString(String path, String content) {
        byte[] b = content.getBytes();
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(path);
            createOrExistsFile(file);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }

    public static boolean createOrExistsFile(File file) {
        if (file == null)
            return false;
        boolean result = false;
        if (isFileExists(file) && isFile(file)) {
            // 判断文件是否存在且为文件，如果存在结果为true
            return true;
        }
        // 如果文件不存在，创建文件
        // 先创建文件夹，否则不会成功
        File parentFile = file.getParentFile();
        if (!createOrExistsFolder(parentFile)) {
            // 如果父文件夹创建不成功，返回false
            return false;
        }
        try {
            if (file.createNewFile()) {
                // 创建成功返回true
                result = true;
            } else {
                // 创建失败返回false
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * 保存Image的方法，有sd卡存储到sd卡，没有就存储到手机目录
     *
     * @param fileName
     * @param bitmap
     * @throws IOException
     */
    public void savaBitmap(String fileName, Bitmap bitmap) throws IOException {
        if (bitmap == null) {
            return;
        }
        String path = getStorageDirectory();
        File folderFile = new File(path);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        path = path + IMAGE_NAME;
        folderFile = new File(path);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }

        File file = new File(path + File.separator + fileName);
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
    }

    /**
     * 从手机或者sd卡获取Bitmap
     *
     * @param fileName
     * @return
     */
    public Bitmap getBitmap(String fileName) {
        return BitmapFactory.decodeFile(getStorageDirectory() + File.separator
                + fileName);
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @return
     */
    public boolean isLocalFileExists(String fileName) {
        return new File(getStorageDirectory() + File.separator + fileName)
                .exists();
    }


    public static boolean isFileExists(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            File file = new File(fileName);
            return isFileExists(file);
        } else {
            return false;
        }
    }

    /**
     * 获取文件的大小
     *
     * @param fileName
     * @return
     */
    public long getFileSize(String fileName) {
        return new File(getStorageDirectory() + File.separator + fileName)
                .length();
    }

    /**
     * 删除SD卡或者手机的缓存图片和目录
     */
    public void deleteFile() {
        File dirFile = new File(getStorageDirectory());
        if (!dirFile.exists()) {
            return;
        }
        if (dirFile.isDirectory()) {
            String[] children = dirFile.list();
            for (int i = 0; i < children.length; i++) {
                new File(dirFile, children[i]).delete();
            }
        }

        dirFile.delete();
    }

    public static boolean createOrExistsFolder(File file) {
        if (file == null)
            return false;
        boolean result = false;

        if (isFileExists(file) && isDirectory(file)) {
            // 如果file存在且是文件夹，返回true
            return true;
        }
        // 如果文件夹不存在，创建文件夹
        if (file.mkdirs()) {
            // 创建成功返回true
            result = true;
        } else {
            // 创建失败返回false
            result = false;
        }
        return result;
    }

    public static boolean createOrExistsFolder(String fileName) {
        if (fileName == null || (fileName = fileName.trim()).equals("")) {
            return false;
        }
        File file = new File(fileName);
        return createOrExistsFolder(file);
    }


    public static boolean isFileExists(File file) {
        if (file != null) {
            return file.exists();
        } else {
            return false;
        }
    }

    public static boolean isFile(File file) {
        if (file != null) {
            return file.isFile();
        } else {
            return false;
        }
    }

    public static boolean isDirectory(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            File file = new File(fileName);
            return isDirectory(file);
        } else {
            return false;
        }

    }

    public static boolean isDirectory(File file) {
        if (file != null) {
            return file.isDirectory();
        } else {
            return false;
        }
    }

    public static String readStringFromFile(String path) {
        FileReader fr = null;
        BufferedReader br = null;
        String readline = "";
        try {
            fr = new FileReader(path);
            //可以换成工程目录下的其他文本文件
            StringBuffer sb = new StringBuffer();
            br = new BufferedReader(fr);

            while ((readline = br.readLine()) != null) {
                sb.append(readline);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 删除一个文件
     *
     * @param filePath 要删除的文件路径名
     * @return true if this file was deleted, false otherwise
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建一个文件，创建成功返回true
     *
     * @param filePath
     * @return
     */
    public static boolean createFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                return file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }



    public static void writeImage(Bitmap bitmap, String destPath, int quality) {
        try {
            deleteFile(destPath);
            if (createFile(destPath)) {
                FileOutputStream out = new FileOutputStream(destPath);
                if (bitmap.compress(CompressFormat.JPEG, quality, out)) {
                    out.flush();
                    out.close();
                    out = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
