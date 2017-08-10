package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static java.util.Collections.singletonMap;

/**
 * Created by sincalza on 09/08/17.
 */
@Controller
public class Index {

    @GetMapping("/index")
    public ModelAndView index(HttpServletRequest httpServletRequest) {
        return new ModelAndView("index", singletonMap("user", httpServletRequest.getUserPrincipal().getName()));
    }

}
