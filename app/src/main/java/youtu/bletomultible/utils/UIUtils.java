package youtu.bletomultible.utils;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

import youtu.bletomultible.MyApplication;


public class UIUtils {
	/**
	 * @return	全局的上下文环境
	 */
	public static Context getContext(){
		return MyApplication.getContext();
	}



	/**
	 * 布局文件转换成view对象方法
	 * @param layoutId	布局文件id
	 * @return	布局文件转换成的view对象
	 */
	public static View inflate(int layoutId){
		return View.inflate(getContext(), layoutId, null);
	}

	/**
	 * @return	返回资源文件夹对象方法
	 */
	public static Resources getResources(){
		return getContext().getResources();
	}

	/**
	 * @param stringId	字符串在xml中对应R文件中的id
	 * @return	string.xml某节点,对应的值
	 */
	public static String getString(int stringId){
		return getResources().getString(stringId);
	}

	/**
	 * @param stringArrayId	字符串数组在xml中对应R文件中的id
	 * @return	节点对应的内容
	 */
	public static String[] getStringArray(int stringArrayId){
		return getResources().getStringArray(stringArrayId);
	}

	//像素密度决定比例关系
	//1:0.75
	//1:1
	//1:1.5
	//1:2
	//1:3
	//dip2px()  dip---->px
	public static int dip2px(int dip){
		//1,获取当前手机的dip和px的转换关系比例值,不同的手机执行此段代码的时候,得到的比例值可能不一致
		float density = getResources().getDisplayMetrics().density;
		//2,将dp转换成px
		return (int)(dip*density+0.5);
	}

	//px2dip
	public static int px2dip(int px){
		//1,获取当前手机的dip和px的转换关系比例值,不同的手机执行此段代码的时候,得到的比例值可能不一致
		float density = getResources().getDisplayMetrics().density;
		//2,将dp转换成px
		return (int)(px/density+0.5);
	}


	public static Drawable getDrawable(int drawableId) {
		return getResources().getDrawable(drawableId);
	}

	/**
	 * 获取一个颜色选择器的对象	
	 * @param mTabTextColorResId	颜色选择器id
	 * @return
	 */
	public static ColorStateList getColorStateList(int mTabTextColorResId) {
		return getResources().getColorStateList(mTabTextColorResId);
	}



	public static byte[] hexStringToByteArray(String s) {
		if (s.length() % 2 != 0) {
			StringBuilder stringBuilder = new StringBuilder(s);
			stringBuilder.insert(s.length()-1,"0");
			s = stringBuilder.toString();
		}


		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}


	public static boolean isRightHexStr(String str){
		String reg = "^[0-9a-fA-F]+$";
		return str.matches(reg);
	}

}
