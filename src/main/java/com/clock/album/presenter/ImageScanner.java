package com.clock.album.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.clock.album.entity.AlbumInfo;
import com.clock.album.ui.interaction.ImageScannerInteraction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 图片扫描器
 * <p/>
 * Created by Clock on 2016/1/20.
 */
public class ImageScanner implements ImageScannerPresenter {

    private final static String TAG = ImageScanner.class.getSimpleName();
    /**
     * Loader的唯一ID号
     */
    private final static int IMAGE_LOADER_ID = 1000;
    /**
     * 加载数据的映射
     */
    private final static String[] IMAGE_PROJECTION = new String[]{
            MediaStore.Images.Media.DATA,//图片路径
            MediaStore.Images.Media.DISPLAY_NAME,//图片文件名，包括后缀名
            MediaStore.Images.Media.TITLE//图片文件名，不包含后缀
    };

    private Context mContext;

    private Handler mRefreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ImageMessage imageMessage = (ImageMessage) msg.obj;
            if (imageMessage != null) {

            } else {
                super.handleMessage(msg);
            }
        }
    };

    public ImageScanner(Context context) {
        this.mContext = context;
    }

    @Override
    public void scanExternalStorage(LoaderManager loaderManager, ImageScannerInteraction interaction) {
        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = createImageLoaderCallbacks(interaction);
        loaderManager.initLoader(IMAGE_LOADER_ID, null, loaderCallbacks);//初始化指定id的Loader
    }

    private LoaderManager.LoaderCallbacks<Cursor> createImageLoaderCallbacks(final ImageScannerInteraction interaction) {

        LoaderManager.LoaderCallbacks<Cursor> imageLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Log.i(TAG, "-----onCreateLoader-----");
                CursorLoader imageCursorLoader = new CursorLoader(mContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
                return imageCursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                Log.i(TAG, "-----onLoadFinished-----");
                int dataColumnIndex = data.getColumnIndex(MediaStore.Images.Media.DATA);
                //int displayNameColumnIndex = data.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                //int titleColumnIndex = data.getColumnIndex(MediaStore.Images.Media.TITLE);
                ArrayList<File> albumFolderList = new ArrayList<>();
                HashMap<String, ArrayList<File>> albumImageListMap = new HashMap<>();
                while (data.moveToNext()) {
                    File imageFile = new File(data.getString(dataColumnIndex));//图片文件
                    File albumFolder = imageFile.getParentFile();//图片目录
                    if (!albumFolderList.contains(albumFolder)) {
                        albumFolderList.add(albumFolder);
                    }
                    String albumPath = albumFolder.getAbsolutePath();
                    ArrayList<File> albumImageFiles = albumImageListMap.get(albumPath);
                    if (albumImageFiles == null) {
                        albumImageFiles = new ArrayList<>();
                        albumImageListMap.put(albumPath, albumImageFiles);
                    }
                    albumImageFiles.add(imageFile);//添加到对应的相册目录下面
                }

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                Log.i(TAG, "-----onLoaderReset-----");
            }
        };
        return imageLoaderCallbacks;
    }

    private static class ImageMessage {
        /**
         * 系统所有有图片的文件夹
         */
        ArrayList<File> albumFolderList;
        /**
         * 每个有图片文件夹下面所包含的图片
         */
        HashMap<String, ArrayList<File>> albumImageListMap;
        /**
         * 图片界面更新接口
         */
        ImageScannerInteraction interaction;
    }
}
