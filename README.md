# KRefreshLayout    and   JRefreshLayout
强大的下拉刷新库，定制任意Header。比官方SwipRefrehLayout更加强大，更加好用</br>
支持定制任意header,需实现KRefreshHeader or JRefreshHeader。实现方式可参考demo
### 示例
[DEMO下载](https://github.com/XiaoQiWen/KRefreshLayout/raw/master/apk/demo.apk)</br>
---
demo截图:
1. 一个没任何效果的header,这样实现类似于ios overscroll</br></br>
![](https://github.com/XiaoQiWen/KRefreshLayout/blob/master/screenshot/gif0.gif) </br> </br>
2.经典的下拉刷新模式</br></br>
![](https://github.com/XiaoQiWen/KRefreshLayout/blob/master/screenshot/gif1.gif)  </br></br>
3.官方SwipeRefreshLayout样式</br></br>
![](https://github.com/XiaoQiWen/KRefreshLayout/blob/master/screenshot/gif2.gif)  </br></br>
4.ViewPager场景Storehouse样式</br></br>
![](https://github.com/XiaoQiWen/KRefreshLayout/blob/master/screenshot/gif3.gif)  </br></br>
5.Coordinatorlayout场景</br></br>
![](https://github.com/XiaoQiWen/KRefreshLayout/blob/master/screenshot/gif4.gif)  </br></br>
### 使用方式
* xml布局中
```
<gorden.refresh.KRefreshLayout
      android:id="@+id/refreshLayout"
      android:layout_width="match_parent"
      android:layout_height="match_parent">
      <gorden.krefreshlayout.demo.header.NullHeader
          android:layout_width="match_parent"
          android:layout_height="wrap_content" />
      <GridView
          android:id="@+id/gridView"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:numColumns="3"
          android:verticalSpacing="5dp"
          android:horizontalSpacing="5dp"
          />
</gorden.refresh.KRefreshLayout>

```
或者这样
```
<gorden.refresh.KRefreshLayout
      android:id="@+id/refreshLayout"
      android:layout_width="match_parent"
      android:layout_height="match_parent">
      <android.support.v7.widget.RecyclerView
          android:id="@+id/recyclerView"
          android:layout_width="match_parent"
          android:layout_height="match_parent"/>
</gorden.refresh.KRefreshLayout>
```
之后在代码中动态添加Header
```
refreshLayout.setHeaderView(ClassicalHeader(context))
```
* Header实现方式</br>
首先我们来看看接口定义
```
interface KRefreshHeader {
    /**
     * 返回headerView
     */
    fun getView(): View
    /**
     * 刷新成功停滞时间
     */
    fun succeedRetention(): Long
    /**
     * 刷新失败停滞时间
     */
    fun failingRetention(): Long

    /**
     * 刷新高度，达到这个高度将触发刷新
     */
    fun refreshHeight(): Int

    /**
     * Content最大下拉高度,无特殊要求
     */
    fun maxOffsetHeight(): Int

    /**
     *  当内容视图到达顶部，视图将被重置
     */
    fun onReset(refreshLayout: KRefreshLayout)

    /**
     * 准备下拉，作初始化工作
     */
    fun onPrepare(refreshLayout: KRefreshLayout)

    /**
     * 刷新中
     */
    fun onRefresh(refreshLayout: KRefreshLayout)

    /**
     * 刷新完成
     * @param isSuccess 成功还是失败
     */
    fun onComplete(refreshLayout: KRefreshLayout,isSuccess:Boolean)

    /**
     * 拖拽中
     * @param distance 当前滑动距离
     * @param percent 当前移动比例  0f - max  1.0为刷新临界点
     * @param refreshing 是否处于刷新中
     */
    fun onScroll(refreshLayout: KRefreshLayout, distance: Int, percent: Float, refreshing: Boolean)
```
每个方法都有注释,然后我们来写自己的ClassicalHeader
```
public class ClassicalHeader extends FrameLayout implements KRefreshHeader{
    private ImageView arrawImg;
    private TextView textTitle;
    private RotateAnimation rotateAnimation = new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    
    public ClassicalHeader(@NonNull Context context) {
        this(context,null);
        
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        addView(root,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        ((LayoutParams)root.getLayoutParams()).gravity = Gravity.CENTER;

        arrawImg = new ImageView(context);
        arrawImg.setImageResource(R.drawable.ic_arrow_down);
        arrawImg.setScaleType(ImageView.ScaleType.CENTER);
        root.addView(arrawImg);

        textTitle = new TextView(context);
        textTitle.setTextSize(13);
        textTitle.setText("下拉刷新...");
        textTitle.setTextColor(Color.parseColor("#999999"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.leftMargin = 20;
        root.addView(textTitle,params);

        rotateAnimation.setDuration(800);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);
        setPadding(0, DensityUtil.dip2px(15),0,DensityUtil.dip2px(15));
        
    }
    
    @NotNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public long succeedRetention() {
        return 200;
    }

    @Override
    public long failingRetention() {
        return 0;
    }

    @Override
    public int refreshHeight() {
        return getHeight();
    }

    @Override
    public int maxOffsetHeight() {
        return 4*getHeight();
    }


    boolean isReset = true;

    @Override
    public void onReset(@NotNull KRefreshLayout refreshLayout) {
        Log.e(TAG,"----------------> onReset");
        arrawImg.setImageResource(R.drawable.ic_arrow_down);
        textTitle.setText("下拉刷新...");
        isReset = true;
        arrawImg.setVisibility(VISIBLE);
    }

    @Override
    public void onPrepare(@NotNull KRefreshLayout refreshLayout) {
        Log.e(TAG,"----------------> onPrepare");
        arrawImg.setImageResource(R.drawable.ic_arrow_down);
        textTitle.setText("下拉刷新...");
    }

    @Override
    public void onRefresh(@NotNull KRefreshLayout refreshLayout) {
        Log.e(TAG,"----------------> onRefresh");
        arrawImg.setImageResource(R.drawable.ic_loading);
        arrawImg.startAnimation(rotateAnimation);
        textTitle.setText("加载中...");
        isReset = false;
    }

    @Override
    public void onComplete(@NotNull KRefreshLayout refreshLayout,boolean isSuccess) {
        Log.e(TAG,"----------------> onComplete");
        arrawImg.clearAnimation();
        arrawImg.setVisibility(GONE);
        if (isSuccess){
            textTitle.setText("刷新完成...");
        }else{
            textTitle.setText("刷新失败...");
        }
    }

    boolean attain = false;

    @Override
    public void onScroll(@NotNull KRefreshLayout refreshLayout, int distance, float percent,boolean refreshing) {
        Log.e(TAG,"----------------> onScroll  "+percent);

        if (!refreshing&&isReset){
            if(percent>=1&&!attain){
                attain = true;
                textTitle.setText("释放刷新...");
                arrawImg.animate().rotation(-180).start();
            }else if (percent<1&&attain){
                attain = false;
                arrawImg.animate().rotation(0).start();
                textTitle.setText("下拉刷新...");
            }
        }
    }
}
```
是不是很简单,通过实现KRefreshHeader 可以实现任意Header效果,可参考Demo中添加的效果
---
---
---
### KRefreshLayout 配置
* xml配置参数
```
app:k_keepheader="true" //刷新时，是否让Header停留。false会直接收回去
app:k_pincontent="false"  // 为True 就是SwipRefreshLayout效果
app:k_durationoffset="200"  // 收缩动画持续时间,回到刷新高度，或回到默认位置动画时间

```
* 代码配置
```
refreshLayout.setPinContent(false)
refreshLayout.setKeepHeaderWhenRefresh(true)
refreshLayout.setDurationOffset(200)
```
* 设置刷新监听
```
refreshLayout.setKRefreshListener {
    refreshLayout.postDelayed({
        //这里的true是指刷新成功，在header接口中complete能接收到这参数
        refreshLayout.refreshComplete(true)
    }, 2000)
}
```
# 联系方式
* 邮箱:gordenxqw@gmail.com
* QQ:354419188
* phone:不给
---
### 如果大家有什么觉得可以优化的地方，可以给我建议，或者与我讨论。
### Gradle and Maven 暂不支持，我有空就提交上去，方便大家使用
