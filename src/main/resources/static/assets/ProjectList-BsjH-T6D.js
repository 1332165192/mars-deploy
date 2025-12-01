import{d as _e,c as q,o as $,a as H,u as ye,r as p,b as V,l as k,m as Pe,e as a,w as o,h as u,y as I,f as v,g as Te,i as Ie,B as c,j as b,q as Y,t as J,z as D,s as i,x as K,A as F}from"./index-ZRmCAO4x.js";import{g as Ee,u as Re,a as $e,b as Ae,c as we,d as ke,e as Ce}from"./project-DCaa_oZL.js";import{g as Oe}from"./server-k8iYyxoR.js";import{t as Ue}from"./build-Csp7udDc.js";import{g as Le,a as je}from"./user-B4dTOeaI.js";import{_ as xe}from"./_plugin-vue_export-helper-DLdCVoit.js";import{S as Se,A as W,C as Me}from"./SearchSharp-DVD_wjTH.js";import{P as Ne}from"./PeopleSharp-Bx_u4HmC.js";import{T as ze}from"./TrashSharp-J88QsNrE.js";const Ge={xmlns:"http://www.w3.org/2000/svg","xmlns:xlink":"http://www.w3.org/1999/xlink",viewBox:"0 0 512 512"},Be=H("path",{d:"M256 48C141.31 48 48 141.31 48 256s93.31 208 208 208s208-93.31 208-208S370.69 48 256 48zm-56 296V168l144 88z",fill:"currentColor"},null,-1),Ve=[Be],Ye=_e({name:"PlayCircleSharp",render:function(C,m){return $(),q("svg",Ge,Ve)}}),Je={class:"project-list"},Ke={class:"toolbar"},Fe={__name:"ProjectList",setup(X){const C=Te(),m=ye(),A=p(!1),_=p(!1),y=p(!1),O=p(null),P=p(null),E=p(""),U=p(),L=p([]),Q=p([]),T=p([]),j=p([]),f=p([]),g=V({page:1,pageSize:10,itemCount:0,showSizePicker:!0,pageSizes:[10,20,50]}),Z=[{label:"Java",value:"JAVA"},{label:"Vue",value:"VUE"}],w=p([{label:"mvn clean package -DskipTests",value:"mvn clean package -DskipTests"},{label:"mvn clean package",value:"mvn clean package"},{label:"mvn clean install -DskipTests",value:"mvn clean install -DskipTests"},{label:"gradle build",value:"gradle build"},{label:"gradle build -x test",value:"gradle build -x test"}]),ee=[{label:"target/*.jar",value:"target/*.jar"},{label:"target/*.war",value:"target/*.war"},{label:"build/libs/*.jar",value:"build/libs/*.jar"},{label:"dist",value:"dist"},{label:"build",value:"build"},{label:"out",value:"out"}],x=p([]);k(()=>T.value.includes("ADMIN"));const S=k(()=>T.value.includes("ADMIN")||T.value.includes("PROJECT_ADMIN")),M=k(()=>S.value),le=[{title:"ID",key:"id",width:60},{title:"项目名称",key:"name"},{title:"项目类型",key:"projectType",render:l=>i(K,{type:l.projectType==="JAVA"?"info":"success",size:"small"},{default:()=>l.projectType})},{title:"Git地址",key:"gitUrl",ellipsis:{tooltip:!0}},{title:"分支",key:"branch"},{title:"创建时间",key:"createTime"},{title:"操作",key:"action",width:350,render:l=>{const e=[];return(S.value||T.value.includes("DEVELOPER"))&&e.push(i(F,{onPositiveClick:()=>pe(l.id)},{default:()=>"确定要部署该项目吗？",trigger:()=>i(c,{text:!0,type:"info",size:"small"},{default:()=>"部署项目",icon:()=>i("i",{class:"n-icon"},[i(Ye)])})})),M.value&&e.push(i(c,{text:!0,type:"success",size:"small",onClick:()=>N(l)},{default:()=>"编辑",icon:()=>i("i",{class:"n-icon"},[i(Me)])}),i(c,{text:!0,type:"warning",size:"small",onClick:()=>me(l.id)},{default:()=>"分配成员",icon:()=>i("i",{class:"n-icon"},[i(Ne)])})),M.value&&e.push(i(F,{onPositiveClick:()=>ne(l.id)},{default:()=>"确定要删除吗？",trigger:()=>i(c,{text:!0,type:"error",size:"small"},{default:()=>"删除",icon:()=>i("i",{class:"n-icon"},[i(ze)])})})),i(I,{},{default:()=>e})}}],t=V({name:"",description:"",gitUrl:"",branch:"master",gitUsername:"",gitPassword:"",projectType:"JAVA",buildCommand:"",buildDir:"",serverIds:[],autoDeploy:0,deployScript:"",appPort:8080}),ae={name:{required:!0,message:"请输入项目名称",trigger:"blur"},gitUrl:{required:!0,message:"请输入Git地址",trigger:"blur"},projectType:{required:!0,message:"请选择项目类型",trigger:"change"},buildCommand:{required:!0,message:"请输入构建命令",trigger:"blur"},buildDir:{required:!0,message:"请输入产物路径",trigger:"blur"}},h=async()=>{try{A.value=!0;const l={current:g.page,size:g.pageSize};E.value&&(l.name=E.value);const e=await Ee(l);L.value=e.records,g.itemCount=e.total}catch(l){console.error(l)}finally{A.value=!1}},te=async()=>{try{const l=await Oe({current:1,size:100});Q.value=l.records,x.value=l.records.map(e=>({label:e.name,value:e.id}))}catch(l){console.error(l)}},oe=l=>{g.page=l,h()},re=l=>{g.pageSize=l,g.page=1,h()},N=async l=>{if(l){P.value=l.id;try{const e=await Ce(l.id),s=e.project,n=e.serverIds||[];Object.assign(t,{name:s.name||"",description:s.description||"",gitUrl:s.gitUrl||"",branch:s.branch||"master",gitUsername:s.gitUsername||"",gitPassword:s.gitPassword||"",projectType:s.projectType||"JAVA",buildCommand:s.buildCommand||"",buildDir:s.buildDir||"",serverIds:n,autoDeploy:s.autoDeploy!=null?s.autoDeploy:0,deployScript:s.deployScript||R(s.projectType),appPort:s.appPort||8080})}catch(e){console.error("获取项目详情失败:",e),m.error("获取项目详情失败")}}else P.value=null,Object.assign(t,{name:"",description:"",gitUrl:"",branch:"master",gitUsername:"",gitPassword:"",projectType:"JAVA",buildCommand:"",buildDir:"",serverIds:[],autoDeploy:0,deployScript:R("JAVA"),appPort:8080});_.value=!0},se=async()=>{var l;try{await((l=U.value)==null?void 0:l.validate());const e={...t,serverIds:t.serverIds||[],autoDeploy:t.autoDeploy||0,appPort:t.appPort||8080};P.value?(e.id=P.value,await Re(e),m.success("更新成功")):(await $e(e),m.success("添加成功")),_.value=!1,h()}catch(e){console.error(e)}},ne=async l=>{try{await ke(l),m.success("删除成功"),h()}catch(e){console.error(e)}},ie=l=>{l==="JAVA"?(w.value=[{label:"mvn clean package -DskipTests",value:"mvn clean package -DskipTests"},{label:"mvn clean package",value:"mvn clean package"},{label:"mvn clean install -DskipTests",value:"mvn clean install -DskipTests"},{label:"mvn clean install",value:"mvn clean install"},{label:"gradle build -x test",value:"gradle build -x test"},{label:"gradle build",value:"gradle build"},{label:"gradle bootJar",value:"gradle bootJar"}],t.buildCommand||(t.buildCommand="mvn clean package -DskipTests")):l==="VUE"&&(w.value=[{label:"npm install && npm run build",value:"npm install && npm run build"},{label:"npm run build",value:"npm run build"},{label:"yarn install && yarn build",value:"yarn install && yarn build"},{label:"yarn build",value:"yarn build"},{label:"pnpm install && pnpm build",value:"pnpm install && pnpm build"},{label:"pnpm build",value:"pnpm build"}],t.buildCommand||(t.buildCommand="npm install && npm run build")),t.autoDeploy===1&&(t.deployScript=R(l))},ue=l=>{l===1&&(t.deployScript=R(t.projectType))},R=l=>(l||t.projectType)==="VUE"?`#!/bin/bash
# Vue 项目部署脚本

DEPLOY_DIR="{{uploadPath}}"
WEB_ROOT="/www/wwwroot"
PROJECT_NAME="{{projectName}}"
TARGET_DIR="$WEB_ROOT/$PROJECT_NAME"
BACKUP_DIR="$WEB_ROOT/backup"

echo "========================================"
echo "开始部署Vue项目"
echo "========================================"

# 1. 备份旧版本
echo "[步骤1] 备份旧版本..."
if [ -d "$TARGET_DIR" ]; then
  mkdir -p $BACKUP_DIR
  BACKUP_NAME="\${PROJECT_NAME}_$(date +%Y%m%d_%H%M%S)"
  mv $TARGET_DIR $BACKUP_DIR/$BACKUP_NAME
  echo "已备份为: $BACKUP_NAME"
  
  # 保留最近3个备份
  ls -t $BACKUP_DIR | grep "^\${PROJECT_NAME}_" | tail -n +4 | xargs -I {} rm -rf $BACKUP_DIR/{}
fi

# 2. 部署新版本
echo "[步骤2] 部署新版本..."
mkdir -p $TARGET_DIR

# 检查是否为压缩包
if [ -f "$DEPLOY_DIR/dist.zip" ]; then
  echo "检测到 dist.zip 文件"
  # 先解压到临时目录
  cd $DEPLOY_DIR
  unzip -o -q dist.zip
  
  # 检查解压后的结构
  if [ -d "$DEPLOY_DIR/dist" ]; then
    # 解压后有 dist 目录，复制其内容
    cp -r $DEPLOY_DIR/dist/* $TARGET_DIR/
    echo "已解压 dist.zip 并复制到 $TARGET_DIR"
  else
    # 解压后直接是文件，移动到目标目录
    mv $DEPLOY_DIR/* $TARGET_DIR/ 2>/dev/null || true
    echo "已解压 dist.zip 到 $TARGET_DIR"
  fi
elif [ -f "$DEPLOY_DIR/dist.tar.gz" ]; then
  tar -xzf $DEPLOY_DIR/dist.tar.gz -C $DEPLOY_DIR
  cp -r $DEPLOY_DIR/dist/* $TARGET_DIR/
  echo "已解压 dist.tar.gz 到 $TARGET_DIR"
elif [ -d "$DEPLOY_DIR/dist" ]; then
  cp -r $DEPLOY_DIR/dist/* $TARGET_DIR/
  echo "已复制 dist 目录到 $TARGET_DIR"
else
  echo "错误: 未找到构建产物（dist目录或压缩包）"
  exit 1
fi

# 3. 设置文件权限
echo "[步骤3] 设置文件权限..."
chmod -R 755 $TARGET_DIR

echo "========================================"
echo "部署完成！"
echo "部署目录: $TARGET_DIR"
echo "访问路径: http://your-domain/$PROJECT_NAME"
echo "========================================"`:`#!/bin/bash
# Java 项目部署脚本

APP_NAME="app"
APP_PORT={{appPort}}
DEPLOY_DIR="{{uploadPath}}"
JAR_FILE=$(ls $DEPLOY_DIR/*.jar | grep -v '\\.bak$' | head -n 1)
LOG_FILE="$DEPLOY_DIR/app.log"

echo "========================================"
echo "开始部署Java应用"
echo "========================================"

# 1. 停止旧进程
echo "[步骤1] 正在停止旧进程..."
PID=$(lsof -t -i:$APP_PORT 2>/dev/null)
if [ ! -z "$PID" ]; then
  kill -15 $PID
  sleep 3
  if ps -p $PID > /dev/null 2>&1; then
    kill -9 $PID
    echo "已强制停止进程: $PID"
  else
    echo "已优雅停止进程: $PID"
  fi
else
  echo "未找到运行中的进程"
fi

sleep 2

# 2. 备份旧版本
echo "[步骤2] 备份旧版本..."
if [ -f "$DEPLOY_DIR/app.jar" ]; then
  BACKUP_NAME="app_$(date +%Y%m%d_%H%M%S).jar.bak"
  mv $DEPLOY_DIR/app.jar $DEPLOY_DIR/$BACKUP_NAME
  echo "已备份为: $BACKUP_NAME"
  ls -t $DEPLOY_DIR/*.jar.bak 2>/dev/null | tail -n +4 | xargs rm -f 2>/dev/null
fi

# 3. 重命名新上传的jar
echo "[步骤3] 准备新版本..."
if [ -f "$JAR_FILE" ]; then
  cp $JAR_FILE $DEPLOY_DIR/app.jar
  echo "已准备新版本: app.jar"
else
  echo "错误: 未找到jar文件"
  exit 1
fi

# 4. 启动新应用
echo "[步骤4] 正在启动应用..."
cd $DEPLOY_DIR

# 清空旧日志
> $LOG_FILE

# 后台启动应用
nohup java -jar -Xms512m -Xmx1024m -Dserver.port=$APP_PORT app.jar > $LOG_FILE 2>&1 &
NEW_PID=$!

echo "应用已启动，PID: $NEW_PID"
echo "日志文件: $LOG_FILE"

# 5. 等待应用启动并实时显示日志
echo "[步骤5] 等待应用启动..."
echo "----------------------------------------"
echo "应用启动日志："
echo "----------------------------------------"

# 实时显示日志并检测启动成功
START_TIME=$(date +%s)
TIMEOUT=60
STARTED=false

# 使用 tail -f 实时显示日志，同时检测启动状态
(
  tail -f $LOG_FILE &
  TAIL_PID=$!
  
  while true; do
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    
    # 检查超时
    if [ $ELAPSED -gt $TIMEOUT ]; then
      kill $TAIL_PID 2>/dev/null
      echo ""
      echo "----------------------------------------"
      echo "警告: 应用启动超时（60秒）"
      echo "请检查日志文件: $LOG_FILE"
      exit 1
    fi
    
    # 检查端口是否已监听
    if lsof -t -i:$APP_PORT > /dev/null 2>&1; then
      sleep 2
      kill $TAIL_PID 2>/dev/null
      STARTED=true
      break
    fi
    
    # 检查进程是否还在运行
    if ! ps -p $NEW_PID > /dev/null 2>&1; then
      kill $TAIL_PID 2>/dev/null
      echo ""
      echo "----------------------------------------"
      echo "错误: 应用进程已退出"
      echo "请检查日志文件: $LOG_FILE"
      exit 1
    fi
    
    sleep 1
  done
  
  if [ "$STARTED" = true ]; then
    echo ""
    echo "----------------------------------------"
    echo "应用启动成功！"
    echo "========================================"
    echo "部署完成！"
    echo "应用端口: $APP_PORT"
    echo "应用PID: $NEW_PID"
    echo "日志文件: $LOG_FILE"
    echo "========================================"
    exit 0
  fi
) || exit 1`,pe=async l=>{try{const e=await Ue(l);m.success("构建任务已创建，正在跳转..."),C.push(`/build-detail/${e.buildId}`)}catch(e){console.error(e),m.error("部署失败")}},de=async()=>{try{const l=await Le();T.value=l.permissions||[]}catch(l){console.error("加载用户权限失败",l)}},ce=[{title:"用户",key:"userId",render:(l,e)=>{const s=j.value.map(n=>({label:`${n.nickname||n.username} (${n.username})`,value:n.id}));return i(D,{value:l.userId,options:s,placeholder:"请选择用户",style:{width:"250px"},onUpdateValue:n=>{f.value[e].userId=n}})}},{title:"角色",key:"roleType",render:(l,e)=>{const s=[{label:"开发者",value:"DEVELOPER"},{label:"成员",value:"MEMBER"}];return i(D,{value:l.roleType,options:s,style:{width:"120px"},onUpdateValue:n=>{f.value[e].roleType=n}})}},{title:"操作",key:"actions",width:100,render:(l,e)=>l.roleType==="OWNER"?i(K,{type:"success"},{default:()=>"拥有者"}):i(c,{text:!0,type:"error",size:"small",onClick:()=>be(e)},{default:()=>"移除"})}],me=async l=>{O.value=l;try{const e=await je({current:1,size:100});j.value=e.records;const s=await we(l);f.value=s.map(n=>({userId:n.userId,roleType:n.roleType,user:n.user})),y.value=!0}catch{m.error("加载成员列表失败")}},ve=()=>{f.value.push({userId:null,roleType:"MEMBER"})},be=l=>{f.value.splice(l,1)},fe=async()=>{try{const l=f.value.filter(e=>e.userId&&e.roleType!=="OWNER").map(e=>({userId:e.userId,roleType:e.roleType}));await Ae(O.value,l),m.success("保存成功"),y.value=!1}catch{m.error("保存失败")}};return Pe(()=>{de(),h(),te()}),(l,e)=>{const s=v("n-icon"),n=v("n-input"),z=v("n-data-table"),d=v("n-form-item"),G=v("n-radio"),ge=v("n-radio-group"),he=v("n-input-number"),De=v("n-form"),B=v("n-modal");return $(),q("div",Je,[H("div",Ke,[a(u(I),null,{default:o(()=>[a(n,{value:E.value,"onUpdate:value":e[0]||(e[0]=r=>E.value=r),placeholder:"搜索项目名称",clearable:"",style:{width:"250px"},onKeyup:Ie(h,["enter"])},{suffix:o(()=>[a(u(c),{text:"",onClick:h},{default:o(()=>[a(s,null,{default:o(()=>[a(u(Se))]),_:1})]),_:1})]),_:1},8,["value"]),a(u(c),{type:"primary",onClick:e[1]||(e[1]=r=>N())},{icon:o(()=>[a(s,null,{default:o(()=>[a(u(W))]),_:1})]),default:o(()=>[e[19]||(e[19]=b(" 新增项目 ",-1))]),_:1})]),_:1})]),a(z,{columns:le,data:L.value,pagination:g,loading:A.value,remote:!0,"onUpdate:page":oe,"onUpdate:pageSize":re},null,8,["data","pagination","loading"]),a(B,{show:_.value,"onUpdate:show":e[16]||(e[16]=r=>_.value=r),title:P.value?"编辑项目":"新增项目",preset:"dialog",style:{width:"800px"},"show-icon":!1},{action:o(()=>[a(u(I),null,{default:o(()=>[a(u(c),{onClick:e[15]||(e[15]=r=>_.value=!1)},{default:o(()=>[...e[22]||(e[22]=[b("取消",-1)])]),_:1}),a(u(c),{type:"primary",onClick:se},{default:o(()=>[...e[23]||(e[23]=[b("确定",-1)])]),_:1})]),_:1})]),default:o(()=>[a(De,{ref_key:"formRef",ref:U,model:t,rules:ae,"label-placement":"left","label-width":"110"},{default:o(()=>[a(d,{label:"项目名称",path:"name"},{default:o(()=>[a(n,{value:t.name,"onUpdate:value":e[2]||(e[2]=r=>t.name=r),placeholder:"请输入项目名称"},null,8,["value"])]),_:1}),a(d,{label:"项目描述",path:"description"},{default:o(()=>[a(n,{value:t.description,"onUpdate:value":e[3]||(e[3]=r=>t.description=r),type:"textarea",rows:3,placeholder:"请输入项目描述"},null,8,["value"])]),_:1}),a(d,{label:"Git地址",path:"gitUrl"},{default:o(()=>[a(n,{value:t.gitUrl,"onUpdate:value":e[4]||(e[4]=r=>t.gitUrl=r),placeholder:"https://github.com/xxx/xxx.git"},null,8,["value"])]),_:1}),a(d,{label:"分支",path:"branch"},{default:o(()=>[a(n,{value:t.branch,"onUpdate:value":e[5]||(e[5]=r=>t.branch=r),placeholder:"master"},null,8,["value"])]),_:1}),a(d,{label:"Git用户名",path:"gitUsername"},{default:o(()=>[a(n,{value:t.gitUsername,"onUpdate:value":e[6]||(e[6]=r=>t.gitUsername=r),placeholder:"请输入Git用户名（可选）"},null,8,["value"])]),_:1}),a(d,{label:"Git密码",path:"gitPassword"},{default:o(()=>[a(n,{value:t.gitPassword,"onUpdate:value":e[7]||(e[7]=r=>t.gitPassword=r),type:"password","show-password-on":"click",placeholder:"请输入Git密码（可选）"},null,8,["value"])]),_:1}),a(d,{label:"项目类型",path:"projectType"},{default:o(()=>[a(u(D),{value:t.projectType,"onUpdate:value":[e[8]||(e[8]=r=>t.projectType=r),ie],options:Z,placeholder:"请选择项目类型"},null,8,["value"])]),_:1}),a(d,{label:"构建命令",path:"buildCommand"},{default:o(()=>[a(u(D),{value:t.buildCommand,"onUpdate:value":e[9]||(e[9]=r=>t.buildCommand=r),options:w.value,tag:"",filterable:"",placeholder:"请选择或自定义构建命令"},null,8,["value","options"])]),_:1}),a(d,{label:"产物路径",path:"buildDir"},{default:o(()=>[a(u(D),{value:t.buildDir,"onUpdate:value":e[10]||(e[10]=r=>t.buildDir=r),options:ee,tag:"",filterable:"",placeholder:"请选择或自定义产物路径"},null,8,["value"])]),_:1}),a(d,{label:"部署服务器",path:"serverIds"},{default:o(()=>[a(u(D),{value:t.serverIds,"onUpdate:value":e[11]||(e[11]=r=>t.serverIds=r),options:x.value,multiple:"",clearable:"",placeholder:"请选择部署服务器（可多选）"},null,8,["value","options"])]),_:1}),a(d,{label:"自动部署",path:"autoDeploy"},{default:o(()=>[a(ge,{value:t.autoDeploy,"onUpdate:value":[e[12]||(e[12]=r=>t.autoDeploy=r),ue]},{default:o(()=>[a(G,{value:1},{default:o(()=>[...e[20]||(e[20]=[b("是",-1)])]),_:1}),a(G,{value:0},{default:o(()=>[...e[21]||(e[21]=[b("否",-1)])]),_:1})]),_:1},8,["value"])]),_:1}),t.autoDeploy===1?($(),Y(d,{key:0,label:"应用端口",path:"appPort"},{default:o(()=>[a(he,{value:t.appPort,"onUpdate:value":e[13]||(e[13]=r=>t.appPort=r),min:1,max:65535,placeholder:"8080",style:{width:"100%"}},null,8,["value"])]),_:1})):J("",!0),t.autoDeploy===1?($(),Y(d,{key:1,label:"部署脚本",path:"deployScript"},{default:o(()=>[a(n,{value:t.deployScript,"onUpdate:value":e[14]||(e[14]=r=>t.deployScript=r),type:"textarea",rows:8,placeholder:"部署脚本将自动生成，也可自定义"},null,8,["value"])]),_:1})):J("",!0)]),_:1},8,["model"])]),_:1},8,["show","title"]),a(B,{show:y.value,"onUpdate:show":e[18]||(e[18]=r=>y.value=r),title:"项目成员管理",preset:"dialog",style:{width:"700px"},"show-icon":!1},{action:o(()=>[a(u(I),null,{default:o(()=>[a(u(c),{onClick:e[17]||(e[17]=r=>y.value=!1)},{default:o(()=>[...e[25]||(e[25]=[b("取消",-1)])]),_:1}),a(u(c),{type:"primary",onClick:fe},{default:o(()=>[...e[26]||(e[26]=[b("保存",-1)])]),_:1})]),_:1})]),default:o(()=>[a(u(I),{vertical:""},{default:o(()=>[a(u(c),{type:"primary",size:"small",onClick:ve},{icon:o(()=>[a(s,null,{default:o(()=>[a(u(W))]),_:1})]),default:o(()=>[e[24]||(e[24]=b(" 添加成员 ",-1))]),_:1}),a(z,{columns:ce,data:f.value,pagination:!1},null,8,["data"])]),_:1})]),_:1},8,["show"])])}}},tl=xe(Fe,[["__scopeId","data-v-ebd00609"]]);export{tl as default};
