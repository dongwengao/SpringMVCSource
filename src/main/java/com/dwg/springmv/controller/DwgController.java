package com.dwg.springmv.controller;

import com.dwg.springmv.annotation.EnjoyAutowired;
import com.dwg.springmv.annotation.EnjoyController;
import com.dwg.springmv.annotation.EnjoyRequestMapping;
import com.dwg.springmv.annotation.EnjoyRequestParam;
import com.dwg.springmv.service.DwgService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@EnjoyController
@EnjoyRequestMapping("/dwg")
public class DwgController {

    @EnjoyAutowired("dwgServiceImpl")
    private DwgService dwgService;

    @EnjoyRequestMapping("/query")
    public void query(HttpServletRequest request,
                      HttpServletResponse response,
                      @EnjoyRequestParam("name") String name,
                      @EnjoyRequestParam("age") String age){

        try {
            PrintWriter writer = response.getWriter();
            String result = dwgService.query(name, age);
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
