package com.dwg.springmv.service.impl;

import com.dwg.springmv.annotation.EnjoyService;
import com.dwg.springmv.service.DwgService;

@EnjoyService("dwgServiceImpl")
public class DwgServiceImpl implements DwgService {

    @Override
    public String query(String name, String age) {
        return "name========="+name+"   ;age ========="+age;
    }
}
