package com.example.acbingo.myfinaltest10.FileManagerService;

import java.text.DecimalFormat;
import java.util.Date;

public class Utils {
	// 更具length获取对应的显示，分为四个等级
	public String sizeAddUnit(long size) {
		char[] unit = new char[] { 'B', 'K', 'M', 'G' };
		int index = 0;
		int div = 1;
		for (; index < unit.length; index++) {
			div *= 1024;
			if (size < div)
				break;
		}
		div /= 1024;
		if (size % div == 0)
			return String.valueOf(size / div) + unit[index];
		else {
			DecimalFormat df = new DecimalFormat("#.00");
			return df.format(size * 1.0 / div) + unit[index];
		}
	}

	// 获取年月日的显示格式
	public String timeFormat(Date date) {
		return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(date);
	}
}
