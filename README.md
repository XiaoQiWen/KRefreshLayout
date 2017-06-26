# KRefreshLayout (JRefreshLayout)
kotlin和java两个版本的下拉刷新框架,支持任意View、支持定制任意header
## Download[![Version](https://img.shields.io/badge/release-1.2-green.svg)](https://github.com/XiaoQiWen/KRefreshLayout/releases)
#### KRefreshLayout
gradle
```
compile 'gorden.refresh:refresh-kotlin:1.2'
```
maven
```
<dependency>
  <groupId>gorden.refresh</groupId>
  <artifactId>refresh-kotlin</artifactId>
  <version>1.2</version>
  <type>pom</type>
</dependency>
```
``注意:kotlin版本目前需要下载插件或者使用AndroidStudio3.0+``</br>
#### JRefreshLayout
gradle
```
compile 'gorden.refresh:refresh-java:1.2'
```
maven
```
<dependency>
  <groupId>gorden.refresh</groupId>
  <artifactId>refresh-java</artifactId>
  <version>1.2</version>
  <type>pom</type>
</dependency
```
## example
[DEMO下载](https://github.com/XiaoQiWen/Resources/raw/master/KRefreshLayout/demo1.2.apk)</br></br>
![](https://github.com/XiaoQiWen/Resources/raw/master/KRefreshLayout/gif0.gif)
![](https://github.com/XiaoQiWen/Resources/raw/master/KRefreshLayout/gif1.gif)
</br></br>
![](https://github.com/XiaoQiWen/Resources/raw/master/KRefreshLayout/gif2.gif)
![](https://github.com/XiaoQiWen/Resources/raw/master/KRefreshLayout/gif3.gif)
</br></br>
![](https://github.com/XiaoQiWen/Resources/raw/master/KRefreshLayout/gif4.gif)
![](https://github.com/XiaoQiWen/Resources/raw/master/KRefreshLayout/gif5.gif)
## Usage
``KRefreshLayou详细使用说明:``
>* [RefreshHeader接口说明](https://github.com/XiaoQiWen/KRefreshLayout/wiki/RefreshHeader%E6%96%B9%E6%B3%95%E8%AF%B4%E6%98%8E)
>* [RefreshLayout开放Api](https://github.com/XiaoQiWen/KRefreshLayout/wiki/RefreshLayout%E5%BC%80%E6%94%BEApi)
>* [XML参数配置](https://github.com/XiaoQiWen/KRefreshLayout/wiki/RefreshHeader-XML%E5%8F%AF%E9%85%8D%E7%BD%AE%E5%8F%82%E6%95%B0)
>* [IOS边缘滚动效果实现](https://github.com/XiaoQiWen/KRefreshLayout/wiki/%E4%BB%BFIos%E8%BE%B9%E7%BC%98%E6%BB%9A%E5%8A%A8%E6%95%88%E6%9E%9C)
>* [微信刷新Header实现](https://github.com/XiaoQiWen/KRefreshLayout/wiki/%E5%BE%AE%E4%BF%A1%E5%88%B7%E6%96%B0Header%E5%AE%9E%E7%8E%B0)

设置刷新监听
```
refreshLayout.setKRefreshListener {
    refreshLayout.postDelayed({
    //这里的true是指刷新成功，在header接口中complete能接收到这参数
    refreshLayout.refreshComplete(true)
    }, 2000)
}
```
更多请参考Demo
### 联系方式
* QQ:   354419188
* Email:    gordenxqw@gmail.com

### License
    Copyright (C) 2017 XiaoQiWen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
