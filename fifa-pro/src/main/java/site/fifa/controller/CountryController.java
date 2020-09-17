package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.entity.Country;
import site.fifa.service.CountryService;

import java.util.List;

@RestController
@RequestMapping(value = "/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @ApiResponses({@ApiResponse(code = 200, message = "Return all countries in project")})
    @GetMapping
    public List<Country> getCountries() {
        return countryService.getCountries();
    }
}
