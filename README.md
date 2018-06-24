# MultiPicturesSelector
## 仿微信的多图选择器 实现了主要的几个功能 
 首先添加存储权限  进入之前 android 6.0+ 请先请求权限

 如果开启了相机  android 7.0 请加入FileProvider

   <provider
             android:name="android.support.v4.content.FileProvider"
             android:authorities="com.fan.multipicturesselector"
             android:exported="false"
             android:grantUriPermissions="true">
             <meta-data
                 android:name="android.support.FILE_PROVIDER_PATHS"
                 android:resource="@xml/paths" />
         </provider>

## 配置选项  

 
 Config config = Config.get();  
 
 config.setMaxNum(15);//最大选择数  
 
 config.setMinMum(1);//最小选择数  
 
 config.setOpenEdit(false);//是否可以涂鸦  
 
 config.setOpenClip(false);//是否可以剪切      

 config.isOpenCamera = false;//是否开启相机

 启动 startActivityForResult(new Intent(this, MultiPicturesSelectorActivity.class), 0);
 
 回调   paths = data.getStringArrayListExtra("paths");//返回 路径数组

##截图
![image](https://github.com/fanyaopeng/MultiPicturesSelector/blob/master/images/Screenshot_20180622-230917.jpg)

![image](https://github.com/fanyaopeng/MultiPicturesSelector/blob/master/images/Screenshot_20180622-230921.jpg)

![image](https://github.com/fanyaopeng/MultiPicturesSelector/blob/master/images/Screenshot_20180622-230931.jpg)

![image](https://github.com/fanyaopeng/MultiPicturesSelector/blob/master/images/Screenshot_20180622-230938.jpg)

![image](https://github.com/fanyaopeng/MultiPicturesSelector/blob/master/images/Screenshot_20180622-230948.jpg)

![image](https://github.com/fanyaopeng/MultiPicturesSelector/blob/master/images/Screenshot_20180622-230956.jpg)

## 使用

dependencies {
   	        implementation 'com.github.fanyaopeng:MultiPicturesSelector:v1.0.0'
   	}
