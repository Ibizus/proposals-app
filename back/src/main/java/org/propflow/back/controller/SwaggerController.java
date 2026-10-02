package org.propflow.back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/openapi")
public class SwaggerController {

  @GetMapping({ "", "/" })
  public RedirectView openApi() {
    return new RedirectView("http://localhost:8080/swagger-ui/index.html");
  }
}