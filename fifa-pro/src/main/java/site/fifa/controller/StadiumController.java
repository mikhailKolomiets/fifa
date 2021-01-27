package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.entity.Stadium;
import site.fifa.service.StadiumService;

@RestController
@RequestMapping("stadium")
public class StadiumController {

    @Autowired
    private StadiumService stadiumService;

    @ApiResponses({@ApiResponse(code = 200, message = "Update ticket price")})
    @PostMapping("change-price")
    public Stadium changeTicketPrice(@RequestParam("price") Integer price, @RequestParam("stadiumId") Long stadiumId) {
        if (price == null)
            return null;
        return stadiumService.changeTicketPrice(price, stadiumId);
    }

}
