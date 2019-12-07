package com.dwg.springmv.servlet;

import com.dwg.springmv.annotation.*;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    List<String> classNames=new ArrayList<String>();

    Map<String,Object> beans=new HashMap<String,Object>();

    Map<String,Object> handlerMap=new HashMap<String, Object>();

    //tomcat启动的时候实例化map ioc
    public void init(ServletConfig config){
        basePackageScan("com.dwg");

        //对classNames进行实例化
        doInstance();

        doAutowired();

        doUrlMapping();

    }

    public void doUrlMapping(){
        for(Map.Entry<String,Object> entry: beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(EnjoyController.class)) {
                EnjoyRequestMapping mapping1 = clazz.getAnnotation(EnjoyRequestMapping.class);
                String classPath=mapping1.value();
                Method[] methods = clazz.getMethods();
                for(Method method:methods){
                    if(method.isAnnotationPresent(EnjoyRequestMapping.class)){
                        EnjoyRequestMapping mapping2=method.getAnnotation(EnjoyRequestMapping.class);
                        String methodPath=mapping2.value();
                        String requestPath=classPath+methodPath;
                        handlerMap.put(requestPath,method);
                    }else{
                        continue;
                    }
                }
            }else{
                continue;
            }
        }
    }

    public void doAutowired(){
        for(Map.Entry<String,Object> entry: beans.entrySet()){
            Object instance=entry.getValue();
            Class<?> clazz = instance.getClass();
            if(clazz.isAnnotationPresent(EnjoyController.class)){
                Field[] fields = clazz.getDeclaredFields();
                for(Field field:fields){
                    if(field.isAnnotationPresent(EnjoyAutowired.class)){
                        EnjoyAutowired auto=field.getAnnotation(EnjoyAutowired.class);
                        String key = auto.value();
                        Object bean = beans.get(key);
                        field.setAccessible(true);
                        try {
                            field.set(instance,bean);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }else{
                        continue;
                    }
                }
            }else{
                continue;
            }
        }
    }

    public void doInstance(){
        for(String className:classNames){
            String cn=className.replace(".class","");
            try {
                Class<?> clazz = Class.forName(cn);
                if(clazz.isAnnotationPresent(EnjoyController.class)){
                    //控制类
                    Object instance = clazz.newInstance();
                    EnjoyRequestMapping mapping= clazz.getAnnotation(EnjoyRequestMapping.class);
                    String key=mapping.value();
                    beans.put(key,instance);
                }else if(clazz.isAnnotationPresent(EnjoyService.class)){
                    //服务类
                    Object instance = clazz.newInstance();
                    EnjoyService service= clazz.getAnnotation(EnjoyService.class);
                    String key=service.value();
                    beans.put(key,instance);
                }else {
                    continue;
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private void basePackageScan(String basePackage){
        //扫描编译好的类路径......class
        URL url=this.getClass()
                .getClassLoader()
                .getResource("/"+basePackage.replaceAll("\\.","/"));
        String fileStr=url.getFile();
        File file=new File(fileStr);
        String[] filesStr = file.list();
        for(String path:filesStr){
            File filePath=new File(fileStr+path);
            if(filePath.isDirectory()){
                basePackageScan(basePackage+"."+path);
            }else{
                //com.enjoy.....xxxx.class
                classNames.add(basePackage+"."+filePath.getName());
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String uri=req.getRequestURI();
        String context = req.getContextPath();
        String path=uri.replace(context,"");
        Method method= (Method) handlerMap.get(path);
        Object instance =  beans.get("/" + path.split("/")[1]);
        Object[] args = hand(req, resp, method);
        try {
            method.invoke(instance,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static Object[] hand(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Method method){
        Class<?>[] paramClazzs=method.getParameterTypes();
        Object[] args=new Object[paramClazzs.length];
        int args_i=0;
        int index=0;
        for(Class<?> paramClazz:paramClazzs){

            if(ServletRequest.class.isAssignableFrom(paramClazz)){
                args[args_i++]=request;
            }
            if(ServletResponse.class.isAssignableFrom(paramClazz)){
                args[args_i++]=response;
            }
            Annotation[] paramAns=method.getParameterAnnotations()[index];
            if(paramAns.length>0){
                for(Annotation paramAn:paramAns){
                    if(EnjoyRequestParam.class.isAssignableFrom(paramAn.getClass())){
                        EnjoyRequestParam rp= (EnjoyRequestParam) paramAn;
                        args[args_i++]=request.getParameter(rp.value());
                    }
                }
            }
        }
        return args;
    }

}
