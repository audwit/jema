package edu.mu.mscs.ubicomp.ema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DashboardController {

  @RequestMapping("/index.html")
  public String greeting() {
    return "index";
  }

}
