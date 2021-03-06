package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.entity.WIKI;
import site.fifa.service.WIKIService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("wiki")
public class WikiController {

    @Autowired
    private WIKIService wikiService;

    @ApiResponses({@ApiResponse(code = 200, message = "Create and return the new wiki info unit")})
    @PostMapping("create")
    public WIKI createWiki(@RequestParam String name, @RequestParam String description) {
        return wikiService.createWiki(WIKI.builder()
                .name(name).description(description)
                .created(LocalDateTime.now()).modify(LocalDateTime.now())
                .build()
        );
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Update and return the new wiki info unit")})
    @PutMapping("update/{wikiId}")
    public WIKI updateWiki(@PathVariable Long wikiId, @RequestParam(required = false) String name, @RequestParam(required = false) String description) {
        return wikiService.editWiki(WIKI.builder().id(wikiId)
                .name(name).description(description)
                .created(LocalDateTime.now()).modify(LocalDateTime.now())
                .build()
        );
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return full list of wikis")})
    @GetMapping("get-all")
    public List<WIKI> getAllWikis() {
        return wikiService.getAllWikis();
    }

}
