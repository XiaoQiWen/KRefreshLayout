# KRefreshLayout (JRefreshLayout)
kotlin和java两个版本的下拉刷新框架,支持任意View、支持定制任意header
## Download[![Version](https://img.shields.io/badge/release-1.1-green.svg)](https://github.com/XiaoQiWen/KRefreshLayout/releases)
* KRefreshLayout</br>
gradle
```
compile 'gorden.refresh:refresh-kotlin:1.1'
```
maven
```
<dependency>
  <groupId>gorden.refresh</groupId>
  <artifactId>refresh-kotlin</artifactId>
  <version>1.1</version>
  <type>pom</type>
</dependency>
```
`注意kotlin版本目前需要下载插件或者使用AndroidStudio3.0+`
---
* JRefreshLayout</br>
gradle
```
compile 'gorden.refresh:refresh-java:1.1'
```
maven
```
<dependency>
  <groupId>gorden.refresh</groupId>
  <artifactId>refresh-java</artifactId>
  <version>1.1</version>
  <type>pom</type>
</dependency
```
## example
[DEMO下载](https://github.com/XiaoQiWen/Resources/raw/master/KRefreshLayout/demo.apk)</br></br>
![](https://github.com/XiaoQiWen/IMG/raw/master/KRefreshLayout/gif0.gif)
![](https://github.com/XiaoQiWen/IMG/raw/master/KRefreshLayout/gif1.gif)
</br></br>
![](https://github.com/XiaoQiWen/IMG/raw/master/KRefreshLayout/gif2.gif)
![](https://github.com/XiaoQiWen/IMG/raw/master/KRefreshLayout/gif3.gif)
</br></br>
![](https://github.com/XiaoQiWen/IMG/raw/master/KRefreshLayout/gif4.gif)
## Usage
``KRefreshLayou详细使用说明:``
>* [RefreshHeader接口说明](/wiki/RefreshHeader方法说明)
>* [RefreshLayout开放Api](/wiki/RefreshLayout开放Api)
>* [XML参数配置](/wiki/RefreshHeader-XML可配置参数)

设置刷新监听
```
refreshLayout.setKRefreshListener {
        refreshLayout.postDelayed({
        //这里的true是指刷新成功，在header接口中complete能接收到这参数
        refreshLayout.refreshComplete(true)
        }, 2000)
}
```
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
