package site.fifa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.entity.Country;
import site.fifa.service.CountryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping(value = "/countries")
    public List<Country> getCountries() {
        return countryService.getCountries();
    }
}
