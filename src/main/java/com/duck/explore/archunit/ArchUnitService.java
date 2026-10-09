package com.duck.explore.archunit;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ArchUnitService {

    public String hello(String name) {
        if(StringUtils.hasText(name)) {
            return "Hello " + name;
        }

        return "Hello";
    }
}
