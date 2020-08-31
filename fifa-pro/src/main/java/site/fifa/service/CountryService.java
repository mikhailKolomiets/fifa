package site.fifa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.fifa.entity.Country;
import site.fifa.repository.CountryRepository;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final List<Country> stubCountries = Arrays.asList(
        new Country(1L, "Ukraine", 1),
        new Country(2L, "Germany", 1),
        new Country(3L, "France", 1)
    );

    public List<Country> getCountries() {
        List<Country> countries = (List<Country>) countryRepository.findAll();
        return countries.isEmpty() ? stubCountries : countries;
    }
}
