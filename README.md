# MultiPicturesSelector
 仿微信的多图选择器 实现了主要的几个功能
 #用法
 ##配置选项  Config config = Config.get();
                config.setMaxNum(15);//最大选择数
                config.setMinMum(1);//最小选择数
                config.setOpenEdit(false);//是否可以涂鸦
                config.setOpenClip(false);//是否可以剪切
 ## 启动 startActivityForResult(new Intent(this, MultiPicturesSelectorActivity.class), 0);
 ##回调   paths = data.getStringArrayListExtra("paths");//返回 路径数组

