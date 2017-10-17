package com.jrdcom.wearable.smartband2.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenSnapshotUtil {

//	private static final String TAG = "ScreenSnapshotUtil";
	private Context mContext;

	private static final String mSnapshotName = "snapshot.png";

	public ScreenSnapshotUtil(Context context) {
		mContext = context;
	}

	public String createBitmap(LinearLayout view, int signView, int bg) {
		Bitmap newb = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Config.ARGB_8888);
		if(newb == null){
			return null;
		}
		Canvas canvas = new Canvas(newb);
		view.draw(canvas);
		Bitmap sign = BitmapFactory.decodeResource(mContext.getResources(),
				signView);
		Bitmap bgBitmap = BitmapFactory.decodeResource(mContext.getResources(),
				bg);
		Bitmap a = getBitmap(newb, sign, bgBitmap);
		return saveMyBitmap(a);

	}

	private Bitmap getBitmap(Bitmap src, Bitmap watermark, Bitmap bg) {
		int w = src.getWidth();
		int h = src.getHeight();
		// create the new blank bitmap
		int width = w;
		int height = h;
		if(width <= 0 || height <= 0){
			return null;
		}
		Bitmap newb = Bitmap.createBitmap(width, height,Config.ARGB_8888);
		if(newb == null){
			return null;
		}
		// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv = new Canvas(newb);
		cv.drawBitmap(bg, 0, 0, null);
		Rect rect = new Rect();
		Rect dst = new Rect();
		rect.set(0, 0, bg.getWidth(), bg.getHeight());
		dst.set(0, 0, width, height);
		cv.drawBitmap(bg, rect, dst, null);
		// draw src into
		cv.drawBitmap(src, 24, 24, null);// 在 0，0坐标开始画入src
		// draw watermark into
//		cv.drawBitmap(watermark, (w - ww) / 2 + padding, h + padding, null);// 在src的右下角画入水印
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		cv.restore();// 存储
		return newb;
	}

	// 保存图片
	public String saveMyBitmap(Bitmap bmp) {
		if(bmp == null){
			return null;
		}
		FileOutputStream fos = null;
		File snapshotFile = null;
		try {
			snapshotFile = mContext.getFileStreamPath(mSnapshotName);
			if (snapshotFile.exists()) {
				snapshotFile.delete();
			}
			fos = mContext.openFileOutput(mSnapshotName,
					Context.MODE_WORLD_READABLE);
			bmp.compress(Bitmap.CompressFormat.PNG, 60, fos);
		} catch (FileNotFoundException e) {
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
				}
			}
		}
		return mContext.getFileStreamPath(mSnapshotName).getAbsolutePath();
	}

}
