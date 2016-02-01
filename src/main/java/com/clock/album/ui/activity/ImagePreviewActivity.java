package com.clock.album.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.clock.album.R;
import com.clock.album.entity.ImageInfo;
import com.clock.album.imageloader.ImageLoaderFactory;
import com.clock.album.imageloader.ImageLoaderWrapper;
import com.clock.album.ui.activity.base.BaseActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * 图片预览界面
 *
 * @author Clock
 * @since 2016-01-25
 */
public class ImagePreviewActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public final static String EXTRA_IMAGE_INFO_LIST = "ImageInfoList";
    public final static String EXTRA_IMAGE_INFO = "ImageInfo";

    public final static String EXTRA_NEW_IMAGE_LIST = "NewImageList";

    private ViewPager mPreviewViewPager;
    private PagerAdapter mPreviewPagerAdapter;
    private ViewPager.OnPageChangeListener mPreviewChangeListener;
    private TextView mTitleView;
    private CheckBox mImageSelectedBox;

    /**
     * 所有图片的列表
     */
    private List<ImageInfo> mPreviewImageInfoList;
    /**
     * 刚进入页面显示的图片
     */
    private ImageInfo mPreviewImageInfo;

    private ImageLoaderWrapper mImageLoaderWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        mImageLoaderWrapper = ImageLoaderFactory.getLoader(ImageLoaderFactory.UNIVERSAL_ANDROID_IMAGE_LOADER);

        mPreviewImageInfo = (ImageInfo) getIntent().getSerializableExtra(EXTRA_IMAGE_INFO);
        mPreviewImageInfoList = (List<ImageInfo>) getIntent().getSerializableExtra(EXTRA_IMAGE_INFO_LIST);

        initView();

    }

    private void initView() {

        mTitleView = (TextView) findViewById(R.id.tv_title);
        if (mPreviewImageInfo != null && mPreviewImageInfoList != null) {
            if (mPreviewImageInfoList.contains(mPreviewImageInfo)) {
                int imageIndex = mPreviewImageInfoList.indexOf(mPreviewImageInfo);
                setPositionToTitle(imageIndex);

            }
        }

        mImageSelectedBox = (CheckBox) findViewById(R.id.ckb_image_select);
        if (mPreviewImageInfo != null) {
            mImageSelectedBox.setChecked(mPreviewImageInfo.isSelected());
        }
        mImageSelectedBox.setOnCheckedChangeListener(this);

        mPreviewViewPager = (ViewPager) findViewById(R.id.gallery_viewpager);
        mPreviewPagerAdapter = new PreviewPagerAdapter();
        mPreviewViewPager.setAdapter(mPreviewPagerAdapter);
        if (mPreviewImageInfo != null && mPreviewImageInfoList != null && mPreviewImageInfoList.contains(mPreviewImageInfo)) {
            int initShowPosition = mPreviewImageInfoList.indexOf(mPreviewImageInfo);
            mPreviewViewPager.setCurrentItem(initShowPosition);
        }
        mPreviewChangeListener = new PreviewChangeListener();
        mPreviewViewPager.addOnPageChangeListener(mPreviewChangeListener);

        findViewById(R.id.iv_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.iv_back) {
            onBackPressed();

        }
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEW_IMAGE_LIST, (Serializable) mPreviewImageInfoList);
        setResult(Activity.RESULT_OK, data);
        super.onBackPressed();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mImageSelectedBox) {
            int currentPosition = mPreviewViewPager.getCurrentItem();
            ImageInfo imageInfo = mPreviewImageInfoList.get(currentPosition);
            imageInfo.setIsSelected(isChecked);
        }
    }

    /**
     * 相册适配器
     */
    private class PreviewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            if (mPreviewImageInfoList == null) {
                return 0;
            }
            return mPreviewImageInfoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            PhotoView galleryPhotoView = (PhotoView) view.findViewById(R.id.iv_show_image);
            galleryPhotoView.setScale(1.0f);//让图片在滑动过程中恢复回缩放操作前原图大小
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View galleryItemView = View.inflate(ImagePreviewActivity.this, R.layout.preview_image_item, null);

            ImageInfo imageInfo = mPreviewImageInfoList.get(position);
            PhotoView galleryPhotoView = (PhotoView) galleryItemView.findViewById(R.id.iv_show_image);
            ImageLoaderWrapper.DisplayOption displayOption = new ImageLoaderWrapper.DisplayOption();
            displayOption.loadErrorResId = R.mipmap.img_error;
            displayOption.loadingResId = R.mipmap.img_default;
            mImageLoaderWrapper.displayImage(galleryPhotoView, imageInfo.getImageFile(), displayOption);

            container.addView(galleryItemView);
            return galleryItemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    /**
     * 相册详情页面滑动监听
     */
    private class PreviewChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mImageSelectedBox.setOnCheckedChangeListener(null);//先反注册监听，避免重复更新选中的状态

            setPositionToTitle(position);
            ImageInfo imageInfo = mPreviewImageInfoList.get(position);
            mImageSelectedBox.setChecked(imageInfo.isSelected());

            mImageSelectedBox.setOnCheckedChangeListener(ImagePreviewActivity.this);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /**
     * 设置标题现实当前所处的位置
     *
     * @param position
     */
    private void setPositionToTitle(int position) {
        if (mPreviewImageInfoList != null) {
            String title = String.format(getString(R.string.image_index), position + 1, mPreviewImageInfoList.size());
            mTitleView.setText(title);
        }
    }

}